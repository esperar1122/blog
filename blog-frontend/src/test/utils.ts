import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createRouter, createWebHistory } from 'vue-router'

export function createTestWrapper(component: any, options: any = {}) {
  const router = createRouter({
    history: createWebHistory(),
    routes: [
      { path: '/', component: { template: '<div></div>' } },
      { path: '/articles/:id', component: { template: '<div></div>' } },
    ],
  })

  const defaultOptions = {
    global: {
      plugins: [createPinia(), router],
      stubs: {
        'router-link': true,
        'router-view': true,
        'font-awesome-icon': true,
      },
    },
  }

  return mount(component, { ...defaultOptions, ...options })
}

export const mockArticle = {
  id: 1,
  title: '测试文章标题',
  summary: '这是一篇测试文章的摘要',
  content: '## 测试内容\n\n这是测试文章的内容。',
  coverImage: 'https://example.com/cover.jpg',
  status: 'PUBLISHED',
  viewCount: 100,
  likeCount: 20,
  commentCount: 5,
  isTop: false,
  authorId: 1,
  authorName: '测试作者',
  authorAvatar: 'https://example.com/avatar.jpg',
  categoryId: 1,
  categoryName: '测试分类',
  createTime: '2025-12-16T10:00:00+08:00',
  updateTime: '2025-12-16T10:00:00+08:00',
  publishTime: '2025-12-16T10:00:00+08:00',
  tags: [
    { id: 1, name: 'Vue', createTime: '2025-12-16T10:00:00+08:00' },
    { id: 2, name: 'TypeScript', createTime: '2025-12-16T10:00:00+08:00' },
  ],
}

export const mockAuthor = {
  id: 1,
  username: 'testuser',
  nickname: '测试作者',
  avatar: 'https://example.com/avatar.jpg',
  email: 'test@example.com',
}

export const mockCategory = {
  id: 1,
  name: '测试分类',
  description: '这是一个测试分类',
  articleCount: 10,
}