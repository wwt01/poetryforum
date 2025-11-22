package com.poetryforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_collection")
public class PoetryCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 诗集名称
    private String name;

    // 简介
    private String intro;

    //售价
    private BigDecimal price;

    // 类型（1：普通合集 2：限量合集）
    private Integer type;

    // 状态（0：下架 1：上架）
    private Integer status;

    // 库存（仅限量合集有效）
    @TableField(exist = false)
    private Integer stock;

    // 开始时间（仅限量合集有效）
    @TableField(exist = false)
    private LocalDateTime beginTime;

    // 结束时间（仅限量合集有效）
    @TableField(exist = false)
    private LocalDateTime endTime;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;
}