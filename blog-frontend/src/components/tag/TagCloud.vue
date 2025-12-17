<template>
  <div class="tag-cloud">
    <h3 v-if="title" class="tag-cloud-title">{{ title }}</h3>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="tags.length === 0" class="empty-container">
      <el-empty description="暂无标签" :image-size="100" />
    </div>

    <div v-else class="tag-cloud-content">
      <transition-group name="tag-fade" tag="div" class="tag-list">
        <a
          v-for="tag in sortedTags"
          :key="tag.id"
          :href="tagLink(tag)"
          :class="['tag-item', `tag-size-${getTagSize(tag)}`]"
          :style="{ color: tag.color }"
          @click.prevent="handleTagClick(tag)"
        >
          <span class="tag-name">{{ tag.name }}</span>
          <span class="tag-count">({{ tag.articleCount }})</span>
        </a>
      </transition-group>
    </div>

    <!-- 更多标签按钮 -->
    <div v-if="showMore && limited && totalTags > limit" class="more-container">
      <el-button type="text" @click="showAllTags = !showAllTags">
        {{ showAllTags ? '收起' : `查看更多 ${totalTags - limit} 个标签` }}
        <el-icon>
          <ArrowDown v-if="!showAllTags" />
          <ArrowUp v-else />
        </el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowDown, ArrowUp } from '@element-plus/icons-vue';
import type { Tag } from '@blog/shared/types';
import { getPopularTags } from '@/api/tag';

interface Props {
  title?: string;
  tags?: Tag[]; // 传入的标签列表，如果不传则自动获取热门标签
  limit?: number; // 限制显示数量
  showMore?: boolean; // 是否显示更多按钮
  sizeRange?: [number, number]; // 字体大小范围，单位为rem
  clickable?: boolean; // 是否可点击
}

interface Emits {
  (e: 'tagClick', tag: Tag): void;
}

const props = withDefaults(defineProps<Props>(), {
  title: '标签云',
  tags: undefined,
  limit: 20,
  showMore: true,
  sizeRange: () => [0.875, 1.5], // 14px to 24px
  clickable: true,
});

const emit = defineEmits<Emits>();

const router = useRouter();
const loading = ref(false);
const popularTags = ref<Tag[]>([]);
const showAllTags = ref(false);

// 计算总标签数
const totalTags = computed(() => {
  return props.tags ? props.tags.length : popularTags.value.length;
});

// 是否限制显示
const limited = computed(() => {
  return props.limit > 0 && !showAllTags.value;
});

// 显示的标签列表
const displayTags = computed(() => {
  const tags = props.tags || popularTags.value;

  if (limited.value) {
    return tags.slice(0, props.limit);
  }

  return tags;
});

// 排序后的标签（按文章数量）
const sortedTags = computed(() => {
  return [...displayTags.value].sort((a, b) => b.articleCount - a.articleCount);
});

// 获取标签
const fetchTags = async () => {
  if (props.tags) return;

  loading.value = true;
  try {
    popularTags.value = await getPopularTags(50); // 获取更多标签用于展示
  } catch (error) {
    console.error('Failed to fetch popular tags:', error);
  } finally {
    loading.value = false;
  }
};

// 根据文章数量计算标签大小级别
const getTagSize = (tag: Tag): number => {
  if (sortedTags.value.length === 0) return 3;

  const maxCount = Math.max(...sortedTags.value.map(t => t.articleCount));
  const minCount = Math.min(...sortedTags.value.map(t => t.articleCount));

  if (maxCount === minCount) return 3;

  // 将文章数量映射到 1-5 的级别
  const ratio = (tag.articleCount - minCount) / (maxCount - minCount);
  return Math.floor(ratio * 4) + 1;
};

// 获取标签链接
const tagLink = (tag: Tag): string => {
  return `/tags/${tag.id}?name=${encodeURIComponent(tag.name)}`;
};

// 处理标签点击
const handleTagClick = (tag: Tag) => {
  if (!props.clickable) return;

  emit('tagClick', tag);
  router.push({
    name: 'TagDetail',
    params: { id: tag.id },
    query: { name: tag.name },
  });
};

// 初始化
onMounted(() => {
  fetchTags();
});
</script>

<style scoped>
.tag-cloud {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}

.tag-cloud-title {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.loading-container,
.empty-container {
  padding: 20px 0;
}

.tag-cloud-content {
  position: relative;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  background: rgba(64, 158, 255, 0.08);
  border: 1px solid rgba(64, 158, 255, 0.2);
  border-radius: 16px;
  text-decoration: none;
  transition: all 0.3s ease;
  cursor: pointer;
}

.tag-item:hover {
  background: rgba(64, 158, 255, 0.15);
  border-color: rgba(64, 158, 255, 0.4);
  transform: translateY(-2px);
}

.tag-name {
  font-weight: 500;
  margin-right: 4px;
}

.tag-count {
  font-size: 0.875em;
  opacity: 0.7;
  font-weight: 400;
}

/* 标签大小级别 */
.tag-size-1 {
  font-size: var(--tag-size-min);
  opacity: 0.7;
}

.tag-size-2 {
  font-size: calc(var(--tag-size-min) + 0.125rem);
  opacity: 0.8;
}

.tag-size-3 {
  font-size: calc(var(--tag-size-min) + 0.25rem);
  opacity: 0.9;
}

.tag-size-4 {
  font-size: calc(var(--tag-size-min) + 0.375rem);
  opacity: 1;
}

.tag-size-5 {
  font-size: var(--tag-size-max);
  font-weight: 600;
  opacity: 1;
}

.more-container {
  margin-top: 16px;
  text-align: center;
}

/* 设置CSS变量 */
.tag-cloud {
  --tag-size-min: 0.875rem; /* 14px */
  --tag-size-max: 1.5rem; /* 24px */
}

/* 过渡动画 */
.tag-fade-enter-active,
.tag-fade-leave-active {
  transition: all 0.3s ease;
}

.tag-fade-enter-from,
.tag-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

/* 移动端适配 */
@media (max-width: 768px) {
  .tag-cloud {
    padding: 16px;
  }

  .tag-list {
    gap: 8px;
  }

  .tag-item {
    padding: 4px 10px;
  }

  .tag-cloud {
    --tag-size-min: 0.75rem; /* 12px */
    --tag-size-max: 1.25rem; /* 20px */
  }
}
</style>