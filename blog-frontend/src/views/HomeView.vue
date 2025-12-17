<template>
  <div class="home-view">
    <el-row :gutter="20">
      <!-- 主要内容区 -->
      <el-col :xs="24" :lg="16">
        <!-- 标签云 -->
        <el-card class="mb-4">
          <template #header>
            <h2>标签云</h2>
          </template>
          <TagCloud
            title=""
            :limit="30"
            :show-more="true"
            @tag-click="handleTagClick"
          />
        </el-card>

        <!-- 最新文章 -->
        <el-card>
          <template #header>
            <div class="flex justify-between items-center">
              <h2>最新文章</h2>
              <el-button type="primary" link @click="$router.push('/articles')">
                查看更多
              </el-button>
            </div>
          </template>
          <div class="articles-grid">
            <ArticleCard
              v-for="article in latestArticles"
              :key="article.id"
              :article="article"
              @click="handleArticleClick"
            />
          </div>
          <el-empty v-if="!loading && latestArticles.length === 0" description="暂无文章" />
          <el-skeleton v-if="loading" :rows="3" animated />
        </el-card>
      </el-col>

      <!-- 侧边栏 -->
      <el-col :xs="24" :lg="8">
        <!-- 热门标签 -->
        <el-card class="mb-4">
          <template #header>
            <h2>热门标签</h2>
          </template>
          <div class="popular-tags">
            <div
              v-for="(tag, index) in popularTags"
              :key="tag.id"
              class="popular-tag-item"
              @click="handleTagClick(tag)"
            >
              <span class="tag-rank">{{ index + 1 }}</span>
              <span
                class="tag-color-dot"
                :style="{ backgroundColor: tag.color }"
              ></span>
              <span class="tag-name">{{ tag.name }}</span>
              <span class="tag-count">{{ tag.articleCount }}</span>
            </div>
          </div>
        </el-card>

        <!-- 热门文章 -->
        <el-card>
          <template #header>
            <h2>热门文章</h2>
          </template>
          <div class="popular-articles">
            <div
              v-for="(article, index) in popularArticles"
              :key="article.id"
              class="popular-article-item"
              @click="handleArticleClick(article)"
            >
              <span class="article-rank">{{ index + 1 }}</span>
              <div class="article-info">
                <h4 class="article-title">{{ article.title }}</h4>
                <p class="article-meta">
                  {{ formatDate(article.createdAt) }} · {{ article.viewCount }} 阅读
                </p>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { Tag, Article } from '@blog/shared/types';
import { getPopularTags } from '@/api/tag';
import { getLatestArticles, getPopularArticles } from '@/api/article';
import TagCloud from '@/components/tag/TagCloud.vue';
import ArticleCard from '@/components/article/ArticleCard.vue';
import { formatDate } from '@/utils/date';

const router = useRouter();
const loading = ref(false);
const popularTags = ref<Tag[]>([]);
const latestArticles = ref<Article[]>([]);
const popularArticles = ref<Article[]>([]);

// 获取热门标签
const fetchPopularTags = async () => {
  try {
    popularTags.value = await getPopularTags(10);
  } catch (error) {
    console.error('Failed to fetch popular tags:', error);
  }
};

// 获取最新文章
const fetchLatestArticles = async () => {
  loading.value = true;
  try {
    latestArticles.value = await getLatestArticles(6);
  } catch (error) {
    console.error('Failed to fetch latest articles:', error);
  } finally {
    loading.value = false;
  }
};

// 获取热门文章
const fetchPopularArticles = async () => {
  try {
    popularArticles.value = await getPopularArticles(10);
  } catch (error) {
    console.error('Failed to fetch popular articles:', error);
  }
};

// 处理标签点击
const handleTagClick = (tag: Tag) => {
  router.push({
    name: 'TagDetail',
    params: { id: tag.id },
    query: { name: tag.name },
  });
};

// 处理文章点击
const handleArticleClick = (article: Article) => {
  router.push(`/articles/${article.id}`);
};

onMounted(() => {
  fetchPopularTags();
  fetchLatestArticles();
  fetchPopularArticles();
});
</script>

<style scoped>
.home-view {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.mb-4 {
  margin-bottom: 20px;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.popular-tags {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.popular-tag-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.popular-tag-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}

.tag-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #1890ff;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  margin-right: 8px;
}

.tag-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 8px;
}

.tag-name {
  flex: 1;
  font-weight: 500;
}

.tag-count {
  color: #6c757d;
  font-size: 14px;
}

.popular-articles {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.popular-article-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.popular-article-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}

.article-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #52c41a;
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
  margin-right: 12px;
  flex-shrink: 0;
}

.article-info {
  flex: 1;
  min-width: 0;
}

.article-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-meta {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .home-view {
    padding: 16px;
  }

  .articles-grid {
    grid-template-columns: 1fr;
  }
}
</style>