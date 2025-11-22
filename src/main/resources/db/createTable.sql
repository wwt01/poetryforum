SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库（若不存在）
CREATE DATABASE IF NOT EXISTS pf_db;
USE pf_db;

-- ----------------------------
-- 诗词分类表（核心分类表，替代原诗人分类表）
-- 存储诗词的朝代/流派分类（如唐诗、宋词-豪放派等）
-- ----------------------------
DROP TABLE IF EXISTS `tb_poetry_category`;
CREATE TABLE `tb_poetry_category` (
                                      `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      `name` VARCHAR(50) NOT NULL COMMENT '分类名称（如：唐诗、宋词-豪放派、元曲）',
                                      `icon` VARCHAR(255) COMMENT '分类图标URL',
                                      `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重（数字越小越靠前）',
                                      `create_time` DATETIME NOT NULL DEFAULT NOW(),
                                      `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW()
) COMMENT '诗词分类表（朝代/流派分类）';

-- ----------------------------
-- 诗人表（保留，关联诗词）
-- ----------------------------
DROP TABLE IF EXISTS `tb_poet`;
CREATE TABLE `tb_poet` (
                           `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                           `name` VARCHAR(50) NOT NULL COMMENT '诗人姓名（如：李白、苏轼）',
                           `dynasty` VARCHAR(20) NOT NULL COMMENT '所属朝代（如：唐代、北宋）',
                           `intro` TEXT COMMENT '诗人简介',
                           `masterpieces` VARCHAR(255) COMMENT '代表作品（用逗号分隔，如：《静夜思》《将进酒》）',
                           `avatar` VARCHAR(255) COMMENT '诗人头像URL',
                           `score` INT DEFAULT 0 COMMENT '诗人声望值（用户评分总和）',
                           `works_count` INT DEFAULT 0 COMMENT '作品数量',
                           `liked` INT DEFAULT 0 COMMENT '诗人点赞数',
                           `create_time` DATETIME NOT NULL DEFAULT NOW(),
                           `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW()
) COMMENT '诗人表';

-- ----------------------------
-- 诗句表（核心内容表，关联诗词分类和诗人）
-- ----------------------------
DROP TABLE IF EXISTS `tb_poem`;
CREATE TABLE `tb_poem` (
                           `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                           `poetry_category_id` BIGINT NOT NULL COMMENT '诗词分类ID（关联tb_poetry_category）',
                           `poet_id` BIGINT NOT NULL COMMENT '诗人ID（关联tb_poet）',
                           `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布用户ID（关联tb_user，改为UNSIGNED兼容）',
                           `title` VARCHAR(100) NOT NULL COMMENT '诗题（如：《静夜思》）',
                           `content` TEXT NOT NULL COMMENT '诗句原文',
                           `tags` VARCHAR(255) COMMENT '标签（用逗号分隔，如：五言绝句、思乡、明月）',
                           `paraphrase` TEXT COMMENT '释义（诗句翻译）',
                           `notes` TEXT COMMENT '注释（关键字解释，JSON格式存储：{"霜":"地上的白霜","举头":"抬头"}）',
                           `creation_background` TEXT COMMENT '创作背景',
                           `liked` INT DEFAULT 0 COMMENT '点赞数',
                           `comments` INT DEFAULT 0 COMMENT '评论数',
                           `views` INT DEFAULT 0 COMMENT '阅读量',
                           `create_time` DATETIME NOT NULL DEFAULT NOW(),
                           `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    -- 外键关联（确保数据一致性）
                           KEY `idx_category` (`poetry_category_id`),
                           KEY `idx_poet` (`poet_id`),
                           KEY `idx_user` (`user_id`),
                           CONSTRAINT `fk_poem_category` FOREIGN KEY (`poetry_category_id`) REFERENCES `tb_poetry_category` (`id`),
                           CONSTRAINT `fk_poem_poet` FOREIGN KEY (`poet_id`) REFERENCES `tb_poet` (`id`),
                           CONSTRAINT `fk_poem_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`)
) COMMENT '诗句表（核心内容表）';

-- ----------------------------
-- 诗词图文笔记表（替代原tb_blog，关联诗词）
-- 用途：用户发布的诗词赏析、读后感、相关图文内容（对应原探店笔记）
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog`;
CREATE TABLE `tb_blog`  (
                            `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键（笔记ID）',
                            `poem_id` bigint(20) NOT NULL COMMENT '关联的诗词ID（关联tb_poem表，替代原shop_id）',
                            `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '发布用户ID（关联tb_user）',
                            `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记标题（如：《静夜思》深度赏析、李白诗歌中的思乡之情）',
                            `images` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '笔记图片，最多9张，多张以逗号隔开（如诗词手写字、相关场景图）',
                            `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '笔记正文（赏析内容、读后感、诗词解读等）',
                            `liked` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '笔记点赞数',
                            `comments` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '笔记评论数（与tb_blog_comments表关联）',
                            `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 索引：优化查询（按诗词ID、用户ID查询笔记）
                            PRIMARY KEY (`id`) USING BTREE,
                            KEY `idx_poem` (`poem_id`), -- 按诗词ID查询相关笔记
                            KEY `idx_user` (`user_id`), -- 按用户ID查询发布的笔记
    -- 外键关联：确保诗词存在才允许创建笔记
                            CONSTRAINT `fk_blog_poem` FOREIGN KEY (`poem_id`) REFERENCES `tb_poem` (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact COMMENT '诗词图文笔记表（用户发布的诗词赏析/读后感）';

-- ----------------------------
-- 诗词笔记评论表（替代原tb_blog_comments，关联诗词笔记）
-- 用途：对诗词图文笔记的评论/回复
-- ----------------------------
DROP TABLE IF EXISTS `tb_blog_comments`;
CREATE TABLE `tb_blog_comments`  (
                                     `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键（评论ID）',
                                     `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '评论用户ID（关联tb_user）',
                                     `blog_id` bigint(20) UNSIGNED NOT NULL COMMENT '关联的诗词笔记ID（关联tb_blog表）',
                                     `parent_id` bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '父评论ID（0=一级评论，非0=回复某条评论）',
                                     `answer_id` bigint(20) UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复目标用户ID（0=无目标，即一级评论；非0=回复某个用户的评论）',
                                     `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '评论内容',
                                     `liked` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '评论点赞数',
                                     `status` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '状态（0=正常，1=被举报，2=禁止查看）',
                                     `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 索引：优化查询（按笔记ID、用户ID、父评论ID查询）
                                     PRIMARY KEY (`id`) USING BTREE,
                                     KEY `idx_blog` (`blog_id`), -- 按笔记ID查询所有评论
                                     KEY `idx_user` (`user_id`), -- 按用户ID查询发布的评论
                                     KEY `idx_parent` (`parent_id`), -- 按父评论ID查询回复（评论楼）
    -- 外键关联：确保笔记存在才允许创建评论
                                     CONSTRAINT `fk_comment_blog` FOREIGN KEY (`blog_id`) REFERENCES `tb_blog` (`id`)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact COMMENT '诗词笔记评论表（对诗词赏析笔记的评论/回复）';

-- ----------------------------
-- 诗集表（合集表，对应原voucher）
-- ----------------------------
DROP TABLE IF EXISTS `tb_collection`;
CREATE TABLE `tb_collection` (
                                 `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 `name` VARCHAR(100) NOT NULL COMMENT '诗集名称（如：《李白经典诗选》《唐诗三百首》）',
                                 `intro` TEXT COMMENT '诗集简介',
                                 `price` DECIMAL(10,2) NOT NULL COMMENT '售价（元）',
                                 `type` TINYINT NOT NULL COMMENT '类型（1=普通诗集，2=限量复刻合集）',
                                 `status` TINYINT DEFAULT 1 COMMENT '状态（0=下架，1=上架）',
                                 `create_time` DATETIME NOT NULL DEFAULT NOW(),
                                 `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW()
) COMMENT '诗集表（诗句合集）';

-- ----------------------------
-- 限量诗集表（对应原seckill_voucher）
-- ----------------------------
DROP TABLE IF EXISTS `tb_limited_collection`;
CREATE TABLE `tb_limited_collection` (
                                         `collection_id` BIGINT PRIMARY KEY COMMENT '诗集ID（关联tb_collection）',
                                         `stock` INT NOT NULL COMMENT '限量库存',
                                         `begin_time` DATETIME NOT NULL COMMENT '抢购开始时间',
                                         `end_time` DATETIME NOT NULL COMMENT '抢购结束时间',
                                         `create_time` DATETIME NOT NULL DEFAULT NOW(),
                                         `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    -- 外键关联
                                         CONSTRAINT `fk_limited_collection` FOREIGN KEY (`collection_id`) REFERENCES `tb_collection` (`id`)
) COMMENT '限量诗集表（复刻合集抢购）';

-- ----------------------------
-- 诗集订单表（对应原voucher_order）- 核心修正：user_id改为BIGINT UNSIGNED
-- ----------------------------
DROP TABLE IF EXISTS `tb_collection_order`;
CREATE TABLE `tb_collection_order` (
                                       `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
                                       `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（关联tb_user，改为UNSIGNED兼容）',
                                       `collection_id` BIGINT NOT NULL COMMENT '诗集ID（关联tb_collection）',
                                       `pay_type` TINYINT COMMENT '支付方式（1=余额，2=支付宝，3=微信）',
                                       `status` TINYINT DEFAULT 1 COMMENT '状态（1=未支付，2=已支付，3=已核销，4=已取消）',
                                       `create_time` DATETIME NOT NULL DEFAULT NOW(),
                                       `pay_time` DATETIME COMMENT '支付时间',
                                       `use_time` DATETIME COMMENT '核销时间（实体发货/电子核销）',
                                       `refund_time` DATETIME COMMENT '退款时间',
                                       `update_time` DATETIME NOT NULL DEFAULT NOW() ON UPDATE NOW(),
    -- 外键关联
                                       KEY `idx_user` (`user_id`),
                                       KEY `idx_collection` (`collection_id`),
                                       CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `tb_user` (`id`), -- 现在类型匹配
                                       CONSTRAINT `fk_order_collection` FOREIGN KEY (`collection_id`) REFERENCES `tb_collection` (`id`)
) COMMENT '诗集订单表';

-- ----------------------------
-- 用户表（保持原结构，AUTO_INCREMENT=1010）
-- ----------------------------
DROP TABLE IF EXISTS `tb_user`;
CREATE TABLE `tb_user` (
                           `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键（1010开始）',
                           `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号码',
                           `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '密码（BCrypt加密）',
                           `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '昵称',
                           `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户头像',
                           `user_type` TINYINT DEFAULT 0 COMMENT '用户类型（0=普通用户，1=诗人认证，2=管理员）',
                           `signature` varchar(255) COMMENT '个性签名',
                           `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`) USING BTREE,
                           UNIQUE INDEX `uniqe_key_phone`(`phone`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1010 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact COMMENT '用户表';
-- ----------------------------
-- Table structure for tb_user_info
-- ----------------------------
DROP TABLE IF EXISTS `tb_user_info`;
CREATE TABLE `tb_user_info`  (
                                 `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '主键，用户id',
                                 `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '城市名称',
                                 `introduce` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '个人介绍，不要超过128个字符',
                                 `fans` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '粉丝数量',
                                 `followee` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '关注的人的数量',
                                 `gender` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '性别，0：男，1：女',
                                 `birthday` date NULL DEFAULT NULL COMMENT '生日',
                                 `credits` int(8) UNSIGNED NULL DEFAULT 0 COMMENT '积分',
                                 `level` tinyint(1) UNSIGNED NULL DEFAULT 0 COMMENT '会员级别，0~9级,0代表未开通会员',
                                 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;