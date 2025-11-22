package com.poetryforum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poetryforum.dto.Result;
import com.poetryforum.dto.search.SearchRequest;
import com.poetryforum.entity.Poem;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 位文同
 */
public interface IPoemService extends IService<Poem> {
    Result queryById(Long id);

    Result update(Poem poem);

    Result queryPoemBySearchText(@RequestBody SearchRequest searchRequest);

}
