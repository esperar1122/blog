<template>
  <div class="comment-moderation-panel">
    <!-- Filters -->
    <el-card class="filter-card">
      <el-form :model="filters" inline>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="选择状态" clearable>
            <el-option label="全部" value="" />
            <el-option label="正常" value="NORMAL" />
            <el-option label="已删除" value="DELETED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索评论内容"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Comments Table -->
    <el-card class="table-card">
      <div class="table-header">
        <span>评论列表</span>
        <div class="table-actions">
          <el-button
            type="danger"
            size="small"
            :disabled="selectedComments.length === 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="comments"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="content" label="评论内容" min-width="200">
          <template #default="{ row }">
            <div class="comment-content">
              {{ row.content }}
              <el-tag v-if="row.isEdited" type="info" size="small" class="edited-tag">
                已编辑
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="评论者" width="120">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="24" :src="row.userAvatar">
                {{ row.userName?.charAt(0) }}
              </el-avatar>
              <span class="username">{{ row.userName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'NORMAL' ? 'success' : 'danger'">
              {{ row.status === 'NORMAL' ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="handleViewComment(row)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.status === 'NORMAL'"
              type="danger"
              size="small"
              @click="handleDeleteComment(row)"
            >
              删除
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleRestoreComment(row)"
            >
              恢复
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- Comment Detail Dialog -->
    <el-dialog
      v-model="commentDetailVisible"
      title="评论详情"
      width="600px"
    >
      <div v-if="selectedComment" class="comment-detail">
        <div class="detail-item">
          <label>评论ID:</label>
          <span>{{ selectedComment.id }}</span>
        </div>
        <div class="detail-item">
          <label>评论内容:</label>
          <div class="comment-text">{{ selectedComment.content }}</div>
        </div>
        <div class="detail-item">
          <label>评论者:</label>
          <span>{{ selectedComment.userName }}</span>
        </div>
        <div class="detail-item">
          <label>创建时间:</label>
          <span>{{ formatDateTime(selectedComment.createTime) }}</span>
        </div>
        <div v-if="selectedComment.isEdited" class="detail-item">
          <label>编辑时间:</label>
          <span>{{ formatDateTime(selectedComment.editedTime) }}</span>
        </div>
        <div class="detail-item">
          <label>点赞数:</label>
          <span>{{ selectedComment.likeCount }}</span>
        </div>
        <div class="detail-item">
          <label>状态:</label>
          <el-tag :type="selectedComment.status === 'NORMAL' ? 'success' : 'danger'">
            {{ selectedComment.status === 'NORMAL' ? '正常' : '已删除' }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="commentDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Batch Delete Dialog -->
    <el-dialog
      v-model="batchDeleteVisible"
      title="批量删除评论"
      width="400px"
    >
      <p>确定要删除选中的 {{ selectedComments.length }} 条评论吗？</p>
      <el-input
        v-model="deleteReason"
        type="textarea"
        placeholder="请输入删除原因"
        :rows="3"
      />
      <template #footer>
        <el-button @click="batchDeleteVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmBatchDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import { commentService } from '@/services/commentService'
import type { Comment } from '@shared/types'

const commentModerationStore = useCommentModerationStore()

// Data
const loading = ref(false)
const comments = ref<Comment[]>([])
const selectedComments = ref<Comment[]>([])
const selectedComment = ref<Comment | null>(null)
const commentDetailVisible = ref(false)
const batchDeleteVisible = ref(false)
const deleteReason = ref('')

const filters = reactive({
  status: '',
  keyword: ''
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

// Methods
const fetchComments = async () => {
  loading.value = true
  try {
    const response = await commentService.getComments({
      articleId: 0, // Special query for all comments
      page: pagination.page,
      size: pagination.size,
      sortBy: 'createTime',
      sortOrder: 'desc'
    })

    if (response.success) {
      comments.value = response.data?.list || []
      pagination.total = response.data?.total || 0
    }
  } catch (error) {
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchComments()
}

const handleReset = () => {
  filters.status = ''
  filters.keyword = ''
  pagination.page = 1
  fetchComments()
}

const handleSelectionChange = (selection: Comment[]) => {
  selectedComments.value = selection
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  pagination.page = 1
  fetchComments()
}

const handleCurrentChange = (page: number) => {
  pagination.page = page
  fetchComments()
}

const handleViewComment = (comment: Comment) => {
  selectedComment.value = comment
  commentDetailVisible.value = true
}

const handleDeleteComment = async (comment: Comment) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      type: 'warning'
    })

    await commentService.deleteComment(comment.id)
    ElMessage.success('评论删除成功')
    await fetchComments()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除评论失败')
    }
  }
}

const handleRestoreComment = async (comment: Comment) => {
  try {
    await commentModerationService.moderateComment(comment.id, {
      status: 'APPROVE',
      reviewNote: '管理员恢复评论'
    })
    ElMessage.success('评论恢复成功')
    await fetchComments()
  } catch (error) {
    ElMessage.error('恢复评论失败')
  }
}

const handleBatchDelete = () => {
  if (selectedComments.value.length === 0) {
    ElMessage.warning('请选择要删除的评论')
    return
  }
  batchDeleteVisible.value = true
}

const confirmBatchDelete = async () => {
  try {
    await commentModerationService.batchModerateComments({
      commentIds: selectedComments.value.map(c => c.id),
      action: 'DELETE',
      reason: deleteReason.value
    })

    ElMessage.success('批量删除成功')
    batchDeleteVisible.value = false
    deleteReason.value = ''
    selectedComments.value = []
    await fetchComments()
  } catch (error) {
    ElMessage.error('批量删除失败')
  }
}

const formatDateTime = (date: Date | string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

// Lifecycle
onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.comment-moderation-panel {
  margin-top: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.comment-content {
  line-height: 1.5;
}

.edited-tag {
  margin-left: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}

.comment-detail {
  padding: 20px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 16px;
}

.detail-item label {
  width: 80px;
  font-weight: bold;
  color: #606266;
}

.detail-item span {
  flex: 1;
  color: #303133;
}

.comment-text {
  line-height: 1.6;
  background-color: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
}
</style>