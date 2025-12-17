<template>
  <el-card
    class="article-card"
    :body-style="{ padding: '20px' }"
    @click="handleClick"
  >
    <div class="article-header">
      <h3 class="article-title">{{ article.title }}</h3>
      <el-tag v-if="article.isTop" type="danger" size="small">置顶</el-tag>
    </div>

    <p class="article-summary">{{ article.summary || article.content?.substring(0, 150) + '...' }}</p>

    <div class="article-meta">
      <span class="meta-item">
        <el-icon><User /></el-icon>
        {{ article.author?.nickname || '匿名' }}
      </span>
      <span class="meta-item">
        <el-icon><Calendar /></el-icon>
        {{ formatDate(article.createdAt) }}
      </span>
      <span class="meta-item" v-if="article.category">
        <el-icon><Folder /></el-icon>
        {{ article.category.name }}
      </span>
    </div>

    <div class="article-footer">
      <div class="article-tags" v-if="article.tags && article.tags.length > 0">
        <el-tag
          v-for="tag in article.tags.slice(0, 3)"
          :key="tag.id"
          size="small"
          :style="{
            backgroundColor: tag.color + '20',
            borderColor: tag.color,
            color: tag.color
          }"
        >
          {{ tag.name }}
        </el-tag>
        <span v-if="article.tags.length > 3" class="more-tags">
          +{{ article.tags.length - 3 }}
        </span>
      </div>

      <div class="article-stats">
        <span class="stat-item">
          <el-icon><View /></el-icon>
          {{ article.viewCount || 0 }}
        </span>
        <span class="stat-item">
          <el-icon><Star /></el-icon>
          {{ article.likeCount || 0 }}
        </span>
        <span class="stat-item">
          <el-icon><ChatDotRound /></el-icon>
          {{ article.commentCount || 0 }}
        </span>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { User, Calendar, Folder, View, Star, ChatDotRound } from '@element-plus/icons-vue';
import type { Article } from '@blog/shared/types';
import { formatDate } from '@/utils/date';

interface Props {
  article: Article;
}

interface Emits {
  (e: 'click', article: Article): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const handleClick = () => {
  emit('click', props.article);
};
</script>

<style scoped>
.article-card {
  cursor: pointer;
  transition: all 0.3s ease;
  height: 100%;
}

.article-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.article-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  margin-right: 12px;
  line-height: 1.4;
}

.article-summary {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
  color: #9ca3af;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}

.article-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}

.more-tags {
  color: #9ca3af;
  font-size: 12px;
}

.article-stats {
  display: flex;
  gap: 12px;
  color: #9ca3af;
  font-size: 13px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .article-meta {
    flex-wrap: wrap;
    gap: 12px;
  }

  .article-footer {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>