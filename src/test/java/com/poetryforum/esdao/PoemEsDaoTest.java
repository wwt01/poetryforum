package com.poetryforum.esdao;

import com.poetryforum.dto.esdto.PoemEsDTO;
import com.poetryforum.service.IPoemService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class PoemEsDaoTest {

    @Resource
    private PoemEsDao poemEsDao;
    @Resource
    private IPoemService poemService;

    @Test
    void testSelect() {
        System.out.println(poemEsDao.count());
        Page<PoemEsDTO> poemPage = poemEsDao.findAll(
                PageRequest.of(0, 5)
        );
        List<PoemEsDTO> content = poemPage.getContent();
        System.out.println(content);
    }

    @Test
    void testAdd() {
        PoemEsDTO poemEsDTO = new PoemEsDTO() ;
        poemEsDTO.setId(3L);
        poemEsDTO.setTitle("我是一个好人");
        poemEsDTO.setContent("岱宗夫如何，齐鲁青未了。造化钟神秀，阴阳割昏晓。");
        poemEsDTO.setTags(Arrays.asList("山河", "大气磅礴"));
        poemEsDao.save(poemEsDTO);
        System.out.println(poemEsDTO.getId());

    }

    @Test
    void testFindByTitle() {
        List<PoemEsDTO> title = poemEsDao.findByTitle("好人");
        System.out.println(title);
//        System.out.println(poemEsDao.findById(2L));
    }
}
