<template>
  <div class="article-list-item" @click="handleClick">
    <div class="article-main">
      <div class="article-header">
        <h3 class="article-title">
          <el-tag v-if="article.isTop" type="danger" size="small" class="top-tag">置顶</el-tag>
          {{ article.title }}
        </h3>
        <div class="article-actions">
          <el-button type="text" size="small" @click.stop="handleEdit" v-if="showEdit">
            <el-icon><Edit /></el-icon>
          </el-button>
          <el-button type="text" size="small" @click.stop="handleDelete" v-if="showDelete">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>

      <p class="article-summary">{{ article.summary || article.content?.substring(0, 200) + '...' }}</p>

      <div class="article-footer">
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

      <div class="article-tags" v-if="article.tags && article.tags.length > 0">
        <el-tag
          v-for="tag in article.tags"
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
      </div>
    </div>

    <div class="article-cover" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { User, Calendar, Folder, View, Star, ChatDotRound, Edit, Delete } from '@element-plus/icons-vue';
import type { Article } from '@blog/shared/types';
import { formatDate } from '@/utils/date';

interface Props {
  article: Article;
  showEdit?: boolean;
  showDelete?: boolean;
}

interface Emits {
  (e: 'click', article: Article): void;
  (e: 'edit', article: Article): void;
  (e: 'delete', article: Article): void;
}

const props = withDefaults(defineProps<Props>(), {
  showEdit: false,
  showDelete: false,
});

const emit = defineEmits<Emits>();

const handleClick = () => {
  emit('click', props.article);
};

const handleEdit = () => {
  emit('edit', props.article);
};

const handleDelete = () => {
  emit('delete', props.article);
};
</script>

<style scoped>
.article-list-item {
  display: flex;
  padding: 20px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  gap: 20px;
}

.article-list-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.article-main {
  flex: 1;
  min-width: 0;
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
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-tag {
  flex-shrink: 0;
}

.article-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.article-list-item:hover .article-actions {
  opacity: 1;
}

.article-summary {
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.article-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #9ca3af;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
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

.article-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.article-cover {
  flex-shrink: 0;
  width: 200px;
  height: 120px;
  border-radius: 6px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.article-list-item:hover .article-cover img {
  transform: scale(1.05);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .article-list-item {
    flex-direction: column;
    padding: 16px;
  }

  .article-cover {
    width: 100%;
    height: 180px;
  }

  .article-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>