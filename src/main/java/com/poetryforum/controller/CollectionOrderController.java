package com.poetryforum.controller;

import com.poetryforum.dto.Result;
import com.poetryforum.service.ICollectionOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/collectionOrder")
public class CollectionOrderController {

    @Resource
    private ICollectionOrderService collectionOrderService;

    @PostMapping("limited/{id}")
    public Result seckillVoucher(@PathVariable("id") Long collectionId) {

        return collectionOrderService.limitedCollection(collectionId);
    }
}
