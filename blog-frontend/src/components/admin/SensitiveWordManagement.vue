<template>
  <div class="sensitive-word-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>敏感词管理</h3>
          <el-button type="primary" @click="handleAdd">添加敏感词</el-button>
        </div>
      </template>

      <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.keyword"
            placeholder="请输入关键词搜索"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>

        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="禁用" value="INACTIVE" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        :data="tableData"
        v-loading="loading"
        row-key="id"
        style="width: 100%"
      >
        <el-table-column label="敏感词" prop="word" min-width="150">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'REGEX'" type="warning">正则</el-tag>
            <span>{{ row.word }}</span>
          </template>
        </el-table-column>

        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 'WORD' ? 'primary' : 'warning'">
              {{ row.type === 'WORD' ? '文本' : '正则' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ getLevelText(row.level) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="handleEdit(row)"
            >
              编辑
            </el-button>

            <el-button
              link
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
            </el-button>

            <el-button
              link
              type="danger"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="queryParams.page"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
        />
      </div>
    </el-card>

    <SensitiveWordDialog
      v-model="dialogVisible"
      :word="currentWord"
      :is-edit="isEdit"
      @success="handleSuccess"
    />

    <ContentTestDialog
      v-model="testDialogVisible"
      @success="handleTestSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import adminContentService from '@/services/adminContentService'
import type { SensitiveWord } from '@/services/adminContentService'
import SensitiveWordDialog from './SensitiveWordDialog.vue'
import ContentTestDialog from './ContentTestDialog.vue'

const loading = ref(false)
const tableData = ref<SensitiveWord[]>([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: ''
})

const dialogVisible = ref(false)
const testDialogVisible = ref(false)
const isEdit = ref(false)
const currentWord = ref<SensitiveWord | null>(null)

const queryForm = ref<FormInstance>()

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const response = await adminContentService.getSensitiveWords(queryParams)
    tableData.value = response.data.records || []
    total.value = response.data.total || 0
  } catch (error) {
    ElMessage.error('加载数据失败')
    console.error('加载敏感词数据错误:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.page = 1
  loadData()
}

const resetQuery = () => {
  queryForm.value?.resetFields()
  queryParams.page = 1
  queryParams.keyword = ''
  queryParams.status = ''
  loadData()
}

const handleAdd = () => {
  isEdit.value = false
  currentWord.value = null
  dialogVisible.value = true
}

const handleEdit = (row: SensitiveWord) => {
  isEdit.value = true
  currentWord.value = { ...row }
  dialogVisible.value = true
}

const handleToggleStatus = async (row: SensitiveWord) => {
  try {
    const newStatus = row.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    await adminContentService.updateSensitiveWordStatus(row.id!, newStatus)

    ElMessage.success(`${newStatus === 'ACTIVE' ? '启用' : '禁用'}成功`)
    loadData()
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error('状态更新错误:', error)
  }
}

const handleDelete = async (row: SensitiveWord) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除敏感词"${row.word}"吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await adminContentService.deleteSensitiveWord(row.id!)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error('删除错误:', error)
    }
  }
}

const handleSuccess = () => {
  dialogVisible.value = false
  loadData()
}

const handleTestSuccess = () => {
  testDialogVisible.value = false
}

const getLevelType = (level: string) => {
  const levelMap: Record<string, string> = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return levelMap[level] || 'info'
}

const getLevelText = (level: string) => {
  const levelMap: Record<string, string> = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高'
  }
  return levelMap[level] || level
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.sensitive-word-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>