<template>
  <div class="blacklist-manager">
    <!-- Header Actions -->
    <el-card class="action-card">
      <div class="action-header">
        <span>黑名单管理</span>
        <el-button type="primary" @click="handleAddBlacklist">
          添加黑名单
        </el-button>
      </div>
    </el-card>

    <!-- Blacklist Table -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="blacklist">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户" width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.user?.avatar">
                {{ row.user?.nickname?.charAt(0) }}
              </el-avatar>
              <div class="user-details">
                <div class="nickname">{{ row.user?.nickname }}</div>
                <div class="username">@{{ row.user?.username }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="拉黑原因" min-width="200">
          <template #default="{ row }">
            <span>{{ row.reason || '未说明原因' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="拉黑人" width="120">
          <template #default="{ row }">
            <span>{{ row.blacklistedByUser?.nickname }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="拉黑时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="160">
          <template #default="{ row }">
            <span v-if="row.expireTime">{{ formatDateTime(row.expireTime) }}</span>
            <el-tag v-else type="danger" size="small">永久</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="isActive(row) ? 'danger' : 'info'">
              {{ isActive(row) ? '生效中' : '已过期' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isActive(row)"
              type="danger"
              size="small"
              @click="handleRemoveBlacklist(row)"
            >
              移除
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleReactivateBlacklist(row)"
            >
              重新激活
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="blacklist.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无黑名单数据" />
      </div>
    </el-card>

    <!-- Add Blacklist Dialog -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加黑名单"
      width="500px"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="用户" prop="userId">
          <el-select
            v-model="addForm.userId"
            placeholder="请选择用户"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="searchLoading"
            style="width: 100%"
          >
            <el-option
              v-for="user in searchResults"
              :key="user.id"
              :label="`${user.nickname} (@${user.username})`"
              :value="user.id"
            >
              <div class="user-option">
                <el-avatar :size="24" :src="user.avatar">
                  {{ user.nickname?.charAt(0) }}
                </el-avatar>
                <div class="user-option-info">
                  <span class="nickname">{{ user.nickname }}</span>
                  <span class="username">@{{ user.username }}</span>
                </div>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="拉黑原因" prop="reason">
          <el-input
            v-model="addForm.reason"
            type="textarea"
            placeholder="请输入拉黑原因"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="addForm.expireTime"
            type="datetime"
            placeholder="选择过期时间（留空表示永久拉黑）"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAddBlacklist">确认添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useCommentModerationStore } from '@/stores/commentModerationStore'
import { userService } from '@/services/userService'
import type { CommentBlacklist, User } from '@shared/types'

const commentModerationStore = useCommentModerationStore()

// Data
const loading = ref(false)
const searchLoading = ref(false)
const blacklist = ref<CommentBlacklist[]>([])
const searchResults = ref<User[]>([])
const addDialogVisible = ref(false)

const addFormRef = ref<FormInstance>()
const addForm = reactive({
  userId: null as number | null,
  reason: '',
  expireTime: ''
})

const addFormRules: FormRules = {
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入拉黑原因', trigger: 'blur' }
  ]
}

// Methods
const fetchBlacklist = async () => {
  loading.value = true
  try {
    await commentModerationStore.fetchBlacklist()
    blacklist.value = commentModerationStore.blacklist
  } catch (error) {
    ElMessage.error('获取黑名单失败')
  } finally {
    loading.value = false
  }
}

const searchUsers = async (query: string) => {
  if (query.length < 2) {
    searchResults.value = []
    return
  }

  searchLoading.value = true
  try {
    const response = await userService.searchUsers({ keyword: query })
    if (response.success) {
      searchResults.value = response.data || []
    }
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    searchLoading.value = false
  }
}

const handleAddBlacklist = () => {
  addForm.userId = null
  addForm.reason = ''
  addForm.expireTime = ''
  searchResults.value = []
  addDialogVisible.value = true
}

const confirmAddBlacklist = async () => {
  if (!addFormRef.value) return

  try {
    await addFormRef.value.validate()
  } catch (error) {
    return
  }

  try {
    await commentModerationStore.addToBlacklist(addForm.userId!, {
      reason: addForm.reason,
      expireTime: addForm.expireTime ? new Date(addForm.expireTime) : undefined
    })

    ElMessage.success('添加黑名单成功')
    addDialogVisible.value = false
    await fetchBlacklist()
  } catch (error) {
    ElMessage.error('添加黑名单失败')
  }
}

const handleRemoveBlacklist = async (item: CommentBlacklist) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 ${item.user?.nickname} 从黑名单中移除吗？`,
      '确认移除',
      { type: 'warning' }
    )

    await commentModerationService.removeFromBlacklist(item.userId)
    ElMessage.success('移除黑名单成功')
    await fetchBlacklist()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('移除黑名单失败')
    }
  }
}

const handleReactivateBlacklist = async (item: CommentBlacklist) => {
  try {
    await commentModerationStore.addToBlacklist(item.userId, {
      reason: item.reason || '重新激活拉黑',
      expireTime: undefined // 永久拉黑
    })

    ElMessage.success('重新激活黑名单成功')
    await fetchBlacklist()
  } catch (error) {
    ElMessage.error('重新激活黑名单失败')
  }
}

const isActive = (item: CommentBlacklist) => {
  return !item.expireTime || new Date(item.expireTime) > new Date()
}

const formatDateTime = (date: Date | string) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

// Lifecycle
onMounted(() => {
  fetchBlacklist()
})
</script>

<style scoped>
.blacklist-manager {
  margin-top: 20px;
}

.action-card {
  margin-bottom: 20px;
}

.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  flex: 1;
}

.nickname {
  font-weight: bold;
  color: #303133;
}

.username {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 40px;
  text-align: center;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-option-info {
  display: flex;
  flex-direction: column;
}

.user-option-info .nickname {
  font-weight: normal;
  color: #303133;
}

.user-option-info .username {
  font-size: 12px;
  color: #909399;
}
</style>