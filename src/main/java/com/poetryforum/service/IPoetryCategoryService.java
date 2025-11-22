package com.poetryforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poetryforum.dto.Result;
import com.poetryforum.entity.PoetryCategory;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 位文同
 */
public interface IPoetryCategoryService extends IService<PoetryCategory> {

    Result queryTypeList();
}
