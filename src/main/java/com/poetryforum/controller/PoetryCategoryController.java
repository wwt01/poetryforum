package com.poetryforum.controller;


import com.poetryforum.dto.Result;
import com.poetryforum.service.IPoetryCategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


@RestController
@RequestMapping("/poetry-category")
public class PoetryCategoryController {
    @Resource
    private IPoetryCategoryService typeService;

    @GetMapping("list")
    public Result queryTypeList() {
        return typeService.queryTypeList();
    }
}
