<template>
  <el-dialog
    :title="isEdit ? '编辑分类' : '新增分类'"
    :model-value="modelValue"
    @update:model-value="handleClose"
    width="500px"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-position="top"
      @submit.prevent
    >
      <el-form-item label="分类名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入分类名称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="父级分类" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="categoryOptions"
          placeholder="请选择父级分类（不选则为顶级分类）"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          :render-after-expand="false"
          clearable
          check-strictly
          node-key="id"
          :filter-node-method="filterNode"
          filterable
        />
      </el-form-item>

      <el-form-item label="分类描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="3"
          placeholder="请输入分类描述"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="分类图标" prop="icon">
        <el-input
          v-model="formData.icon"
          placeholder="请输入图标名称（如：tech、life等）"
          maxlength="50"
        >
          <template #prefix>
            <el-icon v-if="formData.icon">
              <component :is="formData.icon" />
            </el-icon>
          </template>
        </el-input>
        <div class="icon-tips">
          <span>支持的图标：Document、Folder、Star、Heart、Collection、Tools、Reading、Camera 等</span>
        </div>
      </el-form-item>

      <el-form-item label="排序" prop="sortOrder">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="数字越小排序越靠前"
          style="width: 100%"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          {{ isEdit ? '更新' : '创建' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { ElForm, ElMessage } from 'element-plus'
import type {
  Category,
  CategoryTreeItem,
  CreateCategoryRequest,
  UpdateCategoryRequest
} from '@blog/shared/types'

interface Props {
  modelValue: boolean
  category?: Category | null
  categories: CategoryTreeItem[]
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'submit', data: CreateCategoryRequest | UpdateCategoryRequest): void
}

const props = withDefaults(defineProps<Props>(), {
  category: null
})

const emit = defineEmits<Emits>()

const formRef = ref<InstanceType<typeof ElForm>>()
const loading = ref(false)

const isEdit = computed(() => !!props.category)

const formData = reactive({
  name: '',
  parentId: null as number | null,
  description: '',
  icon: '',
  sortOrder: 0
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '长度不能超过 200 个字符', trigger: 'blur' }
  ],
  icon: [
    { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
  ]
}

// 过滤掉自己和自己的子分类（编辑时）
const categoryOptions = computed(() => {
  if (!isEdit.value) return props.categories

  const filterCategories = (categories: CategoryTreeItem[]): CategoryTreeItem[] => {
    return categories.filter(c => c.id !== props.category!.id).map(c => ({
      ...c,
      children: c.children ? filterCategories(c.children) : []
    }))
  }

  return filterCategories(props.categories)
})

// 重置表单
const resetForm = () => {
  formData.name = ''
  formData.parentId = null
  formData.description = ''
  formData.icon = ''
  formData.sortOrder = 0
  formRef.value?.clearValidate()
}

// 监听弹窗显示状态
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      if (props.category) {
        // 编辑模式
        Object.assign(formData, {
          name: props.category.name,
          parentId: props.category.parentId,
          description: props.category.description || '',
          icon: props.category.icon || '',
          sortOrder: props.category.sortOrder
        })
      } else {
        // 新增模式
        resetForm()
        // 获取当前最大排序值
        const maxSort = getMaxSortOrder(props.categories)
        formData.sortOrder = maxSort + 1
      }
    }
  }
)

// 获取最大排序值
const getMaxSortOrder = (categories: CategoryTreeItem[]): number => {
  let maxSort = 0
  const traverse = (cats: CategoryTreeItem[]) => {
    cats.forEach(cat => {
      if (cat.sortOrder > maxSort) {
        maxSort = cat.sortOrder
      }
      if (cat.children) {
        traverse(cat.children)
      }
    })
  }
  traverse(categories)
  return maxSort
}

// 节点过滤方法
const filterNode = (value: string, data: CategoryTreeItem) => {
  if (!value) return true
  return data.name.toLowerCase().includes(value.toLowerCase())
}

// 关闭弹窗
const handleClose = () => {
  emit('update:modelValue', false)
  resetForm()
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    const submitData = {
      name: formData.name,
      parentId: formData.parentId,
      description: formData.description,
      icon: formData.icon,
      sortOrder: formData.sortOrder
    }

    emit('submit', submitData)
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.icon-tips {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>