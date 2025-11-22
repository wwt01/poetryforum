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
@TableName("tb_poem")
public class Poem implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;                  // 主键
    private Long poetId;              // 诗人ID
    private Long poetryCategoryId;            // 分类
    private Long userId;              // 发布用户ID
    private String title;             // 诗题
    private String content;           // 诗句内容
    /**
     * 标签列表 json
     */
    private String tags;              // 标签
    //释义
    private String paraphrase;
    //创作背景
    private String creationBackground;
    private Integer liked;            // 点赞数
    private Integer comments;         // 评论数
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间

    // 非数据库字段
    @TableField(exist = false)
    private String poetName;          // 诗人姓名
    @TableField(exist = false)
    private Boolean isLike;           // 是否点赞
}