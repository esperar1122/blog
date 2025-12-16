<template>
  <div class="admin-layout">
    <!-- 管理员侧边栏 -->
    <aside class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <h2 class="admin-title" v-show="!sidebarCollapsed">管理面板</h2>
        <el-button
          type="text"
          @click="toggleSidebar"
          class="toggle-btn"
        >
          <el-icon>
            <Expand v-if="sidebarCollapsed" />
            <Fold v-else />
          </el-icon>
        </el-button>
      </div>

      <nav class="admin-menu">
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :unique-opened="true"
          background-color="#001529"
          text-color="#fff"
          active-text-color="#1890ff"
          router
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>仪表盘</template>
          </el-menu-item>

          <el-menu-item index="/admin/users">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/articles">
            <el-icon><Document /></el-icon>
            <template #title>文章管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/comments">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>评论管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/categories">
            <el-icon><FolderOpened /></el-icon>
            <template #title>分类管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/tags">
            <el-icon><CollectionTag /></el-icon>
            <template #title>标签管理</template>
          </el-menu-item>

          <el-menu-item index="/admin/settings">
            <el-icon><Setting /></el-icon>
            <template #title>系统设置</template>
          </el-menu-item>
        </el-menu>
      </nav>
    </aside>

    <!-- 主内容区域 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <header class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">管理面板</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle">{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :src="userAvatar" :size="32">
                {{ userInitials }}
              </el-avatar>
              <span class="username">{{ currentUser?.nickname || currentUser?.username }}</span>
              <el-icon class="dropdown-icon"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  用户设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="admin-content">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { usePermission } from '@/composables/usePermission'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Expand,
  Fold,
  DataBoard,
  User,
  Document,
  ChatDotRound,
  FolderOpened,
  CollectionTag,
  Setting,
  CaretBottom,
  SwitchButton
} from '@element-plus/icons-vue'

// 响应式数据
const sidebarCollapsed = ref(false)
const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { isAdmin } = usePermission()

// 计算属性
const currentUser = computed(() => authStore.user)
const userAvatar = computed(() => currentUser.value?.avatar || '')
const userInitials = computed(() => {
  const name = currentUser.value?.nickname || currentUser.value?.username || ''
  return name.charAt(0).toUpperCase()
})

const activeMenu = computed(() => {
  return route.path
})

const currentPageTitle = computed(() => {
  const titleMap: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/users': '用户管理',
    '/admin/articles': '文章管理',
    '/admin/comments': '评论管理',
    '/admin/categories': '分类管理',
    '/admin/tags': '标签管理',
    '/admin/settings': '系统设置'
  }
  return titleMap[route.path] || ''
})

// 方法
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm(
          '确定要退出登录吗？',
          '确认退出',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
        )
        await authStore.logout()
        ElMessage.success('退出登录成功')
        router.push('/login')
      } catch (error) {
        // 用户取消或其他错误
      }
      break
  }
}

// 生命周期
onMounted(() => {
  // 验证管理员权限
  if (!isAdmin()) {
    ElMessage.error('无权访问管理面板')
    router.push('/')
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f0f2f5;
}

.admin-sidebar {
  width: 250px;
  background-color: #001529;
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
}

.admin-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #1f2937;
}

.admin-title {
  color: #fff;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.toggle-btn {
  color: #fff;
  padding: 8px;
}

.toggle-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

.admin-menu {
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-header {
  height: 64px;
  background-color: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: #f3f4f6;
}

.username {
  font-weight: 500;
  color: #374151;
}

.dropdown-icon {
  color: #6b7280;
  transition: transform 0.2s;
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  background-color: #f0f2f5;
}

/* 动画效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }

  .admin-sidebar.collapsed {
    width: 0;
  }

  .admin-main {
    margin-left: 0;
  }

  .admin-content {
    padding: 16px;
  }

  .admin-header {
    padding: 0 16px;
  }
}

/* 滚动条样式 */
.admin-menu::-webkit-scrollbar {
  width: 6px;
}

.admin-menu::-webkit-scrollbar-track {
  background: #001529;
}

.admin-menu::-webkit-scrollbar-thumb {
  background: #1f2937;
  border-radius: 3px;
}

.admin-menu::-webkit-scrollbar-thumb:hover {
  background: #374151;
}
</style>