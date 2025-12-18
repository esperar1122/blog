# Technical Assumptions

## Repository Structure: Monorepo

采用单仓库多模块结构，前端和后端代码在同一仓库中管理，便于版本控制和团队协作。

## Service Architecture

采用单体应用架构，前后端分离部署。后端使用Spring Boot构建RESTful API，前端使用Vue3构建SPA应用。这种架构适合项目规模和团队规模，便于开发、测试和部署。

## Testing Requirements

采用完整的测试金字塔策略：
- **单元测试**: 覆盖核心业务逻辑，目标覆盖率80%以上
- **集成测试**: 测试数据库操作、API接口等
- **端到端测试**: 测试核心用户流程
- **手动测试**: 测试UI交互和用户体验

## Additional Technical Assumptions and Requests

- **数据库**: MySQL 8.0，使用MyBatis Plus作为ORM框架
- **缓存**: Redis用于会话存储和热点数据缓存
- **文件存储**: 本地文件系统存储上传的图片和附件
- **认证**: JWT令牌认证，支持令牌刷新
- **日志**: 使用SLF4J + Logback记录系统日志
- **API文档**: 使用Swagger生成API文档
- **容器化**: 使用Docker进行应用容器化，便于部署
- **版本控制**: 使用Git进行版本控制，遵循Git Flow工作流
- **构建工具**: 后端使用Maven，前端使用Vite
- **代码质量**: 使用ESLint和CheckStyle进行代码规范检查
