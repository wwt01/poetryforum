package com.poetryforum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.entity.LimitedCollection;
import com.poetryforum.mapper.LimitedCollectionMapper;
import com.poetryforum.service.ILimitedCollectionService;
import org.springframework.stereotype.Service;

@Service
public class LimitedCollectionServiceImpl extends ServiceImpl<LimitedCollectionMapper, LimitedCollection> implements ILimitedCollectionService {
}
