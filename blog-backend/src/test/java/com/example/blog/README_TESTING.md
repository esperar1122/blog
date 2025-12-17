# 评论管理功能测试指南

## 概述

本文档描述了评论管理功能的测试结构、覆盖范围和运行方式。

## 测试结构

### 后端测试

#### 单元测试
- `CommentModerationServiceTest.java` - 评论审核服务测试
- `SensitiveWordServiceTest.java` - 敏感词服务测试

#### 集成测试
- `CommentModerationControllerTest.java` - 评论审核控制器集成测试

### 前端测试

#### 组件单元测试
- `CommentModerationPanel.spec.ts` - 评论管理面板组件测试
- `SensitiveWordManager.spec.ts` - 敏感词管理组件测试

#### 端到端测试
- `comment-moderation.spec.ts` - 完整评论管理流程E2E测试

## 测试覆盖范围

### 后端测试覆盖

#### CommentModerationServiceTest
- ✅ 举报评论功能
- ✅ 举报审核功能（通过/拒绝）
- ✅ 黑名单管理（添加/移除）
- ✅ 用户黑名单状态检查
- ✅ 评论编辑权限验证（30分钟限制）
- ✅ 批量评论审核
- ✅ 评论统计功能
- ✅ 过期黑名单清理

#### SensitiveWordServiceTest
- ✅ 敏感词过滤功能
- ✅ 屏蔽词检测
- ✅ 警告词识别
- ✅ 敏感词添加/更新/删除
- ✅ 复杂正则表达式匹配
- ✅ 大小写不敏感匹配
- ✅ 多敏感词同时处理

#### CommentModerationControllerTest
- ✅ 所有API端点的HTTP请求/响应
- ✅ 权限验证（管理员权限）
- ✅ 请求参数验证
- ✅ 错误响应处理
- ✅ 分页功能
- ✅ 批量操作接口

### 前端测试覆盖

#### CommentModerationPanel.spec.ts
- ✅ 组件渲染正确性
- ✅ 评论列表加载
- ✅ 搜索和过滤功能
- ✅ 评论详情查看
- ✅ 单条评论删除
- ✅ 批量评论删除
- ✅ 分页处理
- ✅ 错误状态处理
- ✅ 用户交互反馈

#### SensitiveWordManager.spec.ts
- ✅ 敏感词列表显示
- ✅ 类型过滤功能
- ✅ 敏感词添加/编辑/删除
- ✅ 表单验证
- ✅ 过滤测试功能
- ✅ API错误处理

#### comment-moderation.spec.ts (E2E)
- ✅ 完整用户工作流程
- ✅ 页面导航和布局
- ✅ 统计数据显示
- ✅ 搜索和过滤交互
- ✅ 弹窗和对话框操作
- ✅ 批量操作流程
- ✅ 响应式设计适配
- ✅ 加载状态处理
- ✅ 错误状态处理

## 运行测试

### 后端测试

```bash
# 运行所有测试
./mvnw test

# 运行特定测试类
./mvnw test -Dtest=CommentModerationServiceTest

# 运行特定测试方法
./mvnw test -Dtest=CommentModerationServiceTest#testReportComment_Success

# 运行测试并生成覆盖率报告
./mvnw test jacoco:report
```

### 前端测试

```bash
# 运行所有单元测试
npm run test:unit

# 运行特定测试文件
npm run test:unit -- CommentModerationPanel

# 运行测试并生成覆盖率报告
npm run test:unit:coverage

# 运行E2E测试
npm run test:e2e

# 运行特定E2E测试文件
npm run test:e2e -- comment-moderation.spec.ts
```

## 测试数据

### 测试用实体
- 预定义的测试用户、评论、举报、黑名单和敏感词实体
- 模拟的API响应数据
- 边界条件和异常情况的测试数据

### Mock数据
- 使用Mockito模拟数据库操作
- 使用Vitest mocking功能模拟前端依赖
- E2E测试中使用Playwright route mocking

## 测试环境配置

### 后端测试配置
- 使用H2内存数据库进行测试
- Spring Boot测试配置
- Mockito mocking配置

### 前端测试配置
- Vitest单元测试配置
- Vue Test Utils组件测试配置
- Playwright E2E测试配置

## 持续集成

测试已配置在CI/CD流水线中：
- 后端测试在Maven构建阶段运行
- 前端测试在构建和部署阶段运行
- 测试覆盖率要求：后端>80%，前端>70%

## 测试最佳实践

1. **命名规范**: 测试方法名称应该清楚描述测试场景
2. **AAA模式**: Arrange-Act-Assert结构
3. **独立性**: 每个测试用例应该独立运行
4. **可重复性**: 测试结果应该一致且可重复
5. **边界测试**: 包含正常情况、边界情况和异常情况
6. **Mock使用**: 适当使用Mock来隔离外部依赖
7. **断言完整性**: 验证所有重要的行为和状态

## 故障排除

### 常见问题
1. **数据库连接问题**: 确保测试数据库配置正确
2. **权限问题**: 检查Spring Security测试配置
3. **异步操作**: 使用适当的等待机制处理异步操作
4. **浏览器兼容性**: E2E测试在不同浏览器中的表现

### 调试技巧
1. 使用日志输出调试信息
2. 在IDE中运行单个测试进行调试
3. 使用Playwright的trace查看器调试E2E测试
4. 检查Mock对象的行为和调用