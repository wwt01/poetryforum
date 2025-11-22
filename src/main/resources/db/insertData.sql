SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE pf_db;

-- 1. 诗词分类（2条核心分类）
INSERT INTO `tb_poetry_category` (`name`, `icon`, `sort`) VALUES
                                                              ('唐诗', 'https://example.com/icon/tangshi.png', 1),
                                                              ('宋词-豪放派', 'https://example.com/icon/songci-haofang.png', 2);

-- 2. 诗人（2位核心诗人）
INSERT INTO `tb_poet` (`name`, `dynasty`, `intro`, `masterpieces`, `avatar`, `score`, `works_count`, `liked`) VALUES
                                                                                                                  ('李白', '唐代', '诗仙，浪漫主义诗人代表', '《静夜思》《望庐山瀑布》', 'https://example.com/avatar/libai.jpg', 98, 300, 12500),
                                                                                                                  ('苏轼', '北宋', '豪放派词人，文坛领袖', '《水调歌头》《念奴娇·赤壁怀古》', 'https://example.com/avatar/sushi.jpg', 96, 280, 9900);

-- 3. 测试用户（3个角色：普通用户、诗人认证、管理员）
INSERT INTO `tb_user` (`phone`, `password`, `nick_name`, `icon`, `user_type`, `signature`) VALUES
-- 普通用户（密码：123456，BCrypt加密）
('13800138001', '$2a$10$EixZaYbB.rK4fl8x2qG2iu3x6vF5e8G6Q8f2e7G7H8h9j0k1l2m3n', '诗仙爱好者', 'https://example.com/avatar/user1.jpg', 0, '热爱唐诗宋词'),
-- 诗人认证用户
('13800138002', '$2a$10$EixZaYbB.rK4fl8x2qG2iu3x6vF5e8G6Q8f2e7G7H8h9j0k1l2m3n', '民间诗人', 'https://example.com/avatar/user2.jpg', 1, '偶作闲诗记录生活'),
-- 管理员
('13800138003', '$2a$10$EixZaYbB.rK4fl8x2qG2iu3x6vF5e8G6Q8f2e7G7H8h9j0k1l2m3n', '古诗文管理员', 'https://example.com/avatar/admin.jpg', 2, '传承经典文化');

-- 4. 诗句（3首核心诗词，关联分类、诗人、用户）
INSERT INTO `tb_poem` (
    `poetry_category_id`, `poet_id`, `user_id`, `title`, `content`,
    `tags`, `paraphrase`, `notes`, `creation_background`, `liked`, `comments`, `views`
) VALUES
-- 李白《静夜思》（唐诗）
(1, 1, 1010, '静夜思', '床前明月光，疑是地上霜。举头望明月，低头思故乡。', '五言绝句,思乡,明月',
 '床前洒下皎洁月光，疑是地上白霜。抬头望明月，低头思故乡。',
 '{"床前":"床榻之前","明月光":"皎洁月光","疑是":"怀疑是"}',
 '李白异乡漫游时所作', 5680, 1250, 18900),

-- 苏轼《水调歌头·明月几时有》（宋词-豪放派）
(2, 2, 1010, '水调歌头·明月几时有', '明月几时有？把酒问青天。不知天上宫阙，今夕是何年。', '豪放派,中秋,哲理',
 '明月何时有？端酒问青天。不知天上宫殿，今晚是哪一年。',
 '{"把酒":"端起酒杯","宫阙":"宫殿","今夕":"今晚"}',
 '苏轼思念弟弟苏辙所作', 4230, 1020, 16800),

-- 李白《望庐山瀑布》（唐诗）
(1, 1, 1011, '望庐山瀑布', '日照香炉生紫烟，遥看瀑布挂前川。飞流直下三千尺，疑是银河落九天。', '七言绝句,山水,夸张',
 '日照香炉峰生紫烟，遥看瀑布挂山前。飞流直下三千尺，疑是银河落九天。',
 '{"香炉":"香炉峰","紫烟":"阳光照射水汽形成的烟雾"}',
 '李白游览庐山时所作', 4890, 980, 15600);

-- 5. 博客+评论
-- 假设tb_poem表中已有以下诗词（实际需根据你的诗词数据调整poem_id）
-- 1=《静夜思》（李白）、2=《将进酒》（李白）、3=《水调歌头·明月几时有》（苏轼）
INSERT INTO `tb_blog` VALUES (
     4,
     1, -- poem_id=1（关联《静夜思》）
     1010, -- user_id=1010（默认测试用户）
     '《静夜思》深度赏析：月光下的思乡之情',
     '/imgs/blogs/poem1/1.jpg,/imgs/blogs/poem1/2.jpg',
     '“床前明月光，疑是地上霜。举头望明月，低头思故乡。”\n短短二十字，道尽漂泊者的思乡之苦。\n1. 意象：明月=思乡的经典符号，霜=清冷孤寂的氛围；\n2. 动作：举头→低头，由景入情，情感层层递进；\n3. 语言：朴素自然，却极具画面感，千古流传。\n推荐搭配李白的《春夜洛城闻笛》一起品读，思乡之情更浓～',
     128,
     36,
     '2025-11-18 10:00:00',
     '2025-11-18 15:30:00'
 );

INSERT INTO `tb_blog` VALUES (
     5,
     3, -- poem_id=3（关联《水调歌头·明月几时有》）
     1011, -- user_id=1011（另一测试用户）
     '苏轼《水调歌头》：豁达与浪漫的完美融合',
     '/imgs/blogs/poem3/1.jpg,/imgs/blogs/poem3/2.jpg,/imgs/blogs/poem3/3.jpg',
     '“明月几时有？把酒问青天。”开篇即千古名句，将对宇宙的追问与人生的思考结合。\n- 哲理：“人有悲欢离合，月有阴晴圆缺，此事古难全”——接受不完美，才是人生常态；\n- 浪漫：“但愿人长久，千里共婵娟”——跨越距离的美好祝愿，成为中秋必备佳句；\n- 背景：苏轼被贬黄州期间所作，却无消沉之气，尽显豁达胸襟。',
     96,
     28,
     '2025-11-19 09:15:00',
     '2025-11-19 14:20:00'
 );

-- 笔记评论测试数据（关联上面的笔记）
INSERT INTO `tb_blog_comments` VALUES (
              1,
              1012, -- 评论用户ID
              4, -- 关联笔记ID=4（《静夜思》赏析）
              0, -- 一级评论（无父评论）
              0, -- 无回复目标用户
              '分析得太到位了！尤其是“举头低头”的动作描写，确实把思乡的情感写活了～',
              24,
              0,
              '2025-11-18 11:20:00',
              '2025-11-18 11:20:00'
          );

INSERT INTO `tb_blog_comments` VALUES (
              2,
              1010, -- 笔记作者回复
              4, -- 关联笔记ID=4
              1, -- 回复评论ID=1
              1012, -- 回复目标用户ID=1012
              '谢谢认可～ 李白的诗总能用简单的语言触动人心，这就是经典的力量！',
              12,
              0,
              '2025-11-18 12:05:00',
              '2025-11-18 12:05:00'
          );

-- 6. 诗集（2本：普通+限量）
INSERT INTO `tb_collection` (
    `name`, `intro`, `price`, `type`, `status`
) VALUES
-- 普通诗集（免费）
('李白经典诗选', '精选李白20首代表作', 0.00, 1, 1),
-- 限量诗集（收费）
('宋版《唐诗三百首》复刻', '宣纸线装，限量1000套', 198.00, 2, 1);

-- 7. 限量诗集库存（关联诗集ID=2）
INSERT INTO `tb_limited_collection` (`collection_id`, `stock`, `begin_time`, `end_time`) VALUES
    (2, 1000, '2025-01-01 00:00:00', '2026-12-31 23:59:59');

-- 8. 诗集订单（3条：覆盖不同状态）
INSERT INTO `tb_collection_order` (
    `user_id`, `collection_id`, `pay_type`, `status`, `pay_time`, `use_time`, `refund_time`
) VALUES
-- 已支付已核销（普通诗集）
(1010, 1, NULL, 3, '2025-10-01 10:30:00', '2025-10-02 15:45:00', NULL),
-- 已支付未核销（限量诗集）
(1011, 2, 2, 2, '2025-10-03 09:15:00', NULL, NULL),
-- 未支付（限量诗集）
(1012, 2, NULL, 1, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;