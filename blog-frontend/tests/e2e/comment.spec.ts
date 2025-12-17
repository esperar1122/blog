import { test, expect } from '@playwright/test'

test.describe('评论功能', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟登录
    await page.addInitScript(() => {
      window.localStorage.setItem('token', 'test.jwt.token')
      window.localStorage.setItem('user', JSON.stringify({
        id: 1,
        username: 'testuser',
        nickname: '测试用户',
        role: 'USER'
      }))
    })

    // 模拟 API 响应
    await page.route('**/api/v1/articles/1', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: {
            id: 1,
            title: '测试文章',
            content: '# 测试文章\n\n这是一篇测试文章的内容。',
            authorId: 1,
            author: {
              id: 1,
              username: 'author',
              nickname: '文章作者'
            },
            category: {
              id: 1,
              name: '技术'
            },
            tags: [
              { id: 1, name: 'Vue', color: '#4fc08d' },
              { id: 2, name: 'TypeScript', color: '#3178c6' }
            ],
            viewCount: 100,
            likeCount: 10,
            commentCount: 0,
            createTime: '2025-12-17T10:00:00Z',
            updateTime: '2025-12-17T10:00:00Z'
          }
        })
      })
    })

    await page.route('**/api/v1/comments*', async route => {
      const url = route.request().url()

      if (url.includes('/nested')) {
        // 返回空评论列表
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: []
          })
        })
      } else if (url.includes('/count')) {
        // 返回评论数量
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: { count: 0 }
          })
        })
      } else if (route.request().method() === 'POST') {
        // 创建评论
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              id: 1,
              content: '这是一条新评论',
              articleId: 1,
              userId: 1,
              userName: '测试用户',
              level: 1,
              likeCount: 0,
              status: 'NORMAL',
              createTime: new Date().toISOString(),
              updateTime: new Date().toISOString()
            }
          })
        })
      }
    })
  })

  test('应该显示评论区域', async ({ page }) => {
    await page.goto('/articles/1')

    // 等待页面加载
    await expect(page.locator('h1')).toContainText('测试文章')

    // 检查评论区域是否存在
    await expect(page.locator('.comment-tree')).toBeVisible()
    await expect(page.locator('text=评论 (0)')).toBeVisible()
  })

  test('应该能够发表评论', async ({ page }) => {
    await page.goto('/articles/1')

    // 等待评论表单加载
    await expect(page.locator('.comment-form')).toBeVisible()
    await expect(page.locator('textarea[placeholder*="评论"]')).toBeVisible()

    // 输入评论内容
    await page.fill('textarea[placeholder*="评论"]', '这是一条测试评论')

    // 点击发表按钮
    await page.click('button:has-text("发表评论")')

    // 验证评论是否成功发表（通过检查API调用或页面更新）
    await expect(page.locator('text=评论发表成功')).toBeVisible()
  })

  test('应该验证评论内容', async ({ page }) => {
    await page.goto('/articles/1')

    // 尝试提交空评论
    await page.click('button:has-text("发表评论")')

    // 应该显示验证错误
    await expect(page.locator('text=评论内容不能为空')).toBeVisible()
  })

  test('应该能够对评论进行点赞', async ({ page }) => {
    // 模拟已有评论的响应
    await page.route('**/api/v1/comments/nested*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              id: 1,
              content: '这是一条已有评论',
              articleId: 1,
              userId: 2,
              userName: '其他用户',
              level: 1,
              likeCount: 5,
              status: 'NORMAL',
              createTime: '2025-12-17T09:00:00Z',
              updateTime: '2025-12-17T09:00:00Z'
            }
          ]
        })
      })
    })

    await page.route('**/api/v1/comments/1/like', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({ success: true })
      })
    })

    await page.goto('/articles/1')

    // 等待评论加载
    await expect(page.locator('text=这是一条已有评论')).toBeVisible()

    // 点击点赞按钮
    await page.click('button:has-text("点赞")')

    // 验证点赞成功
    await expect(page.locator('text=点赞成功')).toBeVisible()
  })

  test('应该能够回复评论', async ({ page }) => {
    // 模拟已有评论的响应
    await page.route('**/api/v1/comments/nested*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              id: 1,
              content: '这是一条已有评论',
              articleId: 1,
              userId: 2,
              userName: '其他用户',
              level: 1,
              likeCount: 5,
              status: 'NORMAL',
              createTime: '2025-12-17T09:00:00Z',
              updateTime: '2025-12-17T09:00:00Z'
            }
          ]
        })
      })
    })

    await page.goto('/articles/1')

    // 等待评论加载
    await expect(page.locator('text=这是一条已有评论')).toBeVisible()

    // 点击回复按钮
    await page.click('button:has-text("回复")')

    // 验证回复表单出现
    await expect(page.locator('.reply-form')).toBeVisible()
    await expect(page.locator('textarea[placeholder*="回复"]')).toBeVisible()

    // 输入回复内容
    await page.fill('textarea[placeholder*="回复"]', '这是一条回复')

    // 点击回复按钮
    await page.click('button:has-text("回复")')

    // 验证回复成功
    await expect(page.locator('text=评论发表成功')).toBeVisible()
  })

  test('应该能够编辑自己的评论', async ({ page }) => {
    // 模拟已有评论的响应（用户自己的评论）
    await page.route('**/api/v1/comments/nested*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              id: 1,
              content: '这是我原来的评论',
              articleId: 1,
              userId: 1,
              userName: '测试用户',
              level: 1,
              likeCount: 0,
              status: 'NORMAL',
              createTime: '2025-12-17T09:00:00Z',
              updateTime: '2025-12-17T09:00:00Z'
            }
          ]
        })
      })
    })

    await page.route('**/api/v1/comments/1', async route => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              id: 1,
              content: '这是我更新后的评论',
              articleId: 1,
              userId: 1,
              userName: '测试用户',
              level: 1,
              likeCount: 0,
              status: 'NORMAL',
              createTime: '2025-12-17T09:00:00Z',
              updateTime: new Date().toISOString()
            }
          })
        })
      }
    })

    await page.goto('/articles/1')

    // 等待评论加载
    await expect(page.locator('text=这是我原来的评论')).toBeVisible()

    // 点击更多操作按钮
    await page.click('.comment-actions button:has-text("更多")')

    // 点击编辑选项
    await page.click('text=编辑')

    // 验证编辑表单出现
    await expect(page.locator('.edit-form')).toBeVisible()

    // 更新评论内容
    await page.fill('.edit-form textarea', '这是我更新后的评论')

    // 点击更新按钮
    await page.click('button:has-text("更新")')

    // 验证更新成功
    await expect(page.locator('text=评论发表成功')).toBeVisible()
  })

  test('应该能够删除自己的评论', async ({ page }) => {
    // 模拟已有评论的响应（用户自己的评论）
    await page.route('**/api/v1/comments/nested*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              id: 1,
              content: '这是我想要删除的评论',
              articleId: 1,
              userId: 1,
              userName: '测试用户',
              level: 1,
              likeCount: 0,
              status: 'NORMAL',
              createTime: '2025-12-17T09:00:00Z',
              updateTime: '2025-12-17T09:00:00Z'
            }
          ]
        })
      })
    })

    await page.route('**/api/v1/comments/1', async route => {
      if (route.request().method() === 'DELETE') {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true })
        })
      }
    })

    await page.goto('/articles/1')

    // 等待评论加载
    await expect(page.locator('text=这是我想要删除的评论')).toBeVisible()

    // 点击更多操作按钮
    await page.click('.comment-actions button:has-text("更多")')

    // 点击删除选项
    await page.click('text=删除')

    // 确认删除对话框
    await page.click('button:has-text("确定")')

    // 验证删除成功
    await expect(page.locator('text=评论删除成功')).toBeVisible()
  })

  test('应该能够按时间排序评论', async ({ page }) => {
    // 模拟多条评论
    await page.route('**/api/v1/comments/nested*', async route => {
      await route.fulfill({
        status: 200,
        contentType: 'application/json',
        body: JSON.stringify({
          success: true,
          data: [
            {
              id: 1,
              content: '第一条评论',
              articleId: 1,
              userId: 2,
              userName: '用户A',
              level: 1,
              likeCount: 10,
              status: 'NORMAL',
              createTime: '2025-12-17T08:00:00Z',
              updateTime: '2025-12-17T08:00:00Z'
            },
            {
              id: 2,
              content: '第二条评论',
              articleId: 1,
              userId: 3,
              userName: '用户B',
              level: 1,
              likeCount: 5,
              status: 'NORMAL',
              createTime: '2025-12-17T09:00:00Z',
              updateTime: '2025-12-17T09:00:00Z'
            }
          ]
        })
      })
    })

    await page.goto('/articles/1')

    // 等待评论加载
    await expect(page.locator('text=第一条评论')).toBeVisible()
    await expect(page.locator('text=第二条评论')).toBeVisible()

    // 点击按时间排序
    await page.click('button:has-text("最新")')

    // 验证排序选项切换
    await expect(page.locator('.comment-sort')).toBeVisible()
  })

  test('应该能够按点赞数排序评论', async ({ page }) => {
    // 模拟多条评论
    await page.route('**/api/v1/comments/nested*', async route => {
      const url = new URL(route.request().url())
      const sortBy = url.searchParams.get('sortBy')

      if (sortBy === 'likeCount') {
        // 按点赞数排序的数据
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: [
              {
                id: 1,
                content: '最多点赞的评论',
                articleId: 1,
                userId: 2,
                userName: '用户A',
                level: 1,
                likeCount: 100,
                status: 'NORMAL',
                createTime: '2025-12-17T08:00:00Z',
                updateTime: '2025-12-17T08:00:00Z'
              }
            ]
          })
        })
      } else {
        // 默认排序的数据
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({ success: true, data: [] })
        })
      }
    })

    await page.goto('/articles/1')

    // 点击按热度排序
    await page.click('button:has-text("最热")')

    // 等待排序结果
    await expect(page.locator('text=最多点赞的评论')).toBeVisible()
  })

  test('未登录用户应该看到登录提示', async ({ page }) => {
    // 不模拟登录
    await page.goto('/articles/1')

    // 尝试发表评论
    await page.fill('textarea[placeholder*="评论"]', '这是一条测试评论')
    await page.click('button:has-text("发表评论")')

    // 应该显示登录提示
    await expect(page.locator('text=请先登录后再发表评论')).toBeVisible()
  })
})