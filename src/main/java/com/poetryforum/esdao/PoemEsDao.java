package com.poetryforum.esdao;

import com.poetryforum.dto.esdto.PoemEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 本接口
 * 帖子 ES 操作，继承 Spring Data Elasticsearch 的 ElasticsearchRepository，默认启用简单增删改查
 */
public interface PoemEsDao extends ElasticsearchRepository<PoemEsDTO, Long> {
    List<PoemEsDTO> findByTitle(String title);
//    List<PoemEsDTO> findByPoetId(Long poetId);
}
