<template>
  <div class="hot-keywords">
    <el-card class="keywords-card">
      <template #header>
        <div class="card-header">
          <span>
            <el-icon><TrendCharts /></el-icon>
            热门搜索
          </span>
          <el-button
            v-if="showRefresh"
            type="text"
            size="small"
            :loading="loading"
            @click="refreshKeywords"
          >
            刷新
          </el-button>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="keywords.length === 0" class="empty-keywords">
        <el-empty description="暂无热门搜索" :image-size="60">
          <template #image>
            <el-icon><TrendCharts /></el-icon>
          </template>
        </el-empty>
      </div>

      <div v-else class="keywords-list">
        <div
          v-for="(keyword, index) in displayKeywords"
          :key="keyword.id"
          class="keyword-item"
          :class="getKeywordClass(index)"
          @click="selectKeyword(keyword.keyword)"
        >
          <span class="rank" :class="getRankClass(index)">
            {{ index + 1 }}
          </span>
          <span class="keyword">{{ keyword.keyword }}</span>
          <span class="count">{{ keyword.searchCount }}</span>
        </div>
      </div>

      <div v-if="keywords.length > maxDisplay && !showAll" class="show-more">
        <el-button type="text" @click="showAll = true">
          查看更多 {{ keywords.length - maxDisplay }} 个
        </el-button>
      </div>

      <div v-if="showAll && keywords.length > maxDisplay" class="show-less">
        <el-button type="text" @click="showAll = false">
          收起
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts } from '@element-plus/icons-vue'
import type { HotKeywords } from 'blog-shared'
import { useSearchStore } from '@/stores/searchStore'

interface Props {
  maxDisplay?: number
  showRefresh?: boolean
}

interface Emits {
  (e: 'keyword-select', keyword: string): void
}

const props = withDefaults(defineProps<Props>(), {
  maxDisplay: 10,
  showRefresh: true
})

const emit = defineEmits<Emits>()

const searchStore = useSearchStore()
const showAll = ref(false)

// 计算属性
const keywords = computed(() => searchStore.hotKeywords)
const loading = computed(() => searchStore.loading)

const displayKeywords = computed(() => {
  if (showAll.value) {
    return keywords.value
  }
  return keywords.value.slice(0, props.maxDisplay)
})

// 获取关键词样式类
const getKeywordClass = (index: number) => {
  if (index === 0) return 'top-1'
  if (index === 1) return 'top-2'
  if (index === 2) return 'top-3'
  return 'normal'
}

// 获取排名样式类
const getRankClass = (index: number) => {
  if (index < 3) return 'rank-top'
  return 'rank-normal'
}

// 选择关键词
const selectKeyword = (keyword: string) => {
  emit('keyword-select', keyword)
}

// 刷新关键词列表
const refreshKeywords = async () => {
  try {
    await searchStore.fetchHotKeywords()
    ElMessage.success('热门搜索已刷新')
  } catch (error) {
    ElMessage.error('刷新热门搜索失败')
  }
}

// 初始化
onMounted(async () => {
  if (keywords.value.length === 0) {
    try {
      await searchStore.fetchHotKeywords()
    } catch (error) {
      console.error('Failed to fetch hot keywords:', error)
    }
  }
})
</script>

<style scoped>
.hot-keywords {
  width: 100%;
}

.keywords-card {
  height: fit-content;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.loading {
  padding: 16px;
}

.empty-keywords {
  padding: 20px;
  text-align: center;
}

.keywords-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.keyword-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.keyword-item:hover {
  background-color: var(--el-fill-color-light);
  transform: translateX(2px);
}

.rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: bold;
  border-radius: 50%;
  flex-shrink: 0;
}

.rank-top {
  color: white;
}

.rank-normal {
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.top-1 .rank {
  background-color: #ff4757;
}

.top-2 .rank {
  background-color: #ff9f43;
}

.top-3 .rank {
  background-color: #ffd32c;
  color: var(--el-text-color-primary);
}

.keyword {
  flex: 1;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  background-color: var(--el-fill-color-light);
  padding: 2px 6px;
  border-radius: 10px;
}

.show-more,
.show-less {
  text-align: center;
  padding-top: 8px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 热度动画 */
.top-1 {
  animation: pulse-red 2s infinite;
}

.top-2 {
  animation: pulse-orange 2s infinite;
}

.top-3 {
  animation: pulse-yellow 2s infinite;
}

@keyframes pulse-red {
  0%, 100% {
    border-left: 3px solid #ff4757;
  }
  50% {
    border-left: 3px solid #ff6b7a;
  }
}

@keyframes pulse-orange {
  0%, 100% {
    border-left: 3px solid #ff9f43;
  }
  50% {
    border-left: 3px solid #ffb347;
  }
}

@keyframes pulse-yellow {
  0%, 100% {
    border-left: 3px solid #ffd32c;
  }
  50% {
    border-left: 3px solid #ffe066;
  }
}

@media (max-width: 768px) {
  .keyword-item {
    padding: 8px 10px;
  }

  .rank {
    width: 20px;
    height: 20px;
    font-size: 11px;
  }

  .keyword {
    font-size: 14px;
  }
}
</style>