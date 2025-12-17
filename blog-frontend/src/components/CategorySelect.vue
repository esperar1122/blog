<template>
  <el-tree-select
    v-model="selectedCategoryId"
    :data="categories"
    :props="treeProps"
    placeholder="请选择分类"
    :disabled="loading"
    clearable
    check-strictly
    :render-after-expand="false"
    node-key="id"
    :filter-node-method="filterNode"
    filterable
    @change="handleChange"
  >
    <template #default="{ data }">
      <div class="category-option">
        <span class="category-icon" v-if="data.icon">
          <el-icon><component :is="data.icon" /></el-icon>
        </span>
        <span class="category-name">{{ data.name }}</span>
        <span class="category-count">({{ data.articleCount }})</span>
      </div>
    </template>
  </el-tree-select>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { getCategoryTree } from '@/api/category'
import type { CategoryTreeItem } from '@blog/shared/types'

interface Props {
  modelValue?: number | null
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
  size?: 'large' | 'default' | 'small'
}

interface Emits {
  (e: 'update:modelValue', value: number | null): void
  (e: 'change', value: number | null, category: CategoryTreeItem | null): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  placeholder: '请选择分类',
  disabled: false,
  clearable: true,
  size: 'default'
})

const emit = defineEmits<Emits>()

const loading = ref(false)
const categories = ref<CategoryTreeItem[]>([])

const selectedCategoryId = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const treeProps = {
  label: 'name',
  value: 'id',
  children: 'children'
}

// 加载分类树
const loadCategories = async () => {
  try {
    loading.value = true
    categories.value = await getCategoryTree()
  } catch (error) {
    console.error('加载分类失败:', error)
  } finally {
    loading.value = false
  }
}

// 节点过滤方法
const filterNode = (value: string, data: CategoryTreeItem) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 处理选择变化
const handleChange = (value: number | null) => {
  const category = value ? findCategoryById(categories.value, value) : null
  emit('change', value, category)
}

// 根据ID查找分类
const findCategoryById = (categories: CategoryTreeItem[], id: number): CategoryTreeItem | null => {
  for (const category of categories) {
    if (category.id === id) return category
    if (category.children) {
      const found = findCategoryById(category.children, id)
      if (found) return found
    }
  }
  return null
}

// 暴露方法供外部调用
const refresh = () => {
  loadCategories()
}

defineExpose({
  refresh
})

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-option {
  display: flex;
  align-items: center;
  width: 100%;
}

.category-icon {
  margin-right: 8px;
  font-size: 16px;
}

.category-name {
  flex: 1;
  font-weight: 500;
}

.category-count {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

:deep(.el-tree-select__dropdown) {
  max-height: 300px;
}

:deep(.el-tree-select__tag) {
  max-width: 200px;
}
</style>