<template>
  <div class="search-history">
    <el-card class="history-card">
      <template #header>
        <div class="history-header">
          <span>搜索历史</span>
          <div class="history-actions">
            <el-button
              v-if="searchHistory.length > 0"
              type="text"
              size="small"
              @click="toggleExpand"
            >
              {{ isExpanded ? '收起' : '展开' }}
            </el-button>
            <el-button
              v-if="searchHistory.length > 0"
              type="text"
              size="small"
              @click="clearHistory"
            >
              清空
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="searchHistory.length === 0" class="empty-history">
        <el-empty description="暂无搜索历史" :image-size="60">
          <template #image>
            <el-icon><Clock /></el-icon>
          </template>
        </el-empty>
      </div>

      <div v-else class="history-list">
        <div
          v-for="item in displayHistory"
          :key="item.id"
          class="history-item"
          @click="selectHistory(item.keyword)"
        >
          <div class="item-content">
            <el-icon class="history-icon"><Clock /></el-icon>
            <span class="keyword">{{ item.keyword }}</span>
            <el-tag size="small" type="info">
              {{ item.resultCount }} 结果
            </el-tag>
          </div>
          <div class="item-meta">
            <span class="time">{{ formatTime(item.searchTime) }}</span>
            <el-button
              type="text"
              size="small"
              :icon="Delete"
              @click.stop="removeHistory(item.id)"
              class="delete-btn"
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Clock, Delete } from '@element-plus/icons-vue'
import type { SearchHistory } from 'blog-shared'
import { useSearchStore } from '@/stores/searchStore'

interface Props {
  maxDisplay?: number
}

interface Emits {
  (e: 'history-select', keyword: string): void
}

const props = withDefaults(defineProps<Props>(), {
  maxDisplay: 10
})

const emit = defineEmits<Emits>()

const searchStore = useSearchStore()
const isExpanded = ref(false)

// 计算属性
const searchHistory = computed(() => searchStore.searchHistory)
const loading = computed(() => searchStore.loading)

const displayHistory = computed(() => {
  if (isExpanded.value) {
    return searchHistory.value
  }
  return searchHistory.value.slice(0, props.maxDisplay)
})

// 格式化时间
const formatTime = (timeString: string) => {
  const date = new Date(timeString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  const minute = 60 * 1000
  const hour = minute * 60
  const day = hour * 24
  const week = day * 7

  if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else {
    return date.toLocaleDateString()
  }
}

// 选择搜索历史
const selectHistory = (keyword: string) => {
  emit('history-select', keyword)
}

// 清空搜索历史
const clearHistory = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空所有搜索历史吗？',
      '确认操作',
      {
        type: 'warning'
      }
    )

    await searchStore.clearHistory()
    ElMessage.success('搜索历史已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空搜索历史失败')
    }
  }
}

// 删除单条历史
const removeHistory = async (id: number) => {
  try {
    await searchStore.removeHistory(id)
    ElMessage.success('删除成功')
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

// 切换展开/收起
const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
}
</script>

<style scoped>
.search-history {
  width: 100%;
}

.history-card {
  height: fit-content;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.history-actions {
  display: flex;
  gap: 8px;
}

.loading {
  padding: 16px;
}

.empty-history {
  padding: 20px;
  text-align: center;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.history-item:hover {
  background-color: var(--el-fill-color-light);
}

.item-content {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.history-icon {
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
}

.keyword {
  font-weight: 500;
  color: var(--el-text-color-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

@media (max-width: 768px) {
  .history-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .item-meta {
    align-self: flex-end;
  }
}
</style>