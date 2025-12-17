# Blog Backend 项目结构

## 📁 项目目录结构

```
blog-backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/blog/
│   │   │   ├── BlogApplication.java          # 主启动类
│   │   │   ├── config/                       # 配置类
│   │   │   │   ├── CorsConfig.java           # CORS跨域配置
│   │   │   │   └── MyBatisPlusConfig.java    # MyBatis Plus配置
│   │   │   ├── controller/                   # 控制器层
│   │   │   │   ├── AuthController.java       # 认证控制器
│   │   │   │   ├── ArticleController.java    # 文章控制器
│   │   │   │   ├── CategoryController.java   # 分类控制器
│   │   │   │   └── HealthController.java     # 健康检查控制器
│   │   │   ├── dto/                          # 数据传输对象
│   │   │   │   ├── CreateArticleRequest.java # 创建文章请求
│   │   │   │   ├── UpdateArticleRequest.java # 更新文章请求
│   │   │   │   ├── CreateCategoryRequest.java # 创建分类请求
│   │   │   │   ├── UpdateCategoryRequest.java # 更新分类请求
│   │   │   │   ├── CategoryDTO.java          # 分类数据传输对象
│   │   │   │   ├── LoginRequest.java         # 登录请求
│   │   │   │   └── RegisterRequest.java      # 注册请求
│   │   │   ├── entity/                       # 实体类
│   │   │   │   ├── User.java                 # 用户实体
│   │   │   │   ├── Article.java              # 文章实体
│   │   │   │   ├── Category.java             # 分类实体
│   │   │   │   ├── Tag.java                  # 标签实体
│   │   │   │   ├── Comment.java              # 评论实体
│   │   │   │   ├── ArticleLike.java          # 文章点赞实体
│   │   │   │   ├── Notification.java         # 通知实体
│   │   │   │   └── ArticleTag.java           # 文章标签关联实体
│   │   │   ├── exception/                    # 异常处理
│   │   │   │   ├── BusinessException.java    # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   │   │   ├── mapper/                       # 数据访问层接口
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── ArticleMapper.java
│   │   │   │   ├── CategoryMapper.java
│   │   │   │   ├── TagMapper.java
│   │   │   │   ├── CommentMapper.java
│   │   │   │   ├── ArticleLikeMapper.java
│   │   │   │   └── NotificationMapper.java
│   │   │   ├── service/                      # 服务层接口
│   │   │   │   ├── UserService.java
│   │   │   │   ├── ArticleService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   └── NotificationService.java
│   │   │   ├── service/impl/                 # 服务层实现
│   │   │   │   ├── UserServiceImpl.java
│   │   │   │   ├── ArticleServiceImpl.java
│   │   │   │   ├── CategoryServiceImpl.java
│   │   │   │   └── NotificationServiceImpl.java
│   │   │   ├── common/                       # 通用类
│   │   │   │   └── Result.java               # 统一响应结果
│   │   │   └── util/                         # 工具类
│   │   │       └── JwtUtil.java              # JWT工具类
│   │   └── resources/
│   │       ├── application.yml               # 主配置文件
│   │       ├── application-dev.yml           # 开发环境配置
│   │       ├── mapper/
│   │       │   └── CategoryMapper.xml        # MyBatis映射文件
│   │       └── db/migration/                 # 数据库迁移脚本
│   │           ├── V1__Create_initial_tables.sql
│   │           └── V2__Create_category_triggers.sql
│   └── test/                                 # 测试代码
├── pom.xml                                   # Maven配置文件
├── .env                                      # 环境变量配置
├── ENVIRONMENT_SETUP.md                      # 环境配置指南
├── PROJECT_STRUCTURE.md                      # 项目结构说明
├── install_java17.bat                        # Java 17安装脚本
└── startup.bat                               # 启动脚本
```

## 🏗️ 架构说明

### 分层架构
- **Controller层**: 处理HTTP请求，参数验证，调用Service层
- **Service层**: 业务逻辑处理，事务管理
- **Mapper层**: 数据访问，使用MyBatis Plus
- **Entity层**: 数据库实体映射
- **DTO层**: 数据传输对象，用于API交互

### 技术栈
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.4.1
- **安全**: Spring Security + JWT
- **构建工具**: Maven
- **Java版本**: 17

### 主要功能模块

#### 1. 用户管理 (User Management)
- 用户注册/登录
- JWT认证
- 用户信息管理
- 角色权限控制 (USER/ADMIN)

#### 2. 文章管理 (Article Management)
- 文章CRUD操作
- 文章状态管理 (DRAFT/PUBLISHED/DELETED)
- 文章分类关联
- 文章标签管理
- 文章点赞功能

#### 3. 分类管理 (Category Management)
- 树形分类结构
- 分类CRUD操作
- 自动统计文章数量
- 分类排序

#### 4. 评论系统 (Comment System)
- 多级评论支持
- 评论点赞
- 评论状态管理

#### 5. 通知系统 (Notification System)
- 系统通知
- 用户交互通知

## 🔧 配置说明

### 数据库配置
- 主机: localhost:3306
- 数据库: blog_db
- 字符集: utf8mb4

### JWT配置
- 密钥: 配置在 application.yml
- 过期时间: 24小时

### API路径
- 基础路径: `/api`
- 健康检查: `/api/health`
- 认证相关: `/api/auth/*`
- 文章相关: `/api/articles/*`
- 分类相关: `/api/categories/*`

## 🚀 快速开始

1. **安装Java 17**
   ```cmd
   install_java17.bat
   ```

2. **启动应用**
   ```cmd
   startup.bat
   ```

3. **访问应用**
   - API文档: http://localhost:8080/api/health
   - 数据库: MySQL localhost:3306/blog_db

## 📝 开发规范

### API响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 异常处理
- 使用 `BusinessException` 抛出业务异常
- `GlobalExceptionHandler` 统一处理异常

### 数据库规范
- 表名前缀: `t_`
- 字段命名: 下划线分隔
- 主键: `BIGINT AUTO_INCREMENT`
- 软删除: `deleted` 字段

### 代码规范
- 使用Lombok简化代码
- 统一使用Result类返回响应
- Service层事务控制
- 参数验证使用Bean Validation