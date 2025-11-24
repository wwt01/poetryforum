package com.poetryforum.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.poetryforum.dto.Result;
import com.poetryforum.dto.search.SearchRequest;
import com.poetryforum.entity.Poem;
import com.poetryforum.service.IPoemService;
import com.poetryforum.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 位文同
 * @since 2025-11-18
 */
@RestController
@RequestMapping("/poem")
public class PoemController {

    @Resource
    public IPoemService poemService;

    /**
     * 根据id查询诗词信息
     * @param id 诗词id
     * @return 诗词详情数据
     */
    @GetMapping("/{id:\\d+}")
    public Result queryPoemById(@PathVariable("id") Long id) {

        return poemService.queryById(id);
    }

    /**
     * 新增诗词信息
     * @param poem 诗词数据
     * @return 诗词id
     */
    @PostMapping
    public Result savePoem(@RequestBody Poem poem) {
        // 写入数据库
        poemService.save(poem);
        // 返回店铺id
        return Result.ok(poem.getId());
    }

    /**
     * 更新诗词信息
     * @param poem 诗词数据
     * @return 无
     */
    @PutMapping
    public Result updatePoem(@RequestBody Poem poem) {
        // 写入数据库
        return poemService.update(poem);
    }

    /**
     * 根据诗词类型分页查询诗词信息
     * @param typeId 诗词类型
     * @param current 页码
     * @return 诗词列表
     */
    @GetMapping("/of/type")
    public Result queryPoemByType(
            @RequestParam("typeId") Integer typeId,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Poem> page = poemService.query()
                .eq("poetry_category_id", typeId)
                .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
    }

    /**
     * 根据诗词名称关键字分页查询诗词信息,可以使用elasticSearch实现
     * @param name 诗词名称关键字
     * @param current 页码
     * @return 诗词列表
     */
    @GetMapping("/of/name")
    public Result queryPoemByName(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        // 根据类型分页查询
        Page<Poem> page = poemService.query()
                .like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 返回数据
        return Result.ok(page.getRecords());
//        return poemService.queryPoemByName(name, current);
    }

    /**
     * 根据诗词关键字分页查询诗词信息,可以使用elasticSearch实现
     //     * @param name 诗词关键字
     //     * @param current 页码
     * @return 诗词列表
     */
    @PostMapping("/search")
    public Result queryPoemBySearchText(
            @RequestBody SearchRequest searchRequest
//            @RequestParam(value = "name", required = false) String name,
//            @RequestParam(value = "current", defaultValue = "1") Integer current
    ) {
        return poemService.queryPoemBySearchText(searchRequest);
    }
}
