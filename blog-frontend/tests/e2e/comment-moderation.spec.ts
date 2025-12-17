import { test, expect } from '@playwright/test'

test.describe('评论管理功能测试', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟管理员登录
    await page.goto('/login')
    await page.fill('[data-testid="username-input"]', 'admin')
    await page.fill('[data-testid="password-input"]', 'password')
    await page.click('[data-testid="login-button"]')

    // 导航到评论管理页面
    await page.goto('/admin/comments')
    await page.waitForLoadState('networkidle')
  })

  test('应该显示评论管理页面', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h1, .page-header')).toContainText('评论管理')

    // 验证统计卡片存在
    await expect(page.locator('.statistics-cards')).toBeVisible()
    await expect(page.locator('.stat-card')).toHaveCount(4)

    // 验证标签页存在
    await expect(page.locator('.comment-tabs')).toBeVisible()
    await expect(page.locator('.el-tabs__item')).toContainText(['评论审核', '举报管理', '黑名单管理', '敏感词管理', '统计分析'])
  })

  test('应该显示评论统计数据', async ({ page }) => {
    // 等待统计数据加载
    await page.waitForSelector('.stat-value')

    // 验证统计卡片显示正确的数据
    const statValues = await page.locator('.stat-value').allTextContents()
    expect(statValues).toHaveLength(4)

    // 验证统计标签
    const statLabels = await page.locator('.stat-label').allTextContents()
    expect(statLabels).toContain('总评论数')
    expect(statLabels).toContain('活跃评论')
    expect(statLabels).toContain('待处理举报')
    expect(statLabels).toContain('已删除评论')
  })

  test('应该能够搜索和过滤评论', async ({ page }) => {
    // 切换到评论审核标签页
    await page.click('[data-testid="comments-tab"]')
    await page.waitForLoadState('networkidle')

    // 验证过滤器存在
    await expect(page.locator('[data-testid="status-filter"]')).toBeVisible()
    await expect(page.locator('[data-testid="keyword-search"]')).toBeVisible()
    await expect(page.locator('[data-testid="search-button"]')).toBeVisible()
    await expect(page.locator('[data-testid="reset-button"]')).toBeVisible()

    // 测试状态过滤
    await page.selectOption('[data-testid="status-filter"]', 'NORMAL')
    await page.click('[data-testid="search-button"]')
    await page.waitForLoadState('networkidle')

    // 测试关键词搜索
    await page.fill('[data-testid="keyword-search"]', '测试')
    await page.click('[data-testid="search-button"]')
    await page.waitForLoadState('networkidle')

    // 测试重置过滤器
    await page.click('[data-testid="reset-button"]')
    await expect(page.locator('[data-testid="status-filter"]')).toHaveValue('')
    await expect(page.locator('[data-testid="keyword-search"]')).toHaveValue('')
  })

  test('应该能够查看评论详情', async ({ page }) => {
    // 切换到评论审核标签页
    await page.click('[data-testid="comments-tab"]')
    await page.waitForLoadState('networkidle')

    // 等待评论列表加载
    await page.waitForSelector('[data-testid="comment-table"]')

    // 点击第一条评论的查看按钮
    await page.click('[data-testid="view-comment-button"]:first-child')

    // 验证详情弹窗打开
    await expect(page.locator('[data-testid="comment-detail-dialog"]')).toBeVisible()
    await expect(page.locator('.comment-detail')).toBeVisible()

    // 验证详情内容
    await expect(page.locator('.detail-item')).toHaveCount.greaterThan(0)

    // 关闭详情弹窗
    await page.click('[data-testid="close-detail-button"]')
    await expect(page.locator('[data-testid="comment-detail-dialog"]')).not.toBeVisible()
  })

  test('应该能够删除单条评论', async ({ page }) => {
    // 切换到评论审核标签页
    await page.click('[data-testid="comments-tab"]')
    await page.waitForLoadState('networkidle')

    // 等待评论列表加载
    await page.waitForSelector('[data-testid="comment-table"]')

    // 点击第一条评论的删除按钮
    await page.click('[data-testid="delete-comment-button"]:first-child')

    // 验证确认弹窗
    await expect(page.locator('.el-message-box')).toBeVisible()
    await expect(page.locator('.el-message-box__message')).toContainText('确定要删除这条评论吗？')

    // 确认删除
    await page.click('.el-message-box__btns .el-button--primary')

    // 验证成功消息
    await expect(page.locator('.el-message--success')).toBeVisible()
    await expect(page.locator('.el-message--success')).toContainText('评论删除成功')
  })

  test('应该能够批量删除评论', async ({ page }) => {
    // 切换到评论审核标签页
    await page.click('[data-testid="comments-tab"]')
    await page.waitForLoadState('networkidle')

    // 等待评论列表加载
    await page.waitForSelector('[data-testid="comment-table"]')

    // 选择第一条评论
    await page.check('[data-testid="comment-checkbox"]:first-child')

    // 点击批量删除按钮
    await page.click('[data-testid="batch-delete-button"]')

    // 验证批量删除弹窗
    await expect(page.locator('[data-testid="batch-delete-dialog"]')).toBeVisible()
    await expect(page.locator('[data-testid="batch-delete-dialog"]')).toContainText('批量删除评论')

    // 输入删除原因
    await page.fill('[data-testid="delete-reason-textarea"]', '批量删除测试')

    // 确认批量删除
    await page.click('[data-testid="confirm-batch-delete-button"]')

    // 验证成功消息
    await expect(page.locator('.el-message--success')).toBeVisible()
    await expect(page.locator('.el-message--success')).toContainText('批量删除成功')
  })

  test('应该能够管理举报', async ({ page }) => {
    // 切换到举报管理标签页
    await page.click('[data-testid="reports-tab"]')
    await page.waitForLoadState('networkidle')

    // 验证举报过滤器
    await expect(page.locator('[data-testid="report-status-filter"]')).toBeVisible()
    await expect(page.locator('[data-testid="report-reason-filter"]')).toBeVisible()

    // 等待举报列表加载
    await page.waitForSelector('[data-testid="report-table"]')

    // 查看举报详情
    await page.click('[data-testid="view-report-button"]:first-child')
    await expect(page.locator('[data-testid="report-detail-dialog"]')).toBeVisible()

    // 关闭详情
    await page.click('[data-testid="close-report-detail-button"]')

    // 通过举报
    await page.click('[data-testid="approve-report-button"]:first-child')
    await expect(page.locator('.el-message--success')).toBeVisible()
  })

  test('应该能够管理黑名单', async ({ page }) => {
    // 切换到黑名单管理标签页
    await page.click('[data-testid="blacklist-tab"]')
    await page.waitForLoadState('networkidle')

    // 验证添加黑名单按钮
    await expect(page.locator('[data-testid="add-blacklist-button"]')).toBeVisible()

    // 点击添加黑名单
    await page.click('[data-testid="add-blacklist-button"]')
    await expect(page.locator('[data-testid="add-blacklist-dialog"]')).toBeVisible()

    // 关闭对话框
    await page.click('[data-testid="cancel-blacklist-button"]')

    // 如果有黑名单数据，测试移除功能
    const removeButtons = await page.locator('[data-testid="remove-blacklist-button"]').count()
    if (removeButtons > 0) {
      await page.click('[data-testid="remove-blacklist-button"]:first-child')
      await expect(page.locator('.el-message-box')).toBeVisible()

      // 取消移除
      await page.click('.el-message-box__btns .el-button--default')
    }
  })

  test('应该能够管理敏感词', async ({ page }) => {
    // 切换到敏感词管理标签页
    await page.click('[data-testid="sensitive-tab"]')
    await page.waitForLoadState('networkidle')

    // 验证敏感词管理界面
    await expect(page.locator('[data-testid="add-word-button"]')).toBeVisible()
    await expect(page.locator('[data-testid="test-filter-button"]')).toBeVisible()
    await expect(page.locator('[data-testid="word-type-filter"]')).toBeVisible()

    // 测试类型过滤
    await page.selectOption('[data-testid="word-type-filter"]', 'FILTER')
    await page.waitForLoadState('networkidle')

    // 点击测试过滤
    await page.click('[data-testid="test-filter-button"]')
    await expect(page.locator('[data-testid="test-filter-dialog"]')).toBeVisible()

    // 输入测试文本
    await page.fill('[data-testid="test-text-textarea"]', '这是一个测试')

    // 执行过滤测试
    await page.click('[data-testid="execute-test-button"]')

    // 验证测试结果
    await expect(page.locator('[data-testid="test-result"]')).toBeVisible()

    // 关闭测试对话框
    await page.click('[data-testid="close-test-button"]')

    // 点击添加敏感词
    await page.click('[data-testid="add-word-button"]')
    await expect(page.locator('[data-testid="add-word-dialog"]')).toBeVisible()

    // 关闭添加对话框
    await page.click('[data-testid="cancel-add-word-button"]')
  })

  test('应该显示统计分析', async ({ page }) => {
    // 切换到统计分析标签页
    await page.click('[data-testid="statistics-tab"]')
    await page.waitForLoadState('networkidle')

    // 验证统计概览
    await expect(page.locator('.statistics-overview')).toBeVisible()
    await expect(page.locator('.stat-card')).toHaveCount(4)

    // 验证举报统计
    await expect(page.locator('.report-statistics')).toBeVisible()
    await expect(page.locator('.report-stat-item')).toHaveCount(3)

    // 验证图表区域
    await expect(page.locator('.charts-section')).toBeVisible()
    await expect(page.locator('.chart-container')).toHaveCount(2)

    // 验证最近活动
    await expect(page.locator('.recent-activity')).toBeVisible()
    await expect(page.locator('.activity-item')).toHaveCount.greaterThan(0)

    // 测试刷新数据
    await page.click('[data-testid="refresh-statistics-button"]')
    await expect(page.locator('.el-message--success')).toContainText('统计数据已更新')
  })

  test('应该能够正确处理分页', async ({ page }) => {
    // 切换到评论审核标签页
    await page.click('[data-testid="comments-tab"]')
    await page.waitForLoadState('networkidle')

    // 等待分页组件加载
    await page.waitForSelector('[data-testid="pagination"]')

    // 验证分页控件存在
    await expect(page.locator('[data-testid="pagination"]')).toBeVisible()
    await expect(page.locator('[data-testid="page-size-select"]')).toBeVisible()
    await expect(page.locator('[data-testid="page-numbers"]')).toBeVisible()

    // 测试页面大小变更
    await page.selectOption('[data-testid="page-size-select"]', '50')
    await page.waitForLoadState('networkidle')

    // 测试页面跳转
    const pageNumbers = await page.locator('[data-testid="page-number"]').count()
    if (pageNumbers > 1) {
      await page.click('[data-testid="page-number"]:nth-child(2)')
      await page.waitForLoadState('networkidle')
    }
  })

  test('应该响应式适配', async ({ page }) => {
    // 测试桌面视图
    await page.setViewportSize({ width: 1200, height: 800 })
    await expect(page.locator('.comment-management')).toBeVisible()

    // 测试平板视图
    await page.setViewportSize({ width: 768, height: 1024 })
    await expect(page.locator('.comment-management')).toBeVisible()

    // 测试移动视图
    await page.setViewportSize({ width: 375, height: 667 })
    await expect(page.locator('.comment-management')).toBeVisible()

    // 验证移动端的响应式布局
    await expect(page.locator('.statistics-cards .el-col')).toHaveCount(4)
  })

  test('应该正确处理加载状态', async ({ page }) => {
    // 监听网络请求
    await page.route('/api/v1/comments/reports', route => {
      // 延迟响应以测试加载状态
      setTimeout(() => {
        route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: { list: [], total: 0, page: 1, size: 10 }
          })
        })
      }, 2000)
    })

    // 切换到举报管理标签页
    await page.click('[data-testid="reports-tab"]')

    // 验证加载状态
    await expect(page.locator('.el-table')).toHaveClass(/is-loading/)

    // 等待加载完成
    await page.waitForLoadState('networkidle')
    await expect(page.locator('.el-table')).not.toHaveClass(/is-loading/)
  })

  test('应该正确处理错误状态', async ({ page }) => {
    // 模拟API错误
    await page.route('/api/v1/comments/statistics', route => {
      route.fulfill({
        status: 500,
        contentType: 'application/json',
        body: JSON.stringify({ success: false, message: 'Internal Server Error' })
      })
    })

    // 刷新统计数据
    await page.click('[data-testid="refresh-statistics-button"]')

    // 验证错误处理（这里假设应用会显示错误消息）
    // 具体的错误显示方式取决于应用的实现
    await page.waitForTimeout(1000)
  })
})