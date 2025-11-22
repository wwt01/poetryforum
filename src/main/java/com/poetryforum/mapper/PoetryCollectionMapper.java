package com.poetryforum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.poetryforum.entity.PoetryCollection;
import java.util.List;

public interface PoetryCollectionMapper extends BaseMapper<PoetryCollection> {
    // 接口方法名必须和 XML 中 <select> 的 id 一致，参数和返回值也对应
    List<PoetryCollection> queryCollectionWithStock(Integer status);
}