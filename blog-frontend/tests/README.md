# 前端测试文档

本文档描述了前端测试的配置、运行方法和编写规范。

## 测试框架

- **测试框架**: Vitest
- **测试工具库**: Vue Test Utils
- **测试环境**: jsdom
- **覆盖率工具**: v8

## 测试配置

测试配置文件位于 `vitest.config.ts`，包含：
- 测试环境配置
- 路径别名设置
- 覆盖率配置

## 运行测试

### 命令行运行

```bash
# 运行所有测试
npm run test

# 运行测试并生成覆盖率报告
npm run test:coverage

# 运行测试UI界面
npm run test:ui

# 监听模式运行测试
npm run test -- --watch
```

### 运行特定测试

```bash
# 运行特定测试文件
npm run test ArticleList.test.ts

# 运行特定目录下的测试
npm run test components/
```

## 测试结构

```
tests/
├── unit/                     # 单元测试
│   ├── components/          # 组件测试
│   │   ├── article/         # 文章相关组件测试
│   │   └── common/          # 通用组件测试
│   └── composables/         # Composables测试
├── integration/             # 集成测试
├── e2e/                     # 端到端测试
└── utils.ts                 # 测试工具函数
```

## 已实现的测试用例

### 组件测试

1. **ArticleList.vue** - 文章列表组件测试
   - 加载状态渲染
   - 空状态渲染
   - 网格/列表视图切换
   - 排序功能
   - 分页功能
   - 筛选功能

2. **ArticleDetail.vue** - 文章详情组件测试
   - 文章信息渲染
   - 作者信息显示
   - 点赞功能
   - 分享功能
   - 编辑功能
   - 权限控制

3. **ArticleCard.vue** - 文章卡片组件测试
   - 基本信息渲染
   - 封面图显示
   - 置顶标记
   - 统计信息
   - 点击事件

4. **SearchBox.vue** - 搜索框组件测试
   - 搜索输入
   - 防抖搜索
   - 搜索建议
   - 搜索历史
   - 热门搜索

5. **Pagination.vue** - 分页组件测试
   - 页码显示
   - 上一页/下一页
   - 首页/末页
   - 页码跳转
   - 每页数量变更

6. **ShareButtons.vue** - 分享按钮组件测试
   - 各平台分享
   - 复制链接
   - 二维码显示
   - 分享统计

### Composables测试

1. **useArticle** - 文章相关逻辑测试
   - 获取文章详情
   - 点赞功能
   - 分享功能
   - 缓存机制
   - 权限检查

2. **useArticleList** - 文章列表逻辑测试
   - 获取文章列表
   - 分页处理
   - 筛选功能
   - 排序功能
   - 搜索功能

## 编写测试用例的规范

### 基本原则

1. **测试命名**: 使用 `应该 + 期望结果` 的格式
2. **测试隔离**: 每个测试应该独立运行
3. **Mock依赖**: 使用mock隔离外部依赖
4. **断言清晰**: 使用有意义的断言消息

### 测试结构

```typescript
describe('ComponentName.vue', () => {
  beforeEach(() => {
    // 每个测试前的设置
  })

  it('应该正确渲染基本状态', async () => {
    // Arrange - 准备测试数据
    const props = { /* ... */ }

    // Act - 执行操作
    const wrapper = createTestWrapper(ComponentName, { props })
    await nextTick()

    // Assert - 验证结果
    expect(wrapper.text()).toContain('期望的文本')
  })
})
```

### 最佳实践

1. **使用测试工具函数**: `createTestWrapper` 包装了通用的测试设置
2. **Mock外部依赖**: API、路由、存储等应该被mock
3. **测试用户交互**: 模拟用户的点击、输入等操作
4. **测试边界情况**: 空数据、错误状态、加载状态等
5. **保持测试简洁**: 每个测试只验证一个功能点

## 测试覆盖率目标

- **总体覆盖率**: > 85%
- **组件测试**: > 90%
- **Composables测试**: > 95%
- **关键业务逻辑**: 100%

## 持续集成

测试在以下情况下自动运行：
- 每次提交代码时
- 创建Pull Request时
- 合并到主分支时

覆盖率报告会自动生成并保存在 `coverage/` 目录下。

## 常见问题

### 1. 测试环境配置问题

确保 `vitest.config.ts` 配置正确，包括路径别名和环境设置。

### 2. 组件依赖问题

使用 `stubs` 选项来mock子组件：

```typescript
const wrapper = mount(Component, {
  global: {
    stubs: {
      'router-link': true,
      'font-awesome-icon': true,
    },
  },
})
```

### 3. 异步操作测试

使用 `await nextTick()` 等待Vue的响应式更新：

```typescript
await wrapper.setData({ someData: newValue })
await nextTick()
expect(wrapper.text()).toContain('更新后的内容')
```

### 4. Timer相关测试

使用 `vi.useFakeTimers()` 来mock定时器：

```typescript
vi.useFakeTimers()
// 执行操作
vi.advanceTimersByTime(300)
vi.useRealTimers()
```

## 贡献指南

1. 新功能应该包含对应的测试用例
2. 保持测试覆盖率不低于目标值
3. 遵循现有的测试命名和结构规范
4. 为复杂的业务逻辑编写集成测试

## 相关文档

- [Vitest官方文档](https://vitest.dev/)
- [Vue Test Utils官方文档](https://test-utils.vuejs.org/)
- [项目编码规范](../../docs/architecture/coding-standards.md)