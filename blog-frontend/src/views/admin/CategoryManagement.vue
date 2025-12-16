<template>
  <div class="category-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>分类管理</h3>
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增分类
          </el-button>
        </div>
      </template>

      <!-- 操作工具栏 -->
      <div class="toolbar">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-input
              v-model="searchText"
              placeholder="搜索分类名称"
              clearable
              @input="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="4">
            <el-switch
              v-model="expandAll"
              active-text="展开全部"
              inactive-text="折叠全部"
              @change="handleExpandChange"
            />
          </el-col>
        </el-row>
      </div>

      <!-- 分类树 -->
      <CategoryTree
        v-if="!loading"
        :categories="filteredCategories"
        :expand-all="expandAll"
        @edit="handleEdit"
        @delete="handleDelete"
        @sort="handleSort"
      />

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
    </el-card>

    <!-- 分类表单弹窗 -->
    <CategoryForm
      v-model="showForm"
      :category="currentCategory"
      :categories="categories"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import CategoryTree from '@/components/CategoryTree.vue'
import CategoryForm from '@/components/CategoryForm.vue'
import {
  getCategoryTree,
  createCategory,
  updateCategory,
  deleteCategory,
  updateCategorySort
} from '@/api/category'
import type {
  Category,
  CategoryTreeItem,
  CreateCategoryRequest,
  UpdateCategoryRequest
} from '@blog/shared/types'

// 响应式数据
const loading = ref(false)
const categories = ref<CategoryTreeItem[]>([])
const searchText = ref('')
const expandAll = ref(true)
const showForm = ref(false)
const currentCategory = ref<Category | null>(null)

// 过滤后的分类
const filteredCategories = computed(() => {
  if (!searchText.value) return categories.value

  const filterTree = (items: CategoryTreeItem[]): CategoryTreeItem[] => {
    return items.reduce((result: CategoryTreeItem[], item) => {
      const nameMatch = item.name.toLowerCase().includes(searchText.value.toLowerCase())
      const childrenMatch = item.children ? filterTree(item.children) : []

      if (nameMatch || childrenMatch.length > 0) {
        result.push({
          ...item,
          children: childrenMatch
        })
      }

      return result
    }, [])
  }

  return filterTree(categories.value)
})

// 加载分类树
const loadCategories = async () => {
  try {
    loading.value = true
    categories.value = await getCategoryTree()
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 展开折叠处理
const handleExpandChange = (value: boolean) => {
  // 由组件内部处理
}

// 创建分类
const handleCreate = () => {
  currentCategory.value = null
  showForm.value = true
}

// 编辑分类
const handleEdit = (category: CategoryTreeItem) => {
  currentCategory.value = category
  showForm.value = true
}

// 删除分类
const handleDelete = async (category: CategoryTreeItem) => {
  if (category.articleCount > 0) {
    ElMessage.error('该分类下还有文章，请先移除所有文章后再删除')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${category.name}"吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )

    await deleteCategory(category.id)
    ElMessage.success('删除成功')
    await loadCategories()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除分类失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 处理排序
const handleSort = async (sortedCategories: CategoryTreeItem[]) => {
  try {
    const flattenCategories = (categories: CategoryTreeItem[]): Category[] => {
      const result: Category[] = []
      const traverse = (items: CategoryTreeItem[]) => {
        items.forEach(item => {
          const { children, ...category } = item
          result.push(category)
          if (children && children.length > 0) {
            traverse(children)
          }
        })
      }
      traverse(categories)
      return result
    }

    const categoriesToSort = flattenCategories(sortedCategories)
    await updateCategorySort(categoriesToSort)
    ElMessage.success('排序更新成功')
    await loadCategories()
  } catch (error) {
    console.error('更新排序失败:', error)
    ElMessage.error('更新排序失败')
  }
}

// 提交表单
const handleSubmit = async (data: CreateCategoryRequest | UpdateCategoryRequest) => {
  try {
    if (currentCategory.value) {
      // 编辑模式
      await updateCategory(currentCategory.value.id, data as UpdateCategoryRequest)
      ElMessage.success('更新成功')
    } else {
      // 创建模式
      await createCategory(data as CreateCategoryRequest)
      ElMessage.success('创建成功')
    }

    showForm.value = false
    await loadCategories()
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.toolbar {
  margin-bottom: 20px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.loading-container {
  padding: 20px;
}

:deep(.el-card__header) {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

:deep(.el-card__body) {
  padding: 20px;
}

:deep(.el-button--danger) {
  background-color: var(--el-color-danger);
  border-color: var(--el-color-danger);
}

:deep(.el-button--danger:hover) {
  background-color: var(--el-color-danger-light-3);
  border-color: var(--el-color-danger-light-3);
}
</style>