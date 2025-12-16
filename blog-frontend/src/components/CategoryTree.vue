<template>
  <div class="category-tree">
    <el-tree
      ref="treeRef"
      :data="categories"
      :props="treeProps"
      :default-expand-all="expandAll"
      :expand-on-click-node="false"
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      node-key="id"
      draggable
      @node-drop="handleNodeDrop"
    >
      <template #default="{ node, data }">
        <div class="category-node">
          <span class="category-icon" v-if="data.icon">
            <el-icon><component :is="data.icon" /></el-icon>
          </span>
          <span class="category-name">{{ node.label }}</span>
          <span class="category-count">({{ data.articleCount }})</span>
          <div class="category-actions">
            <el-button
              type="primary"
              link
              size="small"
              @click.stop="handleEdit(data)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click.stop="handleDelete(data)"
            >
              删除
            </el-button>
          </div>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElTree, ElMessageBox } from 'element-plus'
import type { CategoryTreeItem } from '@blog/shared/types'

interface Props {
  categories: CategoryTreeItem[]
  expandAll?: boolean
}

interface Emits {
  (e: 'edit', category: CategoryTreeItem): void
  (e: 'delete', category: CategoryTreeItem): void
  (e: 'sort', categories: CategoryTreeItem[]): void
}

const props = withDefaults(defineProps<Props>(), {
  expandAll: true
})

const emit = defineEmits<Emits>()

const treeRef = ref<InstanceType<typeof ElTree>>()

const treeProps = {
  label: 'name',
  children: 'children'
}

// 允许拖拽
const allowDrag = (draggingNode: any) => {
  return true
}

// 允许放置
const allowDrop = (draggingNode: any, dropNode: any, type: 'prev' | 'inner' | 'next') => {
  // 防止将节点拖拽到自己的子节点下
  if (type === 'inner') {
    return !isChildOf(draggingNode.data, dropNode.data)
  }
  return true
}

// 检查是否为子节点
const isChildOf = (child: CategoryTreeItem, parent: CategoryTreeItem): boolean => {
  if (!parent.children) return false

  for (const item of parent.children) {
    if (item.id === child.id) return true
    if (isChildOf(child, item)) return true
  }

  return false
}

// 处理节点拖拽
const handleNodeDrop = (draggingNode: any, dropNode: any, type: string) => {
  const treeData = treeRef.value?.getStore().states.data.value as CategoryTreeItem[]

  // 更新父子关系和排序
  const updateCategories = (categories: CategoryTreeItem[], parentId: number | null = null): CategoryTreeItem[] => {
    return categories.map((category, index) => {
      const updated = {
        ...category,
        parentId,
        sortOrder: index + 1
      }

      if (category.children) {
        updated.children = updateCategories(category.children, category.id)
      }

      return updated
    })
  }

  const updatedCategories = updateCategories(treeData)
  emit('sort', updatedCategories)
}

// 处理编辑
const handleEdit = (category: CategoryTreeItem) => {
  emit('edit', category)
}

// 处理删除
const handleDelete = (category: CategoryTreeItem) => {
  ElMessageBox.confirm(
    `确定要删除分类"${category.name}"吗？${category.articleCount > 0 ? '\n注意：该分类下还有文章，需要先移除所有文章。' : ''}`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    emit('delete', category)
  })
}
</script>

<style scoped>
.category-tree {
  padding: 20px;
}

.category-node {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
  flex: 1;
}

.category-node:hover {
  background-color: var(--el-fill-color-light);
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
  margin-right: 12px;
}

.category-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.category-node:hover .category-actions {
  opacity: 1;
}

:deep(.el-tree-node__content) {
  height: auto;
  padding: 4px 0;
}

:deep(.el-tree-node__expand-icon) {
  padding: 6px;
}
</style>