package com.poetryforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poetryforum.entity.PoetryCollection;


public interface IPoetryCollectionService extends IService<PoetryCollection> {

    void addLimitedPoetryCollection(PoetryCollection poetryCollection);
}
