<template>
  <div class="admin-menu">
    <el-menu
      :default-active="activeMenu"
      :collapse="collapsed"
      :unique-opened="true"
      background-color="#001529"
      text-color="#fff"
      active-text-color="#1890ff"
      router
      @select="handleMenuSelect"
    >
      <!-- 动态渲染菜单项 -->
      <template v-for="item in filteredMenus" :key="item.path">
        <el-menu-item v-if="!item.children" :index="item.path">
          <el-icon v-if="item.icon">
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.name }}</template>
        </el-menu-item>

        <el-sub-menu v-else :index="item.path">
          <template #title>
            <el-icon v-if="item.icon">
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.name }}</span>
          </template>

          <el-menu-item
            v-for="child in item.children"
            :key="child.path"
            :index="child.path"
          >
            <el-icon v-if="child.icon">
              <component :is="child.icon" />
            </el-icon>
            <template #title>{{ child.name }}</template>
          </el-menu-item>
        </el-sub-menu>
      </template>
    </el-menu>

    <!-- 折叠按钮 -->
    <div class="menu-toggle" @click="toggleCollapse">
      <el-icon>
        <Expand v-if="collapsed" />
        <Fold v-else />
      </el-icon>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePermission } from '@/composables/usePermission'
import type { MenuItem } from '@/types/permission'
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
  Files,
  Monitoring,
  Bell,
  Lock,
  Key,
  Shield
} from '@element-plus/icons-vue'

interface Props {
  collapsed?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  collapsed: false
})

const emit = defineEmits<{
  'update:collapsed': [collapsed: boolean]
  'menu-select': [path: string]
}>()

const route = useRoute()
const { currentUserRole } = usePermission()

// 响应式数据
const isCollapsed = ref(props.collapsed)

// 监听props变化
watch(() => props.collapsed, (newValue) => {
  isCollapsed.value = newValue
})

// 菜单配置
const menuItems: MenuItem[] = [
  {
    path: '/admin/dashboard',
    name: '仪表盘',
    icon: DataBoard,
    roles: ['ADMIN']
  },
  {
    path: '/admin/users',
    name: '用户管理',
    icon: User,
    roles: ['ADMIN']
  },
  {
    path: '/admin/articles',
    name: '文章管理',
    icon: Document,
    roles: ['ADMIN']
  },
  {
    path: '/admin/comments',
    name: '评论管理',
    icon: ChatDotRound,
    roles: ['ADMIN']
  },
  {
    path: '/admin/content',
    name: '内容管理',
    icon: Files,
    roles: ['ADMIN'],
    children: [
      {
        path: '/admin/categories',
        name: '分类管理',
        icon: FolderOpened,
        roles: ['ADMIN']
      },
      {
        path: '/admin/tags',
        name: '标签管理',
        icon: CollectionTag,
        roles: ['ADMIN']
      }
    ]
  },
  {
    path: '/admin/system',
    name: '系统管理',
    icon: Setting,
    roles: ['ADMIN'],
    children: [
      {
        path: '/admin/system/monitoring',
        name: '系统监控',
        icon: Monitoring,
        roles: ['ADMIN']
      },
      {
        path: '/admin/system/logs',
        name: '日志管理',
        icon: Files,
        roles: ['ADMIN']
      },
      {
        path: '/admin/system/notifications',
        name: '通知管理',
        icon: Bell,
        roles: ['ADMIN']
      }
    ]
  },
  {
    path: '/admin/security',
    name: '安全管理',
    icon: Shield,
    roles: ['ADMIN'],
    children: [
      {
        path: '/admin/security/permissions',
        name: '权限管理',
        icon: Lock,
        roles: ['ADMIN']
      },
      {
        path: '/admin/security/keys',
        name: '密钥管理',
        icon: Key,
        roles: ['ADMIN']
      }
    ]
  }
]

// 计算属性
const activeMenu = computed(() => {
  return route.path
})

const filteredMenus = computed(() => {
  return menuItems.filter(menu => {
    if (!menu.roles || menu.roles.length === 0) {
      return true
    }
    return menu.roles.includes(currentUserRole.value)
  }).map(menu => ({
    ...menu,
    children: menu.children?.filter(child => {
      if (!child.roles || child.roles.length === 0) {
        return true
      }
      return child.roles.includes(currentUserRole.value)
    })
  })).filter(menu => {
    // 如果有子菜单但所有子菜单都被过滤掉了，也不显示父菜单
    return !menu.children || menu.children.length > 0
  })
})

// 方法
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
  emit('update:collapsed', isCollapsed.value)
}

const handleMenuSelect = (path: string) => {
  emit('menu-select', path)
}
</script>

<style scoped>
.admin-menu {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.admin-menu :deep(.el-menu) {
  flex: 1;
  border-right: none;
}

.menu-toggle {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #0f172a;
  color: #fff;
  cursor: pointer;
  transition: background-color 0.2s;
  border-top: 1px solid #1e293b;
}

.menu-toggle:hover {
  background-color: #1e293b;
}

.menu-toggle .el-icon {
  font-size: 18px;
}

/* 菜单图标样式 */
.admin-menu :deep(.el-menu-item .el-icon),
.admin-menu :deep(.el-sub-menu__title .el-icon) {
  margin-right: 8px;
  width: 20px;
  text-align: center;
}

/* 折叠时的样式调整 */
.admin-menu :deep(.el-menu--collapse) {
  width: 64px;
}

.admin-menu :deep(.el-menu--collapse .el-menu-item),
.admin-menu :deep(.el-menu--collapse .el-sub-menu__title) {
  text-align: center;
}

.admin-menu :deep(.el-menu--collapse .el-menu-item .el-icon),
.admin-menu :deep(.el-menu--collapse .el-sub-menu__title .el-icon) {
  margin: 0;
}

/* 子菜单样式 */
.admin-menu :deep(.el-sub-menu .el-menu-item) {
  background-color: #0f172a;
  padding-left: 60px !important;
}

.admin-menu :deep(.el-sub-menu .el-menu-item:hover) {
  background-color: #1e293b !important;
}

.admin-menu :deep(.el-sub-menu .el-menu-item.is-active) {
  background-color: #1890ff !important;
}

/* 活动菜单项样式 */
.admin-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff !important;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: #1e293b !important;
}
</style>