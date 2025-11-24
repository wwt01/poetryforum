# Poetry Forum - 诗词论坛后端项目

## 项目基本信息

| 项目属性       | 详情描述                                                                 |
|----------------|--------------------------------------------------------------------------|
| 项目名称       | Poetry Forum（诗词论坛）                                                 |
| 项目描述       | 专注于诗词交流、赏析和分享的在线社区平台，支持诗词浏览、赏析笔记、评论互动、诗集收藏与购买等核心功能 |
| 开发目标       | 打造专业化诗词文化交流社区，促进古典文学爱好者互动与学习                 |
| 版本号         | 1.0.0                                                                    |
| 开发语言       | Java                                                                     |
| 项目架构       | Spring Boot 分层架构（Controller -> Service -> Mapper -> Database）       |
| 开发日期       | 2025年                                                                   |
| 开源协议       | MIT License                                                              |
| GitHub地址     | [https://github.com/your-username/poetry-forum](https://github.com/your-username/poetry-forum)（替换为实际地址） |

## 技术选型

| 技术领域         | 具体技术/框架                          | 版本建议       | 核心作用                                  |
|------------------|---------------------------------------|----------------|-------------------------------------------|
| 核心框架         | Spring Boot                           | 2.7.x          | 快速构建Spring应用，简化配置              |
| ORM框架          | MyBatis-Plus                          | 3.5.x          | 增强MyBatis，提供CRUD接口、分页等功能     |
| 数据库           | MySQL                                 | 8.0            | 关系型数据库，存储核心业务数据（用户、诗词、订单等） |
| 搜索引擎         | Elasticsearch                         | 7.x            | 全文检索，优化诗词、诗集的模糊搜索性能    |
| 缓存             | Redis                                 | 6.0+           | 缓存热点数据（用户会话、分类列表、热门笔记）      |
| 数据同步         | Canal                                 | 1.1.x          | 监听MySQL binlog，同步数据到Elasticsearch |
| 工具类库         | Hutool                                | 5.x            | 简化Java工具类操作（字符串、日期、加密等）        |
| 构建工具         | Maven                                 | 3.6+           | 项目构建、依赖管理                        |
| 日志框架         | SLF4J + Logback                       | 内置           | 系统日志记录                              |

## 库表设计

### 核心表结构说明
| 表名                  | 中文名称               | 核心字段                                                                 | 表作用                                  |
|-----------------------|------------------------|--------------------------------------------------------------------------|-----------------------------------------|
| tb_poetry_category    | 诗词分类表             | id(主键)、name(分类名称)、parent_id(父分类ID)、sort(排序)                | 存储诗词朝代、流派分类（如唐诗、宋词-豪放派） |
| tb_poet               | 诗人表                 | id(主键)、name(诗人姓名)、dynasty(朝代)、intro(简介)、avatar(头像)        | 存储诗人基本信息                        |
| tb_poem               | 诗句表                 | id(主键)、title(诗词标题)、content(原文)、paraphrase(释义)、category_id(分类ID)、poet_id(诗人ID) | 存储诗词核心内容，关联分类和诗人        |
| tb_blog               | 诗词图文笔记表         | id(主键)、user_id(用户ID)、poem_id(诗词ID)、title(笔记标题)、content(内容)、like_count(点赞数) | 用户发布的诗词赏析、读后感等内容        |
| tb_blog_comments      | 诗词笔记评论表         | id(主键)、blog_id(笔记ID)、user_id(用户ID)、content(评论内容)、parent_id(父评论ID) | 对赏析笔记的评论及回复（支持多级评论）  |
| tb_collection         | 诗集表                 | id(主键)、name(诗集名称)、cover(封面)、desc(描述)、type(类型：普通/限量)  | 存储诗集合集信息                        |
| tb_limited_collection | 限量诗集表             | id(主键)、collection_id(诗集ID)、stock(库存)、start_time(抢购开始时间)、end_time(抢购结束时间) | 限量诗集的库存和抢购时间管理            |
| tb_user               | 用户表                 | id(主键)、phone(手机号)、password(加密密码)、token(会话令牌)、status(状态) | 用户账号核心信息                        |
| tb_user_info          | 用户信息表             | id(主键)、user_id(用户ID)、nickname(昵称)、city(城市)、intro(个人介绍)、fans_count(粉丝数) | 用户详细资料                            |
| tb_collection_order   | 诗集订单表             | id(主键)、user_id(用户ID)、collection_id(诗集ID)、create_time(创建时间)、status(订单状态) | 诗集购买订单记录                        |

### 核心表关系
```
tb_poetry_category (1) ──┬── (n) tb_poem
tb_poet (1) ─────────────┘
tb_poem (1) ──────────── (n) tb_blog
tb_blog (1) ──────────── (n) tb_blog_comments
tb_collection (1) ────── (1) tb_limited_collection（仅限量类型）
tb_user (1) ──────────── (1) tb_user_info
tb_user (1) ──────────── (n) tb_blog / tb_blog_comments / tb_collection_order
```

## 主要功能

### 1. 用户模块
- 手机号验证码登录/注册（基于Redis存储验证码，有效期5分钟）
- 用户信息管理（修改昵称、城市、个人介绍等）
- 会话管理（Token有效期控制、自动登出）

### 2. 诗词模块
- 分类浏览：按朝代、流派分类查询诗词
- 详情查看：查看诗词原文、释义、创作背景及关联诗人信息
- 关键词搜索：基于Elasticsearch实现诗词标题、内容、诗人姓名的全文检索
- 诗词管理：管理员新增/编辑/删除诗词（普通用户仅浏览）

### 3. 赏析笔记模块
- 笔记发布：支持图文结合的诗词赏析、读后感发布
- 互动功能：笔记点赞、评论、回复
- 笔记管理：用户编辑/删除自己发布的笔记，查看个人笔记列表
- 热门推荐：按点赞数、评论数排序展示热门笔记

### 4. 诗集模块
- 诗集浏览：查看普通诗集详情、内容列表
- 限量抢购：限量诗集库存校验、抢购时间控制、订单生成
- 订单管理：用户查看个人诗集购买订单、订单状态跟踪

### 5. 公共功能
- 图片上传：笔记封面、诗集封面等图片上传（本地存储，支持批量上传）
- 数据同步：通过Canal监听MySQL数据变更，实时同步诗词、诗集数据到Elasticsearch
- 缓存优化：热点分类、热门笔记、用户会话等数据缓存，提升响应速度

## 接口说明

### 一、用户相关接口
| 接口路径         | 请求方法 | 功能描述               | 请求参数                          | 响应示例                                                                 | 备注                     |
|------------------|----------|------------------------|-----------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/user/code`     | GET      | 发送手机验证码         | phone: 手机号（String，必填）     | `{"code":200,"msg":"success","data":"验证码已发送"}`                      | 验证码有效期5分钟        |
| `/user/login`    | POST     | 用户登录               | body: {phone: String, code: String} | `{"code":200,"msg":"success","data":{"token":"xxx","userId":1001}}`       | 返回Token，有效期2小时   |
| `/user/logout`   | POST     | 用户登出               | 无（Header携带Token）             | `{"code":200,"msg":"登出成功","data":null}`                               | 清除Redis中的Token       |
| `/user/info`     | GET      | 获取当前用户信息       | 无（Header携带Token）             | `{"code":200,"msg":"success","data":{"nickname":"诗仙","city":"杭州","intro":"热爱古典诗词"}}` | 需登录                   |
| `/user/info`     | PUT      | 更新用户信息           | body: {nickname: String, city: String, intro: String} | `{"code":200,"msg":"更新成功","data":null}`                               | 需登录，字段可选         |
| `/user/info/{id}` | GET      | 获取指定用户详情       | id: 用户ID（路径参数）            | `{"code":200,"msg":"success","data":{"nickname":"诗仙","fansCount":120}}` | 公开接口，无需登录       |

### 二、诗词相关接口
| 接口路径                  | 请求方法 | 功能描述               | 请求参数                                  | 响应示例                                                                 | 备注                     |
|---------------------------|----------|------------------------|-------------------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/poem/{id}`              | GET      | 根据ID查询诗词详情     | id: 诗词ID（路径参数）                    | `{"code":200,"msg":"success","data":{"title":"静夜思","content":"床前明月光...","poetName":"李白"}}` | 公开接口                 |
| `/poem`                   | POST     | 新增诗词（管理员）     | body: {title: String, content: String, categoryId: Long, poetId: Long, paraphrase: String} | `{"code":200,"msg":"新增成功","data":{"id":5001}}`                        | 需管理员权限             |
| `/poem`                   | PUT      | 更新诗词（管理员）     | body: {id: Long, title: String, content: String, ...} | `{"code":200,"msg":"更新成功","data":null}`                               | 需管理员权限             |
| `/poem/of/type`           | GET      | 按分类查询诗词         | categoryId: 分类ID（必填）, current: 页码（默认1）, size: 每页条数（默认10） | `{"code":200,"msg":"success","data":{"total":100,"list":[...],"pages":10}}` | 公开接口，分页返回       |
| `/poem/of/poet`           | GET      | 按诗人查询诗词         | poetId: 诗人ID（必填）, current: 页码     | `{"code":200,"msg":"success","data":{"total":50,"list":[...]}}`           | 公开接口                 |
| `/poem/search`            | POST     | 全文搜索诗词           | body: {keyword: String, current: 页码, size: 每页条数} | `{"code":200,"msg":"success","data":{"total":30,"list":[...]}}`           | 基于Elasticsearch搜索    |

### 三、赏析笔记相关接口
| 接口路径                  | 请求方法 | 功能描述               | 请求参数                                  | 响应示例                                                                 | 备注                     |
|---------------------------|----------|------------------------|-------------------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/blog`                   | POST     | 发布赏析笔记           | body: {poemId: Long, title: String, content: String, cover: String（可选）} | `{"code":200,"msg":"发布成功","data":{"id":2001}}`                        | 需登录                   |
| `/blog/{id}`              | GET      | 查询笔记详情           | id: 笔记ID（路径参数）                    | `{"code":200,"msg":"success","data":{"title":"《静夜思》赏析","content":"...","likeCount":50}}` | 公开接口                 |
| `/blog/of/me`             | GET      | 查询我的笔记           | current: 页码（默认1）                    | `{"code":200,"msg":"success","data":{"total":5,"list":[...]}}`            | 需登录                   |
| `/blog/hot`               | GET      | 查询热门笔记           | current: 页码（默认1）                    | `{"code":200,"msg":"success","data":{"list":[...]}}`                      | 按点赞数降序排列         |
| `/blog/{id}`              | PUT      | 编辑笔记               | id: 笔记ID（路径参数）, body: {title: String, content: String} | `{"code":200,"msg":"编辑成功","data":null}`                               | 需登录，仅作者可操作     |
| `/blog/{id}`              | DELETE   | 删除笔记               | id: 笔记ID（路径参数）                    | `{"code":200,"msg":"删除成功","data":null}`                               | 需登录，仅作者可操作     |
| `/blog/like/{id}`         | PUT      | 笔记点赞/取消点赞      | id: 笔记ID（路径参数）                    | `{"code":200,"msg":"点赞成功","data":{"likeCount":51}}`                   | 需登录，重复请求取消点赞 |
| `/blog/likes/{id}`        | GET      | 查询笔记点赞用户       | id: 笔记ID（路径参数）                    | `{"code":200,"msg":"success","data":[{"userId":1001,"nickname":"诗仙"},...]}` | 公开接口                 |

### 四、评论相关接口
| 接口路径                  | 请求方法 | 功能描述               | 请求参数                                  | 响应示例                                                                 | 备注                     |
|---------------------------|----------|------------------------|-------------------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/blog-comments`          | POST     | 发布评论               | body: {blogId: Long, content: String, parentId: Long（可选，父评论ID）} | `{"code":200,"msg":"评论成功","data":{"id":3001}}`                        | 需登录                   |
| `/blog-comments/of/blog`  | GET      | 查询笔记评论列表       | blogId: 笔记ID（必填）, current: 页码     | `{"code":200,"msg":"success","data":{"total":20,"list":[...]}}`           | 公开接口，按时间降序     |
| `/blog-comments/like/{id}`| PUT      | 评论点赞/取消点赞      | id: 评论ID（路径参数）                    | `{"code":200,"msg":"点赞成功","data":null}`                               | 需登录                   |
| `/blog-comments/{id}`     | DELETE   | 删除评论               | id: 评论ID（路径参数）                    | `{"code":200,"msg":"删除成功","data":null}`                               | 需登录，仅作者可操作     |

### 五、诗集相关接口
| 接口路径                  | 请求方法 | 功能描述               | 请求参数                                  | 响应示例                                                                 | 备注                     |
|---------------------------|----------|------------------------|-------------------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/collection`             | GET      | 查询诗集列表           | type: 类型（可选：common/limited）, current: 页码 | `{"code":200,"msg":"success","data":{"total":15,"list":[...]}}`           | 公开接口                 |
| `/collection/{id}`        | GET      | 查询诗集详情           | id: 诗集ID（路径参数）                    | `{"code":200,"msg":"success","data":{"name":"李白经典诗选","cover":"xxx","poems":[...]}}` | 公开接口                 |
| `/collection/seckill/{id}`| POST     | 抢购限量诗集           | id: 诗集ID（路径参数）                    | `{"code":200,"msg":"抢购成功","data":{"orderId":4001}}`                   | 需登录，库存校验、时间校验 |
| `/collection/order`       | GET      | 查询我的诗集订单       | current: 页码（默认1）                    | `{"code":200,"msg":"success","data":{"total":3,"list":[...]}}`            | 需登录                   |
| `/collection/order/{id}`  | GET      | 查询订单详情           | id: 订单ID（路径参数）                    | `{"code":200,"msg":"success","data":{"orderId":4001,"collectionName":"...","status":"已完成"}}` | 需登录，仅本人可查询     |

### 六、上传相关接口
| 接口路径                  | 请求方法 | 功能描述               | 请求参数                                  | 响应示例                                                                 | 备注                     |
|---------------------------|----------|------------------------|-------------------------------------------|--------------------------------------------------------------------------|--------------------------|
| `/upload/blog`            | POST     | 上传笔记图片           | file: 图片文件（multipart/form-data）      | `{"code":200,"msg":"上传成功","data":{"fileName":"20251124xxx.jpg","url":"/upload/imgs/20251124xxx.jpg"}}` | 需登录，支持JPG/PNG/GIF |
| `/upload/blog/delete`     | GET      | 删除笔记图片           | name: 文件名（String，必填）              | `{"code":200,"msg":"删除成功","data":null}`                               | 需登录，仅本人上传的图片可删除 |

## 项目启动说明

### 1. 环境依赖
- JDK 1.8 或更高版本
- MySQL 8.0（需提前创建数据库 `poetry_forum`）
- Redis 6.0+（默认端口6379，无密码）
- Elasticsearch 7.x（默认端口9200，无密码）
- Maven 3.6+（构建项目）

### 2. 初始化步骤
1. 克隆项目：`git clone https://github.com/wwt01/poetryforum.git`
2. 进入项目目录：`cd poetry-forum`
3. 导入数据库脚本：
    - 执行 `src/main/resources/db/createTable.sql` 创建表结构
    - 执行 `src/main/resources/db/insertData.sql` 插入测试数据（分类、诗人、诗词等）
4. 修改配置文件：
    - 编辑 `src/main/resources/application.yaml`，配置MySQL、Redis、Elasticsearch连接信息：
      ```yaml
      spring:
        datasource:
          url: jdbc:mysql://localhost:3306/poetry_forum?useSSL=false&serverTimezone=Asia/Shanghai
          username: root
          password: your-mysql-password
        redis:
          host: localhost
          port: 6379
      elasticsearch:
        rest:
          uris: http://localhost:9200
      ```

### 3. 启动项目
- 方式1：IDE启动（如IntelliJ IDEA）
    - 导入项目为Maven项目，等待依赖下载完成
    - 找到启动类 `com.poetryforum.PoetryforumApplication`
    - 右键执行 `Run` 或 `Debug`
- 方式2：Maven命令启动
    - 打包项目：`mvn clean package -Dmaven.test.skip=true`
    - 运行JAR包：`java -jar target/poetry-forum-1.0.0.jar`

### 4. 验证启动成功
- 访问接口：`http://localhost:8085/category/list`（默认端口8085）
- 响应状态码200且返回分类列表，说明项目启动成功

## 项目目录结构

```
poetry-forum/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── poetryforum/
│   │   │           ├── PoetryforumApplication.java  # 项目启动类
│   │   │           ├── controller/                  # 控制器层（接口入口）
│   │   │           ├── service/                     # 服务层（业务逻辑）
│   │   │           │   └── impl/                    # 服务实现类
│   │   │           ├── mapper/                      # Mapper层（数据库访问）
│   │   │           ├── entity/                      # 数据库实体类（与表映射）
│   │   │           ├── dto/                         # 数据传输对象（请求/响应）
│   │   │           │   ├── request/                 # 请求DTO
│   │   │           │   └── response/                # 响应DTO
│   │   │           ├── esdao/                       # Elasticsearch数据访问层
│   │   │           ├── esentity/                    # Elasticsearch实体类
│   │   │           ├── config/                      # 配置类（Redis、ES、MyBatis等）
│   │   │           ├── utils/                       # 工具类（Result、DateUtil等）
│   │   │           ├── exception/                   # 全局异常处理
│   │   │           └── job/                         # 定时任务（数据同步等）
│   │   └── resources/
│   │       ├── application.yaml                     # 主配置文件
│   │       └── db/
│   │           ├── createTable.sql                  # 建表脚本
│   │           └── insertData.sql                   # 测试数据脚本
│   └── test/                                        # 单元测试
├── pom.xml                                          # Maven依赖配置
├── README.md                                        # 项目说明文档
└── LICENSE                                          # 开源协议
```

## 注意事项

1. 生产环境需修改 `application-prod.yaml` 配置：
    - 关闭SQL日志打印
    - 配置Redis密码、Elasticsearch认证信息
    - 修改服务器端口、项目访问路径
    - 配置图片存储路径为云存储（如阿里云OSS）

2. 数据同步说明：
    - 项目启动时会自动执行全量同步（诗词、诗集数据同步到Elasticsearch）
    - 运行中通过Canal监听MySQL数据变更，实现增量同步
    - 如需手动触发同步，可调用接口 `/sync/es/full`（需管理员权限）

3. 权限控制：
    - 普通用户仅可操作自己的笔记、评论
    - 管理员账号需手动在 `tb_user` 表中设置 `status=1`（普通用户 `status=0`）

4. 性能优化建议：
    - 生产环境开启MySQL连接池优化
    - 扩大Redis缓存容量，调整热点数据缓存时间
    - 对Elasticsearch索引进行分片和副本配置

