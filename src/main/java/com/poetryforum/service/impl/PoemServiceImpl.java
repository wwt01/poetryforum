package com.poetryforum.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poetryforum.dto.esdto.PoemEsDTO;
import com.poetryforum.dto.Result;
import com.poetryforum.dto.search.SearchRequest;
import com.poetryforum.entity.Poem;
import com.poetryforum.mapper.PoemMapper;
import com.poetryforum.service.IPoemService;
import com.poetryforum.utils.CacheClient;
import com.poetryforum.utils.RedisConstants;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.poetryforum.utils.RedisConstants.CACHE_POEM_KEY;

@Service
@Slf4j
public class PoemServiceImpl extends ServiceImpl<PoemMapper, Poem> implements IPoemService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Resource
    private CacheClient cacheClient;

    public Result queryById(Long id) {
//        //缓存穿透
//        Shop shop = queryWithPassThrough(id);
        //使用封装的工具类解决缓存穿透
        Poem poem = cacheClient.queryWithPassThrough(CACHE_POEM_KEY, id, Poem.class, this::getById, RedisConstants.CACHE_POEM_TTL, TimeUnit.MINUTES);

//        //用互斥锁解决缓存击穿
//        Shop shop = queryWithMutex(id);

//        //逻辑过期解决缓存击穿代码
//        Shop shop = queryWithLogicalExpire(id);
        //实用工具类
//        Shop shop = cacheClient
//                .queryWithLogicalExpire(RedisConstants.CACHE_SHOP_KEY, id, Shop.class, this::getById, 20L, TimeUnit.SECONDS);

        if(poem == null)
            return Result.fail("诗词不存在");
        return Result.ok(poem);
    }

    @Override
    public Result update(Poem poem) {
        //1.获取诗词id
        Long id=poem.getId();
        if(id==null)
            return Result.fail("诗词id不能为空");
        //更新数据库
        if(updateById(poem)) {
            //2.删除缓存
            stringRedisTemplate.delete(CACHE_POEM_KEY + id);
            return Result.ok();
        }
        return Result.fail("更新失败");
    }

    @Override
    public Result queryPoemBySearchText(@RequestBody SearchRequest searchRequest) {

        //1.获取参数
        String searchText = searchRequest.getSearchText();
        long current = searchRequest.getCurrent();
        long pageSize = searchRequest.getPageSize();

        //TODO 缓存待定
        //2.在缓存中查询
//        处理空关键词（避免缓存key重复）
        String cacheSearchText = StrUtil.isBlank(searchText) ? "empty" : searchText.trim();
        // 缓存Key：区分关键词、页码、页大小（和上一段代码缓存策略一致）
        String cacheKey = RedisConstants.CACHE_POEM_SEARCH_KEY + ":" + cacheSearchText + ":" + current + ":" + pageSize;
        //2.1如果查到直接返回
        Map<Object, Object> cacheMap = stringRedisTemplate.opsForHash().entries(cacheKey);
        if (!cacheMap.isEmpty()) {
            // 缓存命中，反序列化为 Page<Poem> 并返回
            Page<Poem> cachedPage = new Page<>();
            cachedPage.setTotal(Long.parseLong((String) cacheMap.get("total")));
            cachedPage.setRecords(JSON.parseArray((String) cacheMap.get("records"), Poem.class));
            cachedPage.setCurrent(current);
            cachedPage.setSize(pageSize);
            return Result.ok(cachedPage);
        }
        //2.2如果查不到到ES中查询

        //3.在ES中查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // ========== 核心过滤条件：仅基于搜索关键词 ==========
        if (searchText != null && !searchText.trim().isEmpty()) {
            boolQueryBuilder
                    // 1. tags 精确匹配（关键词需和标签完全一致）
                    .should(QueryBuilders.termQuery("tags", searchText).boost(4.0f))
                    // 2. title 分词匹配（ik_max_word 分词，适配存储逻辑）
                    .should(QueryBuilders.matchQuery("title", searchText)
                            .analyzer("ik_max_word") // 显式指定分词器，和存储时一致
                            .boost(3.0f)) // title 权重高于 content
                    // 3. content 分词匹配（ik_max_word 分词）
                    .should(QueryBuilders.matchQuery("content", searchText)
                            .analyzer("ik_max_word")
                            .boost(1.5f));

            // 必须满足至少一个条件（避免查询到所有数据）
            boolQueryBuilder.minimumShouldMatch(1);
        } else {
            // 若关键词为空，返回所有值
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }
        // =====================================

        // 分页：ES 分页从 0 开始，若前端 current 从 1 开始，需转为 (int)(current - 1)
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询：修复 withSorts 报错（用 withSort 单次添加一个排序条件）
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withPageable(pageRequest)
                // 修复：用 withSort（单数），单次添加一个排序条件（可多次调用叠加）
                .withSort(SortBuilders.fieldSort("_score").order(SortOrder.DESC)) // 按匹配得分降序
                .build();

        // 执行 ES 查询
        SearchHits<PoemEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PoemEsDTO.class);

        // 结果转换：修复 getContent() 报错（遍历 SearchHit 而非 SearchHits）
        Page<Poem> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Poem> resourceList = new ArrayList<>();

        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PoemEsDTO>> searchHitList = searchHits.getSearchHits();
            List<Long> poemIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            List<Poem> poemList = baseMapper.selectBatchIds(poemIdList);
            if (poemList != null) {
                Map<Long, List<Poem>> idPoemMap = poemList.stream().collect(Collectors.groupingBy(Poem::getId));
                poemIdList.forEach(postId -> {
                    if (idPoemMap.containsKey(postId)) {
                        resourceList.add(idPoemMap.get(postId).get(0));
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PoemEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }

        page.setRecords(resourceList);
        page.setCurrent(current);
        page.setSize(pageSize);

        // 5. 缓存存入（核心：将 Page 拆分为 Hash 字段存储）
        if (page.getRecords() != null) {
            // 构建 Hash 存储的键值对（key：字段名，value：序列化后的字符串）
            Map<String, String> redisHash = new HashMap<>(4);
            redisHash.put("total", String.valueOf(page.getTotal())); // 总条数（Long → String）
            redisHash.put("records", JSON.toJSONString(page.getRecords())); // 诗词列表（List<Poem> → JSON 字符串）
            redisHash.put("current", String.valueOf(page.getCurrent())); // 页码（可选，避免读取时依赖请求参数）
            redisHash.put("size", String.valueOf(page.getSize())); // 页大小（可选，同上）

            // 存入 Redis Hash
            stringRedisTemplate.opsForHash().putAll(cacheKey, redisHash);
            // 设置过期时间（10 分钟，可根据业务调整，比如诗词数据更新少可设 1 小时）
            stringRedisTemplate.expire(cacheKey, 10, TimeUnit.MINUTES);
        }

        return Result.ok(page);
    }
}
