<template>
  <div class="category-filter">
    <el-card class="filter-card">
      <template #header>
        <div class="filter-header">
          <h3>分类筛选</h3>
          <el-button
            v-if="selectedCategory"
            link
            type="primary"
            size="small"
            @click="handleClear"
          >
            清除
          </el-button>
        </div>
      </template>

      <div class="category-list">
        <div
          v-for="category in categories"
          :key="category.id"
          class="category-item"
          :class="{ active: selectedCategory?.id === category.id }"
          @click="handleSelect(category)"
        >
          <span class="category-icon" v-if="category.icon">
            <el-icon><component :is="category.icon" /></el-icon>
          </span>
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">({{ category.articleCount }})</span>
        </div>

        <!-- 全部分类选项 -->
        <div
          class="category-item"
          :class="{ active: !selectedCategory }"
          @click="handleClear"
        >
          <span class="category-icon">
            <el-icon><Collection /></el-icon>
          </span>
          <span class="category-name">全部分类</span>
          <span class="category-count">({{ totalArticleCount }})</span>
        </div>
      </div>
    </el-card>

    <!-- 子分类展示 -->
    <el-card v-if="selectedCategory && childCategories.length > 0" class="filter-card">
      <template #header>
        <div class="filter-header">
          <h4>{{ selectedCategory.name }} 的子分类</h4>
        </div>
      </template>

      <div class="category-list child-categories">
        <div
          v-for="category in childCategories"
          :key="category.id"
          class="category-item"
          :class="{ active: selectedCategory?.id === category.id }"
          @click="handleSelect(category)"
        >
          <span class="category-icon" v-if="category.icon">
            <el-icon><component :is="category.icon" /></el-icon>
          </span>
          <span class="category-name">{{ category.name }}</span>
          <span class="category-count">({{ category.articleCount }})</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { Collection } from '@element-plus/icons-vue'
import { getAllCategories } from '@/api/category'
import type { Category, CategoryTreeItem } from '@blog/shared/types'

interface Props {
  modelValue?: Category | null
  showChildCategories?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: Category | null): void
  (e: 'change', category: Category | null): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  showChildCategories: true
})

const emit = defineEmits<Emits>()

const categories = ref<Category[]>([])
const childCategories = ref<Category[]>([])

const selectedCategory = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 总文章数
const totalArticleCount = computed(() => {
  return categories.value.reduce((total, category) => {
    return total + (category.articleCount || 0)
  }, 0)
})

// 加载所有分类
const loadCategories = async () => {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

// 处理选择分类
const handleSelect = (category: Category) => {
  selectedCategory.value = category
  emit('change', category)

  // 加载子分类
  if (props.showChildCategories && category.parentId === null) {
    childCategories.value = categories.value.filter(c => c.parentId === category.id)
  } else {
    childCategories.value = []
  }
}

// 处理清除选择
const handleClear = () => {
  selectedCategory.value = null
  childCategories.value = []
  emit('change', null)
}

// 监听选中分类变化
watch(
  () => selectedCategory.value,
  (newCategory) => {
    if (newCategory && props.showChildCategories) {
      if (newCategory.parentId === null) {
        // 如果是顶级分类，加载子分类
        childCategories.value = categories.value.filter(c => c.parentId === newCategory.id)
      } else {
        // 如果是子分类，清空子分类列表
        childCategories.value = []
      }
    } else {
      childCategories.value = []
    }
  },
  { immediate: true }
)

// 暴露方法
defineExpose({
  refresh: loadCategories,
  clear: handleClear
})

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-filter {
  width: 100%;
}

.filter-card {
  margin-bottom: 16px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-header h3,
.filter-header h4 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.filter-header h4 {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.category-list {
  max-height: 300px;
  overflow-y: auto;
}

.child-categories {
  max-height: 200px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 10px 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s;
  margin-bottom: 4px;
}

.category-item:hover {
  background-color: var(--el-fill-color-light);
}

.category-item.active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.category-icon {
  margin-right: 8px;
  font-size: 16px;
  flex-shrink: 0;
}

.category-name {
  flex: 1;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-left: 8px;
  flex-shrink: 0;
}

/* 滚动条样式 */
.category-list::-webkit-scrollbar {
  width: 4px;
}

.category-list::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 2px;
}

.category-list::-webkit-scrollbar-thumb {
  background: var(--el-fill-color-dark);
  border-radius: 2px;
}

.category-list::-webkit-scrollbar-thumb:hover {
  background: var(--el-fill-color-darker);
}
</style>