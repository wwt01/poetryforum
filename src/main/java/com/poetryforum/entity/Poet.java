package com.poetryforum.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_poet")
public class Poet implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    // 诗人姓名
    private String name;


    // 诗人简介
    private String intro;

    // 朝代
    private String dynasty;

    // 代表作品
    private String masterpieces;

    // 头像
    private String avatar;

    // 评分（1-10分）
    private Integer score;

    // 作品数量
    private Integer worksCount;

    // 点赞数
    private Integer liked;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

}