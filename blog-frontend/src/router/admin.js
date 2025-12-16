import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        {
          path: 'dashboard',
          name: 'AdminDashboard',
          component: () => import('@/views/admin/AdminDashboard.vue'),
          meta: { title: '管理员仪表板', icon: 'Odometer' }
        },
        {
          path: 'monitor',
          name: 'SystemMonitor',
          redirect: '/admin/monitor/overview',
          meta: { title: '系统监控', icon: 'Monitor' },
          children: [
            {
              path: 'overview',
              name: 'MonitorOverview',
              component: () => import('@/views/admin/monitor/SystemMonitor.vue'),
              meta: { title: '系统概览' }
            },
            {
              path: 'metrics',
              name: 'SystemMetrics',
              component: () => import('@/views/admin/monitor/SystemMetrics.vue'),
              meta: { title: '详细指标' }
            },
            {
              path: 'database',
              name: 'DatabaseStatus',
              component: () => import('@/views/admin/monitor/DatabaseStatus.vue'),
              meta: { title: '数据库状态' }
            }
          ]
        },
        {
          path: 'logs',
          name: 'LogManagement',
          component: () => import('@/views/admin/monitor/LogManagement.vue'),
          meta: { title: '日志管理', icon: 'Document' }
        },
        {
          path: 'backup',
          name: 'BackupManagement',
          component: () => import('@/views/admin/monitor/BackupManagement.vue'),
          meta: { title: '备份管理', icon: 'FolderOpened' }
        },
        {
          path: 'settings',
          name: 'SystemSettings',
          component: () => import('@/views/admin/monitor/SystemSettings.vue'),
          meta: { title: '系统设置', icon: 'Setting' }
        }
      ]
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = `${to.meta.title} - 博客管理系统`
  }

  // 检查认证
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  // 检查管理员权限
  if (to.meta.requiresAdmin && token) {
    try {
      const user = JSON.parse(localStorage.getItem('user') || '{}')
      if (user.role !== 'ADMIN') {
        next('/403')
        return
      }
    } catch (error) {
      next('/login')
      return
    }
  }

  next()
})

export default router