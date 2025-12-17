<template>
  <el-config-provider :locale="locale">
    <div id="app">
      <!-- 导航栏 -->
      <el-header class="app-header">
        <div class="header-container">
          <div class="logo" @click="$router.push('/')">
            <h1>博客系统</h1>
          </div>

          <el-menu
            :default-active="$route.path"
            mode="horizontal"
            :router="true"
            class="nav-menu"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/articles">文章</el-menu-item>
            <el-menu-item index="/categories">分类</el-menu-item>
            <el-menu-item index="/tags">标签</el-menu-item>
          </el-menu>

          <div class="header-actions">
            <template v-if="!isLoggedIn">
              <el-button @click="$router.push('/login')">登录</el-button>
              <el-button type="primary" @click="$router.push('/register')">注册</el-button>
            </template>
            <template v-else>
              <el-dropdown @command="handleCommand">
                <span class="user-info">
                  <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                    {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                  </el-avatar>
                  <span class="username">{{ userStore.userInfo?.nickname }}</span>
                  <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                    <el-dropdown-item v-if="userStore.isAdmin" command="admin">
                      管理后台
                    </el-dropdown-item>
                    <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </div>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <!-- 页脚 -->
      <el-footer class="app-footer">
        <p>&copy; 2025 博客系统. All rights reserved.</p>
      </el-footer>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import zhCn from 'element-plus/es/locale/lang/zh-cn';

const router = useRouter();
const userStore = useUserStore();
const locale = ref(zhCn);

const isLoggedIn = computed(() => userStore.isLoggedIn);
const isAdmin = computed(() => userStore.isAdmin);

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile');
      break;
    case 'admin':
      router.push('/admin');
      break;
    case 'logout':
      await userStore.doLogout();
      ElMessage.success('退出登录成功');
      router.push('/');
      break;
  }
};
</script>

<style scoped>
#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  padding: 0;
  height: 60px;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo {
  cursor: pointer;
}

.logo h1 {
  margin: 0;
  font-size: 24px;
  color: #1890ff;
}

.nav-menu {
  border: none;
  background: transparent;
  flex: 1;
  margin: 0 40px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  color: #303133;
}

.app-main {
  flex: 1;
  padding: 0;
  background: #f5f7fa;
}

.app-footer {
  background: #fff;
  border-top: 1px solid #e4e7ed;
  text-align: center;
  color: #909399;
  padding: 20px;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .header-container {
    padding: 0 16px;
  }

  .nav-menu {
    margin: 0 20px;
  }

  .username {
    display: none;
  }

  .logo h1 {
    font-size: 20px;
  }
}
</style>