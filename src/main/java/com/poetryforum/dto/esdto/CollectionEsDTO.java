package com.poetryforum.dto.esdto;

import com.poetryforum.entity.PoetryCollection;
import lombok.Data;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

/**
 * 诗文 ES 包装类
 */
@Document(indexName = "poetry_collection")
@Data
public class CollectionEsDTO implements Serializable {

    /**
     * id，必须打上id注解，保证ESid与实体id相一致
     */
    @Id
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 简介
     */
    private String intro;


    private static final long serialVersionUID = 1L;

    /**
     * 对象转包装类
     *
     * @param collection
     * @return
     */
    public static CollectionEsDTO objToDto(PoetryCollection collection) {
        if (collection == null) {
            return null;
        }
        CollectionEsDTO collectionEsDTO = new CollectionEsDTO();
        BeanUtils.copyProperties(collection, collectionEsDTO);
        return collectionEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param collectionEsDTO
     * @return
     */
    public static PoetryCollection dtoToObj(CollectionEsDTO collectionEsDTO) {
        if (collectionEsDTO == null) {
            return null;
        }
        PoetryCollection collection = new PoetryCollection();
        BeanUtils.copyProperties(collectionEsDTO, collection);
        return collection;
    }
}
