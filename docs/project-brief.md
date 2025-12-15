# Project Brief: 大学生个人博客系统

## Executive Summary

本项目旨在开发一个基于Spring Boot + MySQL + Vue3技术栈的个人博客系统，为大学生提供一个完整的博客发布和管理平台。系统将实现用户注册登录、权限管理、文章管理等核心功能，采用前后端分离架构，确保良好的用户体验和系统可维护性。

## Problem Statement

当前大学生在分享学习心得、生活感悟和技术成长过程中缺乏一个专属的、易于使用的个人博客平台。现有的通用博客平台要么功能过于复杂，要么缺乏个性化定制能力。学生们需要一个简单、高效、支持富文本编辑的个人博客系统，能够：

1. 方便地发布和管理个人文章
2. 与同学和老师分享学习成果
3. 建立个人技术品牌和学术影响力
4. 提供评论互动功能促进学术交流
5. 支持多媒体内容展示

## Proposed Solution

我们将开发一个功能完整的个人博客系统，采用现代化的技术栈和架构设计。系统将提供直观的用户界面、强大的内容管理功能和完善的后台管理系统。通过前后端分离的架构，确保系统的可扩展性和维护性。系统将支持富文本编辑、图片上传、分类管理、标签系统等博客核心功能，同时确保数据安全和用户隐私保护。

## Target Users

### Primary User Segment: 大学生博主
- **用户画像**: 18-25岁在校大学生，包括本科生和研究生
- **技术能力**: 具备基本的计算机操作能力，对网络应用熟悉
- **需求特征**:
  - 需要记录学习过程和心得体会
  - 希望建立个人学术/技术品牌
  - 需要与同学、老师、潜在雇主分享作品
  - 追求简洁易用的界面和良好的用户体验
- **使用场景**:
  - 发布课程作业和项目经验
  - 分享实习经历和求职心得
  - 记录技术学习笔记
  - 展示个人项目和研究成果

### Secondary User Segment: 课程教师/导师
- **用户画像**: 大学教师、研究生导师
- **需求特征**:
  - 关注学生的学习进展和成果
  - 需要评价和指导学生的学术写作
  - 希望发现优秀学生作品
- **使用场景**:
  - 查看学生博客了解学习情况
  - 对学生文章进行评论指导
  - 推荐优秀文章给其他学生

## Goals & Success Metrics

### Business Objectives
- **目标1**: 在3个月内完成系统开发和部署，服务至少100名学生用户
- **目标2**: 实现用户活跃度达到60%以上（周活跃用户/总用户）
- **目标3**: 确保系统稳定性达到99%以上，响应时间小于2秒
- **目标4**: 通过用户反馈持续优化，用户满意度达到4.5/5分以上

### User Success Metrics
- **文章发布效率**: 用户平均发布一篇文章的时间小于10分钟
- **系统易用性**: 新用户首次成功发布文章的时间小于30分钟
- **功能使用率**: 80%以上的用户使用超过5个核心功能
- **用户留存率**: 1个月用户留存率达到70%以上

### Key Performance Indicators (KPIs)
- **用户注册数**: 每月新增用户数达到20-30人
- **文章发布量**: 每周平均发布文章50篇以上
- **用户互动量**: 每篇文章平均获得2-3条评论
- **系统性能**: 页面加载时间小于3秒，API响应时间小于500ms

## MVP Scope

### Core Features (Must Have)
- **用户管理系统**: 完整的用户注册、登录、个人资料管理功能
- **权限控制系统**: 管理员和普通用户角色分离，实现不同权限控制
- **文章管理系统**: 文章的创建、编辑、删除、查看功能，支持富文本编辑
- **分类标签系统**: 文章分类和标签管理，便于内容组织和检索
- **评论系统**: 用户可以对文章进行评论和回复
- **搜索功能**: 支持按标题、内容、作者、标签进行文章搜索
- **后台管理**: 管理员可以管理用户、文章、评论等数据

### Out of Scope for MVP
- 移动端APP（仅响应式Web设计）
- 社交媒体分享功能
- 邮件订阅通知功能
- 多语言支持
- 高级数据分析统计
- 第三方登录集成
- 文章定时发布功能

### MVP Success Criteria
系统成功部署上线，100名学生能够正常注册使用，每月产生100篇以上文章内容，用户反馈满意度达到4分以上，系统稳定运行无重大bug。

## Post-MVP Vision

### Phase 2 Features
- 移动端适配优化和PWA支持
- 社交媒体分享和集成功能
- 邮件通知系统
- 文章收藏和点赞功能
- 用户关注和粉丝系统
- 多媒体内容支持（视频、音频）
- SEO优化功能

### Long-term Vision
发展成为一个完整的校园内容生态平台，整合学习资源分享、学术交流、职业发展等功能，为大学生提供全方位的个人品牌建设服务。

### Expansion Opportunities
- 扩展到其他高校和校园
- 与学校教务系统集成
- 提供付费高级功能
- 建立内容推荐算法
- 支持多人协作写作
- 开发API供第三方应用集成

## Technical Considerations

### Platform Requirements
- **目标平台**: Web应用，支持Chrome、Firefox、Safari、Edge等主流浏览器
- **响应式设计**: 支持PC、平板、手机等不同设备访问
- **性能要求**: 页面加载时间小于3秒，支持并发100用户访问
- **兼容性**: 支持HTML5、CSS3、ES6+等现代Web标准

### Technology Preferences
- **前端**: Vue3 + Vue Router + Pinia + Element Plus UI框架
- **后端**: Spring Boot 3.x + Spring Security + MyBatis Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **文件存储**: 本地文件系统或阿里云OSS
- **构建工具**: Maven + Vite
- **版本控制**: Git

### Architecture Considerations
- **架构模式**: 前后端分离架构，RESTful API设计
- **项目结构**: 标准Maven多模块项目结构
- **服务架构**: 单体应用架构，便于开发和部署
- **集成需求**: 暂无外部系统集成需求
- **安全要求**: JWT认证，密码加密存储，SQL注入防护，XSS攻击防护

## Constraints & Assumptions

### Constraints
- **预算**: 开发阶段无商业预算，使用开源技术和免费云服务
- **时间线**: 3个月内完成MVP版本开发和测试
- **资源**: 3-5人开发团队，包括前端、后端、测试人员
- **技术**: 必须使用指定的Spring Boot + MySQL + Vue3技术栈

### Key Assumptions
- 用户具备基本的计算机操作能力
- 开发团队熟悉所选技术栈
- 服务器环境稳定可靠
- 用户接受现代化的Web界面设计
- 用户需求相对标准化，不需要大量定制化功能

## Risks & Open Questions

### Key Risks
- **技术风险**: 开发团队对技术栈熟悉度不足可能影响开发进度
- **需求变更**: 开发过程中用户需求可能发生变化
- **性能风险**: 大量用户同时访问时系统性能可能不足
- **安全风险**: Web应用可能面临各种网络攻击威胁

### Open Questions
- 系统部署采用云服务器还是本地服务器？
- 是否需要支持文章草稿功能？
- 评论是否需要审核机制？
- 如何处理敏感内容和不当言论？
- 是否需要数据备份和恢复机制？

### Areas Needing Further Research
- 竞品分析研究现有博客系统的功能特点
- 用户调研深入了解大学生具体需求
- 技术选型进一步验证和优化
- 部署方案和成本预算详细分析
- 数据安全和隐私保护法规要求

## Appendices

### A. Database Schema Design

#### 1. 用户表 (t_user)
```sql
CREATE TABLE t_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    bio TEXT COMMENT '个人简介',
    role ENUM('ADMIN', 'USER') DEFAULT 'USER' COMMENT '用户角色',
    status ENUM('ACTIVE', 'BANNED') DEFAULT 'ACTIVE' COMMENT '用户状态',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 2. 文章表 (t_article)
```sql
CREATE TABLE t_article (
    article_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    content LONGTEXT NOT NULL COMMENT '文章内容（Markdown格式）',
    summary VARCHAR(500) COMMENT '文章摘要',
    cover_image VARCHAR(255) COMMENT '封面图片URL',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    category_id BIGINT COMMENT '分类ID',
    status ENUM('DRAFT', 'PUBLISHED', 'DELETED') DEFAULT 'DRAFT' COMMENT '文章状态',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id) REFERENCES t_user(user_id),
    FOREIGN KEY (category_id) REFERENCES t_category(category_id)
);
```

#### 3. 分类表 (t_category)
```sql
CREATE TABLE t_category (
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(200) COMMENT '分类描述',
    icon VARCHAR(100) COMMENT '分类图标',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### 4. 标签表 (t_tag)
```sql
CREATE TABLE t_tag (
    tag_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) UNIQUE NOT NULL COMMENT '标签名称',
    color VARCHAR(10) DEFAULT '#1890ff' COMMENT '标签颜色',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

#### 5. 评论表 (t_comment)
```sql
CREATE TABLE t_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL COMMENT '文章ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论ID（0表示一级评论）',
    content TEXT NOT NULL COMMENT '评论内容',
    status ENUM('NORMAL', 'DELETED') DEFAULT 'NORMAL' COMMENT '评论状态',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES t_article(article_id),
    FOREIGN KEY (user_id) REFERENCES t_user(user_id)
);
```

#### 6. 文章标签关联表 (t_article_tag)
```sql
CREATE TABLE t_article_tag (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    article_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_article_tag (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES t_article(article_id),
    FOREIGN KEY (tag_id) REFERENCES t_tag(tag_id)
);
```

### B. Core API Endpoints

#### 用户管理 API
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/user/profile` - 获取用户信息
- `PUT /api/user/profile` - 更新用户信息
- `GET /api/admin/users` - 管理员获取用户列表
- `PUT /api/admin/users/{id}` - 管理员更新用户
- `DELETE /api/admin/users/{id}` - 管理员删除用户

#### 文章管理 API
- `GET /api/articles` - 获取文章列表
- `GET /api/articles/{id}` - 获取文章详情
- `POST /api/articles` - 创建文章
- `PUT /api/articles/{id}` - 更新文章
- `DELETE /api/articles/{id}` - 删除文章
- `GET /api/admin/articles` - 管理员获取所有文章
- `PUT /api/admin/articles/{id}/status` - 管理员更新文章状态

#### 分类管理 API
- `GET /api/categories` - 获取分类列表
- `POST /api/admin/categories` - 创建分类
- `PUT /api/admin/categories/{id}` - 更新分类
- `DELETE /api/admin/categories/{id}` - 删除分类

#### 标签管理 API
- `GET /api/tags` - 获取标签列表
- `POST /api/admin/tags` - 创建标签
- `PUT /api/admin/tags/{id}` - 更新标签
- `DELETE /api/admin/tags/{id}` - 删除标签

#### 评论管理 API
- `GET /api/articles/{id}/comments` - 获取文章评论
- `POST /api/articles/{id}/comments` - 创建评论
- `PUT /api/comments/{id}` - 更新评论
- `DELETE /api/comments/{id}` - 删除评论
- `GET /api/admin/comments` - 管理员获取所有评论

### C. Project Structure

#### 后端项目结构
```
blog-backend/
├── src/main/java/com/blog/
│   ├── controller/          # 控制器层
│   │   ├── auth/           # 认证相关
│   │   ├── user/           # 用户管理
│   │   ├── article/        # 文章管理
│   │   ├── category/       # 分类管理
│   │   ├── tag/           # 标签管理
│   │   └── comment/       # 评论管理
│   ├── service/           # 服务层
│   │   ├── impl/          # 服务实现
│   │   └── ...            # 服务接口
│   ├── mapper/            # 数据访问层
│   ├── entity/            # 实体类
│   ├── dto/               # 数据传输对象
│   ├── config/            # 配置类
│   ├── utils/             # 工具类
│   └── BlogApplication.java
├── src/main/resources/
│   ├── mapper/            # MyBatis映射文件
│   ├── application.yml    # 应用配置
│   └── static/           # 静态资源
└── pom.xml
```

#### 前端项目结构
```
blog-frontend/
├── src/
│   ├── api/              # API接口
│   ├── components/       # 公共组件
│   ├── views/           # 页面组件
│   │   ├── auth/        # 认证页面
│   │   ├── article/     # 文章页面
│   │   ├── user/        # 用户页面
│   │   └── admin/       # 管理页面
│   ├── router/          # 路由配置
│   ├── store/           # 状态管理
│   ├── utils/           # 工具函数
│   ├── styles/          # 样式文件
│   └── App.vue
├── public/              # 公共资源
├── package.json
└── vite.config.js
```

## Next Steps

### Immediate Actions
1. 环境搭建：配置开发环境，包括IDE、数据库、Node.js等
2. 项目初始化：创建前后端项目，配置基础依赖
3. 数据库设计：根据设计方案创建数据库表结构
4. 基础架构搭建：实现前后端基础框架和配置
5. 用户认证模块：实现用户注册、登录、JWT认证功能
6. 文章管理模块：实现文章的CRUD操作
7. 前端页面开发：实现用户界面和交互功能
8. 系统测试：进行功能测试、性能测试和安全测试
9. 部署上线：将系统部署到服务器并配置域名
10. 用户培训：编写用户手册，进行用户培训

### PM Handoff

This Project Brief provides the full context for 大学生个人博客系统. Please start in 'PRD Generation Mode', review the brief thoroughly to work with the user to create the PRD section by section as the template indicates, asking for any necessary clarification or suggesting improvements.

---
*文档创建时间: 2025-12-15*
*技术栈: Spring Boot 3.x + MySQL 8.0 + Vue3*