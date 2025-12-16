<template>
  <div class="user-stats">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon total">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.totalUsers || 0 }}</div>
              <div class="stats-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon active">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.activeUsers || 0 }}</div>
              <div class="stats-label">活跃用户</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon banned">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.bannedUsers || 0 }}</div>
              <div class="stats-label">禁用用户</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon new">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.todayRegistrations || 0 }}</div>
              <div class="stats-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon admin">
              <el-icon><Avatar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.adminUsers || 0 }}</div>
              <div class="stats-label">管理员</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon normal">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.normalUsers || 0 }}</div>
              <div class="stats-label">普通用户</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon week">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-number">{{ stats.weekRegistrations || 0 }}</div>
              <div class="stats-label">本周新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { User, UserFilled, Plus, Avatar, Calendar } from '@element-plus/icons-vue'
import adminService from '@/services/adminService'
import type { UserStats } from '@/services/adminService'

const stats = ref<UserStats>({
  totalUsers: 0,
  activeUsers: 0,
  bannedUsers: 0,
  inactiveUsers: 0,
  adminUsers: 0,
  normalUsers: 0,
  todayRegistrations: 0,
  weekRegistrations: 0,
  monthRegistrations: 0
})

const loadStats = async () => {
  try {
    const data = await adminService.getUserStats()
    stats.value = data
  } catch (error) {
    console.error('加载用户统计失败:', error)
  }
}

onMounted(() => {
  loadStats()
})

defineExpose({
  loadStats
})
</script>

<style scoped>
.user-stats {
  margin-bottom: 20px;
}

.stats-card {
  height: 100px;
}

.stats-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 24px;
  color: white;
}

.stats-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-icon.active {
  background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
}

.stats-icon.banned {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
}

.stats-icon.new {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.admin {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stats-icon.normal {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stats-icon.week {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stats-info {
  flex: 1;
}

.stats-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stats-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}
</style>