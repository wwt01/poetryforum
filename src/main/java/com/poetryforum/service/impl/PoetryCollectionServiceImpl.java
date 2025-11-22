package com.poetryforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.entity.PoetryCollection;
import com.poetryforum.mapper.PoetryCollectionMapper;
import com.poetryforum.service.IPoetryCollectionService;
import org.springframework.stereotype.Service;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class PoetryCollectionServiceImpl extends ServiceImpl<PoetryCollectionMapper, PoetryCollection> implements IPoetryCollectionService {

}
