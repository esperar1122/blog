<template>
  <div class="article-status-manager">
    <div class="status-indicators">
      <!-- 状态徽章 -->
      <el-tag
        :type="statusTagType"
        :effect="effect"
        class="status-badge"
      >
        <el-icon class="status-icon">
          <component :is="statusIcon" />
        </el-icon>
        {{ statusText }}
      </el-tag>

      <!-- 置顶徽章 -->
      <el-tag
        v-if="article.isTop"
        type="warning"
        effect="dark"
        class="pin-badge"
      >
        <el-icon><Top /></el-icon>
        置顶
      </el-tag>

      <!-- 定时发布徽章 -->
      <el-tag
        v-if="article.scheduledPublishTime"
        type="info"
        effect="dark"
        class="scheduled-badge"
      >
        <el-icon><Timer /></el-icon>
        定时发布：{{ formatScheduledTime(article.scheduledPublishTime) }}
      </el-tag>
    </div>

    <!-- 操作按钮组 -->
    <div class="action-buttons" v-if="showActions">
      <el-dropdown
        trigger="click"
        @command="handleCommand"
        placement="bottom-end"
      >
        <el-button type="primary" :loading="loading" :disabled="loading">
          状态管理
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </el-button>

        <template #dropdown>
          <el-dropdown-menu>
            <!-- 发布相关操作 -->
            <el-dropdown-item
              v-if="canPublish"
              command="publish"
              :icon="Promotion"
            >
              <span class="dropdown-text">发布文章</span>
            </el-dropdown-item>

            <el-dropdown-item
              v-if="canUnpublish"
              command="unpublish"
              :icon="RemoveFilled"
            >
              <span class="dropdown-text">下线文章</span>
            </el-dropdown-item>

            <!-- 置顶相关操作 -->
            <el-dropdown-item
              v-if="canPin"
              command="pin"
              :icon="Top"
            >
              <span class="dropdown-text">置顶文章</span>
            </el-dropdown-item>

            <el-dropdown-item
              v-if="canUnpin"
              command="unpin"
              :icon="Remove"
            >
              <span class="dropdown-text">取消置顶</span>
            </el-dropdown-item>

            <!-- 定时发布 -->
            <el-dropdown-item
              v-if="canSchedulePublish"
              command="schedule"
              :icon="Timer"
            >
              <span class="dropdown-text">定时发布</span>
            </el-dropdown-item>

            <el-dropdown-item
              v-if="canCancelSchedule"
              command="cancel-schedule"
              :icon="Close"
            >
              <span class="dropdown-text">取消定时</span>
            </el-dropdown-item>

            <!-- 删除和恢复 -->
            <el-dropdown-item
              v-if="canDelete"
              command="delete"
              :icon="Delete"
              divided
              class="danger-item"
            >
              <span class="dropdown-text danger-text">删除文章</span>
            </el-dropdown-item>

            <el-dropdown-item
              v-if="canRestore"
              command="restore"
              :icon="RefreshLeft"
            >
              <span class="dropdown-text">恢复文章</span>
            </el-dropdown-item>

            <!-- 查看操作日志 -->
            <el-dropdown-item
              v-if="canViewLogs"
              command="logs"
              :icon="Document"
              divided
            >
              <span class="dropdown-text">查看操作日志</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <!-- 定时发布对话框 -->
    <SchedulePublishDialog
      v-model="showScheduleDialog"
      :article="article"
      @confirm="handleSchedulePublish"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, type PropType } from 'vue'
import {
  ElIcon, ElTag, ElDropdown, ElDropdownMenu, ElDropdownItem, ElButton
} from 'element-plus'
import {
  Top, Remove, Timer, Delete, RefreshLeft, Document,
  Promotion, RemoveFilled, Close, ArrowDown
} from '@element-plus/icons-vue'
import { ArticleStatus, type Article } from '@/types/article'
import { useArticleLifecycle } from '@/composables/useArticleLifecycle'
import SchedulePublishDialog from './SchedulePublishDialog.vue'

const props = defineProps({
  article: {
    type: Object as PropType<Article>,
    required: true
  },
  showActions: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits<{
  statusChanged: [article: Article]
}>()

const {
  loading,
  publishArticle,
  unpublishArticle,
  pinArticle,
  unpinArticle,
  schedulePublish,
  softDeleteArticle,
  restoreArticle
} = useArticleLifecycle()

const showScheduleDialog = ref(false)

// 计算状态相关
const statusText = computed(() => {
  switch (props.article.status) {
    case ArticleStatus.DRAFT:
      return '草稿'
    case ArticleStatus.PUBLISHED:
      return '已发布'
    case ArticleStatus.DELETED:
      return '已删除'
    default:
      return '未知'
  }
})

const statusTagType = computed(() => {
  switch (props.article.status) {
    case ArticleStatus.DRAFT:
      return 'info'
    case ArticleStatus.PUBLISHED:
      return 'success'
    case ArticleStatus.DELETED:
      return 'danger'
    default:
      return 'warning'
  }
})

const statusIcon = computed(() => {
  switch (props.article.status) {
    case ArticleStatus.DRAFT:
      return 'EditPen'
    case ArticleStatus.PUBLISHED:
      return 'View'
    case ArticleStatus.DELETED:
      return 'Delete'
    default:
      return 'QuestionFilled'
  }
})

const effect = computed(() => {
  return props.article.status === ArticleStatus.PUBLISHED ? 'dark' : 'light'
})

// 权限判断
const canPublish = computed(() => {
  return props.article.status === ArticleStatus.DRAFT && !props.article.scheduledPublishTime
})

const canUnpublish = computed(() => {
  return props.article.status === ArticleStatus.PUBLISHED
})

const canPin = computed(() => {
  return !props.article.isTop && props.article.status !== ArticleStatus.DELETED
})

const canUnpin = computed(() => {
  return props.article.isTop
})

const canSchedulePublish = computed(() => {
  return props.article.status === ArticleStatus.DRAFT && !props.article.scheduledPublishTime
})

const canCancelSchedule = computed(() => {
  return props.article.scheduledPublishTime
})

const canDelete = computed(() => {
  return props.article.status !== ArticleStatus.DELETED
})

const canRestore = computed(() => {
  return props.article.status === ArticleStatus.DELETED
})

const canViewLogs = computed(() => {
  return true // 所有文章都可以查看操作日志
})

// 格式化定时发布时间
function formatScheduledTime(time: string) {
  return new Date(time).toLocaleString()
}

// 处理下拉菜单命令
async function handleCommand(command: string) {
  try {
    switch (command) {
      case 'publish':
        await handlePublish()
        break
      case 'unpublish':
        await handleUnpublish()
        break
      case 'pin':
        await handlePin()
        break
      case 'unpin':
        await handleUnpin()
        break
      case 'schedule':
        showScheduleDialog.value = true
        break
      case 'cancel-schedule':
        await handleCancelSchedule()
        break
      case 'delete':
        await handleDelete()
        break
      case 'restore':
        await handleRestore()
        break
      case 'logs':
        handleViewLogs()
        break
    }
  } catch (error) {
    console.error('操作失败:', error)
  }
}

// 发布文章
async function handlePublish() {
  const result = await publishArticle(props.article.id)
  if (result) {
    emit('statusChanged', { ...props.article, status: 'PUBLISHED' })
  }
}

// 下线文章
async function handleUnpublish() {
  const result = await unpublishArticle(props.article.id)
  if (result) {
    emit('statusChanged', { ...props.article, status: 'DRAFT' })
  }
}

// 置顶文章
async function handlePin() {
  const result = await pinArticle(props.article.id)
  if (result) {
    emit('statusChanged', { ...props.article, isTop: true })
  }
}

// 取消置顶
async function handleUnpin() {
  const result = await unpinArticle(props.article.id)
  if (result) {
    emit('statusChanged', { ...props.article, isTop: false })
  }
}

// 定时发布
async function handleSchedulePublish(scheduledTime: string) {
  const result = await schedulePublish(props.article.id, scheduledTime)
  if (result) {
    emit('statusChanged', {
      ...props.article,
      scheduledPublishTime: scheduledTime
    })
  }
}

// 取消定时发布
async function handleCancelSchedule() {
  const result = await schedulePublish(props.article.id, new Date(0).toISOString())
  if (result) {
    emit('statusChanged', {
      ...props.article,
      scheduledPublishTime: null
    })
  }
}

// 删除文章
async function handleDelete() {
  const result = await softDeleteArticle(props.article.id)
  if (result) {
    emit('statusChanged', {
      ...props.article,
      status: 'DELETED',
      isTop: false
    })
  }
}

// 恢复文章
async function handleRestore() {
  const result = await restoreArticle(props.article.id)
  if (result) {
    emit('statusChanged', {
      ...props.article,
      status: 'DRAFT'
    })
  }
}

// 查看操作日志
function handleViewLogs() {
  // 这里可以打开操作日志对话框或跳转到日志页面
  console.log('查看操作日志')
}
</script>

<style scoped>
.article-status-manager {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-indicators {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pin-badge, .scheduled-badge {
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-icon {
  font-size: 14px;
}

.action-buttons {
  margin-left: auto;
}

.dropdown-text {
  display: flex;
  align-items: center;
  gap: 8px;
}

.danger-item:hover {
  background-color: #fef2f2;
}

.danger-text {
  color: #ef4444;
}

@media (max-width: 768px) {
  .article-status-manager {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .action-buttons {
    margin-left: 0;
  }
}
</style>