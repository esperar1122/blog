import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: { title: '首页' }
    },
    {
      path: '/articles',
      name: 'articles',
      component: () => import('@/views/ArticlesView.vue'),
      meta: { title: '文章列表' }
    },
    {
      path: '/articles/:id',
      name: 'article-detail',
      component: () => import('@/views/ArticleDetailView.vue'),
      meta: { title: '文章详情' }
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/CategoriesView.vue'),
      meta: { title: '分类' }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { title: '登录' }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { title: '注册' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { title: '个人中心', requiresAuth: true }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: () => import('@/views/admin/UserManagement.vue'),
      meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/content',
      name: 'admin-content',
      component: () => import('@/views/admin/AdminDashboard.vue'),
      meta: { title: '内容管理', requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
      meta: { title: '页面不存在' }
    }
  ]
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title} - 博客系统`

  // 检查是否需要登录
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()

    // 如果用户信息不存在，尝试初始化
    if (!userStore.userInfo) {
      try {
        await userStore.initializeUser()
      } catch (error) {
        console.error('初始化用户信息失败:', error)
        next({ name: 'login', query: { redirect: to.fullPath } })
        return
      }
    }

    // 检查用户是否已登录
    if (!userStore.isLoggedIn) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    // 检查是否需要管理员权限
    if (to.meta.requiresAdmin && !userStore.isAdmin) {
      // 可以重定向到首页或显示无权限页面
      next({ name: 'home' })
      return
    }
  }

  next()
})

export default router