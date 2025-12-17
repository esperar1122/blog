<template>
  <div class="search-results">
    <!-- 搜索信息 -->
    <div v-if="searchResult" class="search-info">
      <div class="search-summary">
        <span>找到 <strong>{{ searchResult.searchMeta.resultCount }}</strong> 条结果</span>
        <span class="search-time">
          (耗时 {{ searchResult.searchMeta.toFixed(2) }}s)
        </span>
      </div>
      <div class="search-keyword">
        关键词: <el-tag type="primary">{{ searchResult.searchMeta.keyword }}</el-tag>
      </div>
    </div>

    <!-- 搜索结果列表 -->
    <div v-if="hasResults" class="results-list">
      <el-card
        v-for="item in searchResult.results"
        :key="item.id"
        class="result-item"
        shadow="hover"
      >
        <div class="item-header">
          <router-link
            :to="`/article/${item.id}`"
            class="item-title"
            v-html="item.highlights?.title?.[0] || item.title"
          />
          <div class="item-meta">
            <span class="author" v-if="item.author">
              <el-avatar :size="20" :src="item.author.avatar">
                {{ item.author.nickname?.charAt(0) }}
              </el-avatar>
              {{ item.author.nickname }}
            </span>
            <span class="time">{{ formatTime(item.createTime) }}</span>
            <span class="views">
              <el-icon><View /></el-icon>
              {{ item.viewCount }}
            </span>
          </div>
        </div>

        <div class="item-content">
          <p v-html="item.highlights?.content?.[0] || item.summary"></p>
        </div>

        <div class="item-footer">
          <el-tag
            v-if="item.category"
            type="info"
            size="small"
            class="category-tag"
          >
            {{ item.category.name }}
          </el-tag>
          <el-tag
            v-for="tag in item.tags"
            :key="tag.id"
            :color="tag.color"
            size="small"
            class="tag-item"
          >
            {{ tag.name }}
          </el-tag>
        </div>
      </el-card>

      <!-- 加载更多 -->
      <div v-if="canLoadMore" class="load-more">
        <el-button
          :loading="loading"
          @click="loadMore"
          size="large"
        >
          加载更多
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="searchResult && !loading" class="empty-state">
      <el-empty description="没有找到相关内容">
        <template #image>
          <el-icon size="80"><DocumentRemove /></el-icon>
        </template>
        <p>试试其他关键词或调整筛选条件</p>
        <el-button type="primary" @click="$emit('clear-search')">
          清空搜索
        </el-button>
      </el-empty>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && !searchResult" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { View, DocumentRemove } from '@element-plus/icons-vue'
import type { SearchResult } from 'blog-shared'

interface Props {
  searchResult: SearchResult | null
  loading?: boolean
  canLoadMore?: boolean
}

interface Emits {
  (e: 'load-more'): void
  (e: 'clear-search'): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  canLoadMore: false
})

const emit = defineEmits<Emits>()

// 计算属性
const hasResults = computed(() => {
  return props.searchResult && props.searchResult.results.length > 0
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
  const month = day * 30

  if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`
  } else if (diff < day) {
    return `${Math.floor(diff / hour)}小时前`
  } else if (diff < week) {
    return `${Math.floor(diff / day)}天前`
  } else if (diff < month) {
    return `${Math.floor(diff / week)}周前`
  } else {
    return date.toLocaleDateString()
  }
}

// 加载更多
const loadMore = () => {
  emit('load-more')
}
</script>

<style scoped>
.search-results {
  flex: 1;
  min-width: 0;
}

.search-info {
  margin-bottom: 20px;
  padding: 16px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
}

.search-summary {
  font-size: 16px;
  margin-bottom: 8px;
}

.search-time {
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.search-keyword {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item {
  transition: all 0.2s;
}

.result-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.item-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 12px;
}

.item-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  text-decoration: none;
  line-height: 1.4;
}

.item-title:hover {
  color: var(--el-color-primary);
}

.item-title :deep(mark) {
  background-color: #ffeb3b;
  color: inherit;
  font-weight: 600;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.author {
  display: flex;
  align-items: center;
  gap: 6px;
}

.views {
  display: flex;
  align-items: center;
  gap: 4px;
}

.item-content {
  margin-bottom: 12px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.item-content :deep(mark) {
  background-color: #ffeb3b;
  color: inherit;
  font-weight: 600;
}

.item-content p {
  margin: 0;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.item-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tag {
  margin-right: 8px;
}

.tag-item {
  border: none;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.loading-state {
  padding: 20px;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .item-meta {
    flex-wrap: wrap;
    gap: 8px;
  }

  .item-title {
    font-size: 16px;
  }

  .search-info {
    padding: 12px;
  }
}
</style>