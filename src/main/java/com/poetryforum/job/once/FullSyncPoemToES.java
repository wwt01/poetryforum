package com.poetryforum.job.once;

import cn.hutool.core.collection.CollUtil;
import com.poetryforum.dto.esdto.CollectionEsDTO;
import com.poetryforum.dto.esdto.PoemEsDTO;
import com.poetryforum.entity.Poem;
import com.poetryforum.entity.PoetryCollection;
import com.poetryforum.esdao.CollectionEsDao;
import com.poetryforum.service.IPoemService;
import com.poetryforum.esdao.PoemEsDao;
import com.poetryforum.service.IPoetryCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FullSyncPoemToES implements CommandLineRunner {

    @Resource
    private PoemEsDao poemEsDao;
    @Resource
    private CollectionEsDao collectionEsDao;

    @Resource
    private IPoemService poemService;
    @Resource
    private IPoetryCollectionService poetryCollectionService;

    @Override
    public void run(String... args) throws Exception {

        //诗词部分
        List<Poem> poemList = poemService.list();
        if(!CollUtil.isEmpty(poemList)) {
            List<PoemEsDTO> poemEsDTOList = poemList.stream().map(PoemEsDTO::objToDto).collect(Collectors.toList());
            final int pageSize = 500;
            int total = poemEsDTOList.size();
            log.info("诗词部分 start, total {}", total);
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                log.info("sync from {} to {}", i, end);
                poemEsDao.saveAll(poemEsDTOList.subList(i, end));
            }
            log.info("诗词部分 end, total {}", total);
        }

        //诗集部分
        List<PoetryCollection> poetryCollectionList = poetryCollectionService.list();
        if(!CollUtil.isEmpty(poetryCollectionList)) {
            List<CollectionEsDTO> collectionEsDTOList = poetryCollectionList.stream().map(CollectionEsDTO::objToDto).collect(Collectors.toList());
            final int pageSize = 500;
            int total = collectionEsDTOList.size();
            log.info("诗集部分 start, total {}", total);
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                collectionEsDao.saveAll(collectionEsDTOList.subList(i, end));
            }
            log.info("诗集部分 end, total {}", total);
        }
    }
}
