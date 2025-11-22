package com.poetryforum.dto.esdto;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.poetryforum.entity.Poem;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;
import java.util.List;

/**
 * 诗文 ES 包装类
 */
@Document(indexName = "poem")
@Data
public class PoemEsDTO implements Serializable {

    /**
     * id，必须打上id注解，保证ESid与实体id相一致
     */
    @Id
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 创建诗人 id
     */
    private Long poetId;

    private static final long serialVersionUID = 1L;

    /**
     * 对象转包装类
     *
     * @param poem
     * @return
     */
    public static PoemEsDTO objToDto(Poem poem) {
        if (poem == null) {
            return null;
        }
        PoemEsDTO poemEsDTO = new PoemEsDTO();
        BeanUtils.copyProperties(poem, poemEsDTO);
        String tagsStr = poem.getTags();
        if (StringUtils.isNotBlank(tagsStr)) {
            poemEsDTO.setTags(JSONUtil.toList(tagsStr, String.class));
        }
        return poemEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param poemEsDTO
     * @return
     */
    public static Poem dtoToObj(PoemEsDTO poemEsDTO) {
        if (poemEsDTO == null) {
            return null;
        }
        Poem poem = new Poem();
        BeanUtils.copyProperties(poemEsDTO, poem);
        List<String> tagList = poemEsDTO.getTags();
        if (CollUtil.isNotEmpty(tagList)) {
            poem.setTags(JSONUtil.toJsonStr(tagList));
        }
        return poem;
    }
}
