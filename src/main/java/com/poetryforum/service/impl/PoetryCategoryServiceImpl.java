package com.poetryforum.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.dto.Result;
import com.poetryforum.entity.PoetryCategory;
import com.poetryforum.mapper.PoetryCategoryMapper;
import com.poetryforum.service.IPoetryCategoryService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 位文同
 */
@Service
public class PoetryCategoryServiceImpl extends ServiceImpl<PoetryCategoryMapper, PoetryCategory> implements IPoetryCategoryService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        //1.从redis查询商铺缓存
        String shopType = stringRedisTemplate.opsForValue().get("poetry-category");
        List<PoetryCategory> list = JSONUtil.toList(shopType, PoetryCategory.class);
        //2.判断是否存在
        if (CollUtil.isNotEmpty(list)) {
            //3.存在，返回
            System.out.println(list);
            return Result.ok(list);
        }
        //4.不存在，根据id查询数据库
        List<PoetryCategory> list1 = query().orderByAsc("sort").list();
        //5.不存在，返回错误
        if (CollUtil.isEmpty(list1)) {
            return Result.fail("没有查询到店铺类型");
        }
        //6.存在，写入redis
        stringRedisTemplate.opsForValue().set("poetry-category", JSONUtil.toJsonStr(list1));

        //7.返回
        return Result.ok(list1);
    }
}
