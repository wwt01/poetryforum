package com.poetryforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poetryforum.dto.Result;
import com.poetryforum.entity.CollectionOrder;

public interface ICollectionOrderService extends IService<CollectionOrder> {
    Result limitedCollection(Long collectionId);

    void createCollectionOrder(CollectionOrder collectionOrder);
}
