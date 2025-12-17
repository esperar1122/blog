<template>
  <el-select
    v-model="selectedTagIds"
    :multiple="multiple"
    :placeholder="placeholder"
    :loading="loading"
    :disabled="disabled"
    :filterable="filterable"
    :remote="remote"
    :remote-method="remoteMethod"
    :clearable="clearable"
    :collapse-tags="collapseTags"
    :collapse-tags-tooltip="collapseTagsTooltip"
    @change="handleChange"
    @clear="handleClear"
  >
    <el-option
      v-for="tag in filteredTags"
      :key="tag.id"
      :label="tag.name"
      :value="tag.id"
    >
      <div class="flex items-center">
        <span
          class="inline-block w-3 h-3 rounded-full mr-2"
          :style="{ backgroundColor: tag.color }"
        ></span>
        <span>{{ tag.name }}</span>
        <span class="ml-auto text-gray-500 text-sm">
          ({{ tag.articleCount }})
        </span>
      </div>
    </el-option>
  </el-select>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import type { Tag } from '@blog/shared/types';
import { getAllTags, searchTags } from '@/api/tag';

interface Props {
  modelValue?: number | number[];
  tags?: Tag[]; // 传入的标签列表，如果不传则自动获取
  multiple?: boolean;
  placeholder?: string;
  disabled?: boolean;
  filterable?: boolean;
  remote?: boolean;
  clearable?: boolean;
  collapseTags?: boolean;
  collapseTagsTooltip?: boolean;
  showPopular?: boolean; // 是否优先显示热门标签
  popularLimit?: number; // 热门标签数量限制
}

interface Emits {
  (e: 'update:modelValue', value: number | number[]): void;
  (e: 'change', value: number | number[], tags: Tag | Tag[]): void;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: undefined,
  tags: undefined,
  multiple: false,
  placeholder: '请选择标签',
  disabled: false,
  filterable: true,
  remote: false,
  clearable: true,
  collapseTags: false,
  collapseTagsTooltip: true,
  showPopular: false,
  popularLimit: 10,
});

const emit = defineEmits<Emits>();

const loading = ref(false);
const allTags = ref<Tag[]>([]);
const popularTags = ref<Tag[]>([]);
const searchQuery = ref('');

const selectedTagIds = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
});

// 过滤后的标签列表
const filteredTags = computed(() => {
  let tags = props.tags || allTags.value;

  if (props.showPopular && popularTags.value.length > 0) {
    // 将热门标签放在前面
    const popularIds = new Set(popularTags.value.map(t => t.id));
    const popular = tags.filter(t => popularIds.has(t.id));
    const normal = tags.filter(t => !popularIds.has(t.id));

    // 按文章数量排序
    popular.sort((a, b) => b.articleCount - a.articleCount);
    normal.sort((a, b) => b.articleCount - a.articleCount);

    return [...popular, ...normal];
  }

  return tags;
});

// 获取标签列表
const fetchTags = async () => {
  if (props.tags) return; // 如果传入了tags，则不获取

  loading.value = true;
  try {
    if (props.showPopular) {
      // 获取热门标签
      const [popularRes, allRes] = await Promise.all([
        searchTags('', props.popularLimit),
        getAllTags(),
      ]);
      popularTags.value = popularRes;
      allTags.value = allRes;
    } else {
      allTags.value = await getAllTags();
    }
  } catch (error) {
    ElMessage.error('获取标签列表失败');
    console.error('Failed to fetch tags:', error);
  } finally {
    loading.value = false;
  }
};

// 远程搜索
const remoteMethod = async (query: string) => {
  searchQuery.value = query;

  if (!query) {
    await fetchTags();
    return;
  }

  loading.value = true;
  try {
    const results = await searchTags(query, 50);
    allTags.value = results;
  } catch (error) {
    ElMessage.error('搜索标签失败');
    console.error('Failed to search tags:', error);
  } finally {
    loading.value = false;
  }
};

// 处理选择变化
const handleChange = (value: number | number[]) => {
  const selectedTags = Array.isArray(value)
    ? filteredTags.value.filter(t => value.includes(t.id))
    : filteredTags.value.find(t => t.id === value);

  emit('change', value, selectedTags);
};

// 处理清空
const handleClear = () => {
  emit('change', props.multiple ? [] : 0, props.multiple ? [] : null);
};

// 监听showPopular变化
watch(() => props.showPopular, fetchTags, { immediate: true });

// 监听tags变化
watch(() => props.tags, (newTags) => {
  if (newTags) {
    allTags.value = newTags;
  }
}, { immediate: true });

// 初始化
onMounted(() => {
  if (!props.tags && !props.remote) {
    fetchTags();
  }
});
</script>

<style scoped>
.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.inline-block {
  display: inline-block;
}

.w-3 {
  width: 0.75rem;
}

.h-3 {
  height: 0.75rem;
}

.rounded-full {
  border-radius: 50%;
}

.mr-2 {
  margin-right: 0.5rem;
}

.ml-auto {
  margin-left: auto;
}

.text-gray-500 {
  color: #6b7280;
}

.text-sm {
  font-size: 0.875rem;
}
</style>