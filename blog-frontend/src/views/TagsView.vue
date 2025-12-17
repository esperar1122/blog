<template>
  <div class="tags-view">
    <el-row :gutter="20">
      <!-- 标签云 -->
      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <h2>标签云</h2>
          </template>
          <TagCloud
            title=""
            :limit="50"
            :show-more="true"
            @tag-click="handleTagClick"
          />
        </el-card>
      </el-col>

      <!-- 热门标签列表 -->
      <el-col :xs="24" :lg="8">
        <el-card>
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

          <div class="mt-4 text-center">
            <el-button type="primary" link @click="viewAllTags">
              查看所有标签
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 所有标签列表 -->
    <el-card class="mt-4" v-if="showAllTagsList">
      <template #header>
        <div class="flex justify-between items-center">
          <h2>所有标签</h2>
          <el-button type="text" @click="showAllTagsList = false">
            收起
          </el-button>
        </div>
      </template>
      <TagList
        :show-create="false"
        :show-actions="false"
        :auto-load="true"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import type { Tag } from '@blog/shared/types';
import { getPopularTags } from '@/api/tag';
import TagCloud from '@/components/tag/TagCloud.vue';
import TagList from '@/components/tag/TagList.vue';

const router = useRouter();
const popularTags = ref<Tag[]>([]);
const showAllTagsList = ref(false);

// 获取热门标签
const fetchPopularTags = async () => {
  try {
    popularTags.value = await getPopularTags(20);
  } catch (error) {
    console.error('Failed to fetch popular tags:', error);
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

// 查看所有标签
const viewAllTags = () => {
  showAllTagsList.value = true;
};

onMounted(() => {
  fetchPopularTags();
});
</script>

<style scoped>
.tags-view {
  padding: 20px;
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

.mt-4 {
  margin-top: 16px;
}

.text-center {
  text-align: center;
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
  .tags-view {
    padding: 16px;
  }
}
</style>