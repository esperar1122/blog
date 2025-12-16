<template>
  <div class="content-stats">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in articleStats" :key="stat.title">
        <el-card class="stat-card" :class="stat.type">
          <div class="stat-content">
            <div class="stat-icon">
              <component :is="stat.icon" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6" v-for="stat in commentStats" :key="stat.title">
        <el-card class="stat-card" :class="stat.type">
          <div class="stat-content">
            <div class="stat-icon">
              <component :is="stat.icon" />
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stat.value }}</div>
              <div class="stat-title">{{ stat.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>今日数据</span>
            </div>
          </template>
          <div class="today-stats">
            <div class="today-item">
              <el-icon class="today-icon"><Document /></el-icon>
              <span class="today-label">今日文章</span>
              <span class="today-value">{{ todayStats.articles }}</span>
            </div>
            <div class="today-item">
              <el-icon class="today-icon"><ChatLineSquare /></el-icon>
              <span class="today-label">今日评论</span>
              <span class="today-value">{{ todayStats.comments }}</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快速操作</span>
            </div>
          </template>
          <div class="quick-actions">
            <el-button type="primary" @click="$emit('refresh')" :loading="refreshing">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <el-button type="success" @click="$emit('export')">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button type="warning" @click="$emit('manage-sensitive')">
              <el-icon><Warning /></el-icon>
              敏感词管理
            </el-button>
            <el-button type="info" @click="$emit('system-settings')">
              <el-icon><Setting /></el-icon>
              系统设置
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document,
  ChatLineSquare,
  User,
  View,
  Star,
  Delete,
  Refresh,
  Download,
  Warning,
  Setting
} from '@element-plus/icons-vue'
import adminContentService from '@/services/adminContentService'

interface StatsData {
  totalArticles: number
  publishedArticles: number
  draftArticles: number
  totalComments: number
  activeComments: number
  deletedComments: number
  todayArticles: number
  todayComments: number
}

const props = defineProps<{
  refreshing?: boolean
}>()

const emit = defineEmits<{
  'refresh': []
  'export': []
  'manage-sensitive': []
  'system-settings': []
}>()

const refreshing = ref(false)
const stats = reactive<StatsData>({
  totalArticles: 0,
  publishedArticles: 0,
  draftArticles: 0,
  totalComments: 0,
  activeComments: 0,
  deletedComments: 0,
  todayArticles: 0,
  todayComments: 0
})

const articleStats = [
  {
    title: '文章总数',
    value: stats.totalArticles,
    icon: Document,
    type: 'primary'
  },
  {
    title: '已发布',
    value: stats.publishedArticles,
    icon: Star,
    type: 'success'
  },
  {
    title: '草稿',
    value: stats.draftArticles,
    icon: Document,
    type: 'warning'
  },
  {
    title: '总浏览量',
    value: 0,
    icon: View,
    type: 'info'
  }
]

const commentStats = [
  {
    title: '评论总数',
    value: stats.totalComments,
    icon: ChatLineSquare,
    type: 'primary'
  },
  {
    title: '正常评论',
    value: stats.activeComments,
    icon: User,
    type: 'success'
  },
  {
    title: '已删除评论',
    value: stats.deletedComments,
    icon: Delete,
    type: 'danger'
  },
  {
    title: '总点赞数',
    value: 0,
    icon: Star,
    type: 'info'
  }
]

const todayStats = {
  articles: stats.todayArticles,
  comments: stats.todayComments
}

onMounted(() => {
  loadStats()
})

const loadStats = async () => {
  try {
    refreshing.value = true
    const response = await adminContentService.getContentStats()
    const data = response.data

    Object.assign(stats, data)

    articleStats[0].value = data.totalArticles
    articleStats[1].value = data.publishedArticles
    articleStats[2].value = data.draftArticles

    commentStats[0].value = data.totalComments
    commentStats[1].value = data.activeComments
    commentStats[2].value = data.deletedComments

    todayStats.articles = data.todayArticles
    todayStats.comments = data.todayComments
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error('加载统计数据错误:', error)
  } finally {
    refreshing.value = false
  }
}

defineExpose({
  loadStats
})
</script>

<style scoped>
.content-stats {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 10px;
}

.stat-icon {
  font-size: 48px;
  margin-right: 20px;
  opacity: 0.8;
}

.stat-card.primary .stat-icon {
  color: #409eff;
}

.stat-card.success .stat-icon {
  color: #67c23a;
}

.stat-card.warning .stat-icon {
  color: #e6a23c;
}

.stat-card.danger .stat-icon {
  color: #f56c6c;
}

.stat-card.info .stat-icon {
  color: #909399;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-title {
  font-size: 14px;
  color: #606266;
}

.today-stats {
  display: flex;
  justify-content: space-around;
  padding: 20px 0;
}

.today-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.today-icon {
  font-size: 24px;
  color: #409eff;
}

.today-label {
  font-size: 14px;
  color: #606266;
}

.today-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 10px 0;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 50px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>