# Frontend Architecture

## Component Architecture

### Component Organization

```
src/
├── components/           # Reusable components
│   ├── common/          # Generic components
│   │   ├── AppHeader.vue
│   │   ├── AppFooter.vue
│   │   ├── AppSidebar.vue
│   │   ├── LoadingSpinner.vue
│   │   └── Pagination.vue
│   ├── article/         # Article-related components
│   │   ├── ArticleCard.vue
│   │   ├── ArticleList.vue
│   │   ├── ArticleDetail.vue
│   │   ├── ArticleEditor.vue
│   │   └── RelatedArticles.vue
│   ├── comment/         # Comment components
│   │   ├── CommentTree.vue
│   │   ├── CommentItem.vue
│   │   └── CommentForm.vue
│   ├── user/            # User-related components
│   │   ├── UserAvatar.vue
│   │   ├── UserProfile.vue
│   │   ├── UserCard.vue
│   │   └── UserStats.vue
│   ├── form/            # Form components
│   │   ├── LoginForm.vue
│   │   ├── RegisterForm.vue
│   │   ├── SearchForm.vue
│   │   └── CategorySelect.vue
│   └── admin/           # Admin components
│       ├── UserManagement.vue
│       ├── ArticleManagement.vue
│       ├── CategoryManagement.vue
│       └── Dashboard.vue
├── views/               # Page components
│   ├── Home.vue
│   ├── ArticleDetail.vue
│   ├── ArticleEditor.vue
│   ├── Login.vue
│   ├── Register.vue
│   ├── Profile.vue
│   ├── Search.vue
│   └── Admin/
├── composables/         # Vue 3 composables
│   ├── useAuth.ts
│   ├── useApi.ts
│   ├── useArticle.ts
│   ├── useComment.ts
│   └── usePagination.ts
├── stores/              # Pinia stores
│   ├── auth.ts
│   ├── article.ts
│   ├── comment.ts
│   └── user.ts
├── services/            # API services
│   ├── api.ts           # Axios configuration
│   ├── authService.ts
│   ├── articleService.ts
│   ├── commentService.ts
│   └── userService.ts
├── utils/               # Utility functions
│   ├── constants.ts
│   ├── helpers.ts
│   ├── validation.ts
│   └── formatters.ts
├── router/              # Vue Router configuration
│   ├── index.ts
│   └── guards.ts
└── assets/              # Static assets
    ├── styles/
    ├── images/
    └── icons/
```

### Component Template

```typescript
// src/components/article/ArticleCard.vue
<template>
  <el-card
    class="article-card"
    :class="{ 'is-top': article.isTop }"
    @click="handleClick"
  >
    <template #header v-if="showHeader">
      <div class="card-header">
        <UserAvatar :user="article.author" size="small" />
        <span class="author-name">{{ article.author.nickname }}</span>
        <span class="publish-time">{{ formatTime(article.publishTime) }}</span>
      </div>
    </template>

    <div class="article-cover" v-if="article.coverImage">
      <el-image
        :src="article.coverImage"
        :alt="article.title"
        fit="cover"
      />
    </div>

    <div class="article-content">
      <h3 class="article-title">{{ article.title }}</h3>
      <p class="article-summary" v-if="article.summary">
        {{ article.summary }}
      </p>

      <div class="article-meta">
        <el-tag v-if="article.category" size="small">
          {{ article.category.name }}
        </el-tag>
        <el-tag
          v-for="tag in article.tags"
          :key="tag.id"
          size="small"
          :color="tag.color"
        >
          {{ tag.name }}
        </el-tag>
      </div>

      <div class="article-stats">
        <span><el-icon><View /></el-icon> {{ article.viewCount }}</span>
        <span><el-icon><ChatDotRound /></el-icon> {{ article.commentCount }}</span>
        <span><el-icon><Star /></el-icon> {{ article.likeCount }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { Article } from '@/types/models'
import UserAvatar from '@/components/user/UserAvatar.vue'
import { formatTime } from '@/utils/formatters'

interface Props {
  article: Article
  showHeader?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  showHeader: true
})

const emit = defineEmits<{
  click: [article: Article]
}>()

const handleClick = () => {
  emit('click', props.article)
}
</script>

<style scoped lang="scss">
.article-card {
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &.is-top {
    border: 2px solid var(--el-color-primary);
  }
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;

  .author-name {
    font-weight: 500;
    flex: 1;
  }

  .publish-time {
    color: var(--el-text-color-secondary);
    font-size: 12px;
  }
}

.article-cover {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 16px;

  .el-image {
    width: 100%;
    height: 100%;
  }
}

.article-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--el-text-color-primary);
}

.article-summary {
  margin: 0 0 16px 0;
  color: var(--el-text-color-regular);
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  margin-bottom: 16px;

  .el-tag {
    margin-right: 8px;
    margin-bottom: 4px;
  }
}

.article-stats {
  display: flex;
  gap: 16px;
  color: var(--el-text-color-secondary);
  font-size: 13px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}
</style>
```

## State Management Architecture

### State Structure

```typescript
// src/stores/auth.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types/models'
import { authService } from '@/services/authService'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const accessToken = ref<string>('')
  const refreshToken = ref<string>('')

  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  const login = async (credentials: LoginCredentials) => {
    try {
      const response = await authService.login(credentials)
      user.value = response.user
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken

      // Store tokens in localStorage
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)

      return response
    } catch (error) {
      throw error
    }
  }

  const logout = () => {
    user.value = null
    accessToken.value = ''
    refreshToken.value = ''
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  const refreshAccessToken = async () => {
    try {
      const response = await authService.refreshToken(refreshToken.value)
      accessToken.value = response.accessToken
      refreshToken.value = response.refreshToken
      localStorage.setItem('accessToken', response.accessToken)
      localStorage.setItem('refreshToken', response.refreshToken)
    } catch (error) {
      logout()
      throw error
    }
  }

  const initializeAuth = () => {
    const storedAccessToken = localStorage.getItem('accessToken')
    const storedRefreshToken = localStorage.getItem('refreshToken')

    if (storedAccessToken && storedRefreshToken) {
      accessToken.value = storedAccessToken
      refreshToken.value = storedRefreshToken
      // TODO: Validate token and fetch user info
    }
  }

  return {
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    isAdmin,
    login,
    logout,
    refreshAccessToken,
    initializeAuth
  }
})
```

### State Management Patterns

- **Store-per-feature:** Each major feature (auth, articles, comments) has its own Pinia store
- **Composition API style:** Using the setup syntax for better TypeScript inference and code organization
- **Persistent state:** Authentication state persisted in localStorage with automatic initialization
- **Computed properties:** Derived state (isAuthenticated, isAdmin) computed from reactive state
- **Actions for async operations:** All API calls encapsulated in store actions with proper error handling

## Routing Architecture

### Route Organization

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: {
      title: '首页'
    }
  },
  {
    path: '/articles/:id',
    name: 'ArticleDetail',
    component: () => import('@/views/ArticleDetail.vue'),
    props: true,
    meta: {
      title: '文章详情'
    }
  },
  {
    path: '/editor',
    name: 'ArticleEditor',
    component: () => import('@/views/ArticleEditor.vue'),
    meta: {
      title: '写文章',
      requiresAuth: true
    }
  },
  {
    path: '/editor/:id',
    name: 'ArticleEdit',
    component: () => import('@/views/ArticleEditor.vue'),
    props: true,
    meta: {
      title: '编辑文章',
      requiresAuth: true
    }
  },
  {
    path: '/search',
    name: 'Search',
    component: () => import('@/views/Search.vue'),
    meta: {
      title: '搜索'
    }
  },
  {
    path: '/category/:id',
    name: 'Category',
    component: () => import('@/views/Category.vue'),
    props: true,
    meta: {
      title: '分类'
    }
  },
  {
    path: '/tag/:id',
    name: 'Tag',
    component: () => import('@/views/Tag.vue'),
    props: true,
    meta: {
      title: '标签'
    }
  },
  {
    path: '/auth',
    component: () => import('@/layouts/AuthLayout.vue'),
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('@/views/Login.vue'),
        meta: {
          title: '登录',
          requiresGuest: true
        }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('@/views/Register.vue'),
        meta: {
          title: '注册',
          requiresGuest: true
        }
      }
    ]
  },
  {
    path: '/profile',
    component: () => import('@/layouts/UserLayout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: {
          title: '个人中心'
        }
      },
      {
        path: 'articles',
        name: 'MyArticles',
        component: () => import('@/views/MyArticles.vue'),
        meta: {
          title: '我的文章'
        }
      },
      {
        path: 'comments',
        name: 'MyComments',
        component: () => import('@/views/MyComments.vue'),
        meta: {
          title: '我的评论'
        }
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: {
          title: '管理后台'
        }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: {
          title: '用户管理'
        }
      },
      {
        path: 'articles',
        name: 'ArticleManagement',
        component: () => import('@/views/admin/ArticleManagement.vue'),
        meta: {
          title: '文章管理'
        }
      },
      {
        path: 'categories',
        name: 'CategoryManagement',
        component: () => import('@/views/admin/CategoryManagement.vue'),
        meta: {
          title: '分类管理'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: {
      title: '页面未找到'
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

export default router
```

### Protected Route Pattern

```typescript
// src/router/guards.ts
import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

export function setupRouterGuards(router: Router) {
  router.beforeEach(async (to, from, next) => {
    const authStore = useAuthStore()

    // Initialize auth if not already done
    if (!authStore.isAuthenticated) {
      authStore.initializeAuth()
    }

    // Update document title
    document.title = to.meta.title
      ? `${to.meta.title} - 大学生博客系统`
      : '大学生博客系统'

    // Check route requirements
    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
      ElMessage.warning('请先登录')
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    if (to.meta.requiresGuest && authStore.isAuthenticated) {
      next({ name: 'Home' })
      return
    }

    if (to.meta.requiresAdmin && !authStore.isAdmin) {
      ElMessage.error('需要管理员权限')
      next({ name: 'Home' })
      return
    }

    next()
  })

  router.onError((error) => {
    console.error('Router error:', error)
    ElMessage.error('页面加载失败')
  })
}
```

## Frontend Services Layer

### API Client Setup

```typescript
// src/services/api.ts
import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type AxiosResponse
} from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

class ApiClient {
  private instance: AxiosInstance

  constructor() {
    this.instance = axios.create({
      baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    })

    this.setupInterceptors()
  }

  private setupInterceptors() {
    // Request interceptor
    this.instance.interceptors.request.use(
      (config) => {
        const authStore = useAuthStore()
        if (authStore.accessToken) {
          config.headers.Authorization = `Bearer ${authStore.accessToken}`
        }
        return config
      },
      (error) => {
        return Promise.reject(error)
      }
    )

    // Response interceptor
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        return response
      },
      async (error) => {
        const authStore = useAuthStore()

        if (error.response?.status === 401) {
          // Token expired, try to refresh
          try {
            await authStore.refreshAccessToken()
            // Retry the original request
            return this.instance.request(error.config)
          } catch (refreshError) {
            // Refresh failed, logout and redirect
            authStore.logout()
            router.push({ name: 'Login' })
            ElMessage.error('登录已过期，请重新登录')
          }
        }

        // Handle other errors
        const message = error.response?.data?.message || '请求失败'
        ElMessage.error(message)

        return Promise.reject(error)
      }
    )
  }

  public get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, config)
  }

  public post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, config)
  }

  public put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, config)
  }

  public delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config)
  }

  public upload<T = any>(url: string, formData: FormData, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, formData, {
      ...config,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}

export const apiClient = new ApiClient()
```

### Service Example

```typescript
// src/services/articleService.ts
import { apiClient } from './api'
import type { Article, CreateArticleRequest, UpdateArticleRequest } from '@/types/models'

export interface ArticleQuery {
  page?: number
  size?: number
  categoryId?: number
  tagId?: number
  keyword?: string
  status?: 'DRAFT' | 'PUBLISHED'
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const articleService = {
  async getArticles(query: ArticleQuery = {}): Promise<PageResult<Article>> {
    const response = await apiClient.get<PageResult<Article>>('/articles', { params: query })
    return response.data
  },

  async getArticle(id: number): Promise<Article> {
    const response = await apiClient.get<Article>(`/articles/${id}`)
    return response.data
  },

  async createArticle(article: CreateArticleRequest): Promise<Article> {
    const response = await apiClient.post<Article>('/articles', article)
    return response.data
  },

  async updateArticle(id: number, article: UpdateArticleRequest): Promise<Article> {
    const response = await apiClient.put<Article>(`/articles/${id}`, article)
    return response.data
  },

  async deleteArticle(id: number): Promise<void> {
    await apiClient.delete(`/articles/${id}`)
  },

  async publishArticle(id: number): Promise<void> {
    await apiClient.put(`/articles/${id}/publish`)
  },

  async likeArticle(id: number): Promise<void> {
    await apiClient.post(`/articles/${id}/like`)
  },

  async unlikeArticle(id: number): Promise<void> {
    await apiClient.delete(`/articles/${id}/like`)
  },

  async uploadImage(file: File): Promise<string> {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', 'article')

    const response = await apiClient.upload<{ url: string }>('/upload', formData)
    return response.data.url
  }
}
```
