package com.poetryforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.entity.LimitedCollection;
import com.poetryforum.entity.PoetryCollection;
import com.poetryforum.mapper.PoetryCollectionMapper;
import com.poetryforum.service.ILimitedCollectionService;
import com.poetryforum.service.IPoetryCollectionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import static com.poetryforum.utils.RedisConstants.LIMITED_STOCK_KEY;


@Service
public class PoetryCollectionServiceImpl extends ServiceImpl<PoetryCollectionMapper, PoetryCollection> implements IPoetryCollectionService {

    @Resource
    private ILimitedCollectionService limitedCollectionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addLimitedPoetryCollection(PoetryCollection poetryCollection) {
        //保存诗集
        save(poetryCollection);
        //保存限量信息
        LimitedCollection limitedCollection = new LimitedCollection();
        limitedCollection.setCollectionId(poetryCollection.getId());
        limitedCollection.setStock(poetryCollection.getStock());
        limitedCollection.setBeginTime(poetryCollection.getBeginTime());
        limitedCollection.setEndTime(poetryCollection.getEndTime());
        limitedCollectionService.save(limitedCollection);
        //保存库存信息到redis
        stringRedisTemplate.opsForValue().set(LIMITED_STOCK_KEY + poetryCollection.getId(), limitedCollection.getStock().toString());
    }
}
