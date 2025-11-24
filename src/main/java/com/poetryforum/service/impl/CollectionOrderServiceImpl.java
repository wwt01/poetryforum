package com.poetryforum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.dto.Result;
import com.poetryforum.entity.CollectionOrder;
import com.poetryforum.mapper.CollectionOrderMapper;
import com.poetryforum.service.ICollectionOrderService;
import com.poetryforum.service.ILimitedCollectionService;
import com.poetryforum.utils.RedisIdWorker;
import com.poetryforum.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class CollectionOrderServiceImpl extends ServiceImpl<CollectionOrderMapper, CollectionOrder> implements ICollectionOrderService {

    @Resource
    private ILimitedCollectionService limitedCollectionService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;


    private static final DefaultRedisScript<Long> LIMITED_SCRIPT;
    static {
        LIMITED_SCRIPT = new DefaultRedisScript<>();
        LIMITED_SCRIPT.setLocation(new ClassPathResource("limited.lua"));
        LIMITED_SCRIPT.setResultType(Long.class);
    }

    //创建线程池
    private static final ExecutorService LIMITED_ORDER_EXECUTOR = Executors.newSingleThreadExecutor();//单线程

    //该注解标识在当前类初始化完毕以后来去执行
    @PostConstruct
    private void init() {
        LIMITED_ORDER_EXECUTOR.submit(new CollectionOrderHandler());
    }

    private class CollectionOrderHandler implements Runnable{

        String queueName = "stream.orders";

        @Override
        public void run() {
            // 初始化消费者组
            try {
                // 创建消费者组，如果组已存在则忽略错误
                stringRedisTemplate.opsForStream().createGroup(queueName, "g1");
            } catch (Exception e) {
                log.warn("消费者组 g1 已存在，跳过创建");
            }
            while (true) {
                try {
                    //1.获取消息队列中的订单信息,xreadgroup group g1 c1 count 1 block 2000 streams streams.orders >
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                            StreamOffset.create(queueName, ReadOffset.lastConsumed())
                    );
                    //2.判断消息获取是否成功
                    if(list==null || list.isEmpty()) {
                        //2.1获取失败，没有消息，继续下一次循环
                        continue;
                    }
                    //解析订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    CollectionOrder collectionOrder = BeanUtil.fillBeanWithMap(values, new CollectionOrder(), true);
                    //3.获取成功，可以下单
                    handleCollectionOrder(collectionOrder);
                    //4.ACK确认,SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName,"g1", record.getId());
                } catch (Exception e) {
                    //
                    log.error("处理订单异常error", e);
                    handlePendingList();
                }
            }
        }

        private void handlePendingList() {
            while (true) {
                try {
                    //1.获取pending-list中的订单信息,xreadgroup group g1 c1 count 1 block 2000 streams streams.orders 0
                    List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                            Consumer.from("g1", "c1"),
                            StreamReadOptions.empty().count(1),
                            StreamOffset.create(queueName, ReadOffset.from("0"))
                    );
                    //2.判断消息获取是否成功
                    if(list==null || list.isEmpty()) {
                        //获取失败，pending-list没有异常消息
                        break;
                    }
                    //解析订单信息
                    MapRecord<String, Object, Object> record = list.get(0);
                    Map<Object, Object> values = record.getValue();
                    CollectionOrder collectionOrder = BeanUtil.fillBeanWithMap(values, new CollectionOrder(), true);
                    //3.获取成功，可以下单
                    handleCollectionOrder(collectionOrder);
                    //4.ACK确认,SACK stream.orders g1 id
                    stringRedisTemplate.opsForStream().acknowledge(queueName,"g1", record.getId());
                } catch (Exception e) {
                    log.error("处理pending-list异常error", e);
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }

    private void handleCollectionOrder(CollectionOrder collectionOrder) {
        //1.获取用户
        Long userId = collectionOrder.getUserId();
        //2.获取锁对象
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        //3.获取锁
        boolean isLock = lock.tryLock();
        //4.判断锁是否成功
        if (!isLock) {
            //获取锁失败，返回失败或者重试
            log.error("获取锁失败，返回失败或者重试");
            return;
        }
        //获取锁成功
        try {
            proxy.createCollectionOrder(collectionOrder);
        } finally {
            lock.unlock();
        }
    }


    private ICollectionOrderService proxy;
    @Override
    public Result limitedCollection(Long collectionId) {
        //1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        //获取订单id
        long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(LIMITED_SCRIPT,
                Collections.emptyList(),
                collectionId.toString(), userId.toString(), String.valueOf(orderId)
        );
        // 2.判断结果是否为0
        int r = 0;
        if (result != null) {
            r = result.intValue();
        }
        if(r != 0) {
            //2.1不为0，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }
        //3.获取代理对象(事务),让子线程拿到
        proxy = (ICollectionOrderService) AopContext.currentProxy();
        //返回订单id
        return Result.ok(orderId);
    }

    @Override
    public void createCollectionOrder(CollectionOrder collectionOrder) {
        //获取用户
        Long userId = collectionOrder.getUserId();
        //查询订单
        int count = query().eq("user_id", userId).eq("collection_id", collectionOrder.getCollectionId()).count();
        //判断是否已存在
        if (count > 0) {
            log.error("用户已购买");
            return;
        }
        //扣减库存
        //将查询和更新放在一起，保证事物原子性
        boolean success = limitedCollectionService.update()
                .setSql("stock=stock-1")
                .eq("collection_id", collectionOrder.getCollectionId())
                .gt("stock", 0)
                .update();
        if(!success){
            log.error("库存不足");
            return;
        }
        save(collectionOrder);
    }
}
