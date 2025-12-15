# 博客系统

该博客系统由大学生团队开发，具备完整的用户注册与登录功能，实现管理员与普通用户权限分离。系统包含用户、文章、评论、分类、标签等5张数据库表，均支持CRUD操作。管理员可管理用户及业务数据，普通用户仅管理个人内容。项目使用AI辅助编程（含提交记录证明），Git仓库规范（超20次提交），并通过GitHub多人协作，采用PR与Issues进行任务管理。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2+
- MyBatis Plus
- Spring Security + JWT
- MySQL 8.0

### 前端
- Vue 3.4+
- TypeScript 5.0+
- Vite 5.0+
- Element Plus
- Pinia
- Vue Router

## 项目结构

```
blog-system/
├── blog-backend/          # Spring Boot 后端应用
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java 源码
│   │   │   └── resources/ # 配置文件
│   │   └── test/          # 测试代码
│   └── pom.xml
├── blog-frontend/         # Vue 3 前端应用
│   ├── src/
│   │   ├── components/    # Vue 组件
│   │   ├── views/         # 页面视图
│   │   ├── router/        # 路由配置
│   │   ├── stores/        # Pinia 状态管理
│   │   ├── api/           # API 请求
│   │   └── utils/         # 工具函数
│   ├── tests/             # 测试文件
│   └── package.json
├── blog-shared/           # 共享类型和工具
│   ├── src/
│   │   ├── types/         # TypeScript 类型定义
│   │   ├── constants/     # 常量定义
│   │   └── utils/         # 工具函数
│   └── package.json
├── docs/                  # 项目文档
└── package.json           # 根目录工作区配置
```

## 快速开始

### 环境要求
- Node.js 18+
- Java 17+
- Maven 3.9+
- MySQL 8.0+

### 安装依赖
```bash
npm run install:all
```

### 开发模式
```bash
npm run dev
```

### 构建项目
```bash
npm run build
```

### 运行测试
```bash
npm run test
```

## 开发指南

### 后端开发
- 进入 `blog-backend` 目录
- 使用 Maven 进行依赖管理和构建
- 遵循 RESTful API 设计规范
- 使用 MyBatis Plus 简化数据库操作

### 前端开发
- 进入 `blog-frontend` 目录
- 使用 TypeScript 进行类型安全开发
- 遵循 Vue 3 Composition API 最佳实践
- 使用 Pinia 进行状态管理

### 类型共享
- 在 `blog-shared` 中定义前后端共享的类型
- 前端项目通过 workspace 引用共享类型
- 保证前后端接口类型的一致性

## 脚本说明

- `npm run dev` - 同时启动前后端开发服务器
- `npm run build` - 构建所有项目
- `npm run test` - 运行所有测试
- `npm run lint` - 前端代码检查
- `npm run clean` - 清理所有构建文件

## License

MIT
