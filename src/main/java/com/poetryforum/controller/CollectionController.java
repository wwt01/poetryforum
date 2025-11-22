package com.poetryforum.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poetryforum.dto.Result;
import com.poetryforum.entity.PoetryCollection;
import com.poetryforum.service.IPoetryCollectionService;
import com.poetryforum.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/poetryCollection")
public class CollectionController {

    @Resource
    private IPoetryCollectionService poetryCollectionService;

    /**
     * 新增普通券
     * @param poetryCollection 诗集信息
     * @return 诗集id
     */
    @PostMapping
    public Result addPoetryCollection(@RequestBody PoetryCollection poetryCollection) {
        poetryCollectionService.save(poetryCollection);
        return Result.ok(poetryCollection.getId());
    }

//    /**
//     * 新增秒杀券
//     * @param PoetryCollection 优惠券信息，包含秒杀信息
//     * @return 优惠券id
//     */
//    @PostMapping("seckill")
//    public Result addSeckillPoetryCollection(@RequestBody PoetryCollection PoetryCollection) {
//        PoetryCollectionService.addSeckillPoetryCollection(PoetryCollection);
//        return Result.ok(PoetryCollection.getId());
//    }
//
    /**
     * 根据诗词名称关键字分页查询诗集信息,可以使用elasticSearch实现
     * @param name 名称关键字
     * @param current 页码
     * @return 诗集列表
     */
    @GetMapping("/of/name")
    public Result queryPoetryCollection(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        //分页查询诗集
        Page<PoetryCollection> page = poetryCollectionService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));

        return Result.ok(page.getRecords());
    }

    @GetMapping("/list")
    public Result queryPoetryCollectionList() {
        List<PoetryCollection> list = poetryCollectionService.query().list();
        return Result.ok(list);
    }
}
