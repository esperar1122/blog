<template>
  <div class="user-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="refreshStats">
            <el-icon><Refresh /></el-icon>
            刷新统计
          </el-button>
        </div>
      </template>

      <!-- 用户统计 -->
      <UserStats ref="userStatsRef" />

      <!-- 搜索和筛选 -->
      <div class="search-section">
        <el-form :model="queryForm" inline>
          <el-form-item label="关键词">
            <el-input
              v-model="queryForm.keyword"
              placeholder="搜索用户名、邮箱、昵称"
              clearable
              @clear="handleSearch"
              style="width: 200px"
            />
          </el-form-item>

          <el-form-item label="角色">
            <el-select
              v-model="queryForm.role"
              placeholder="选择角色"
              clearable
              @change="handleSearch"
              style="width: 120px"
            >
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>

          <el-form-item label="状态">
            <el-select
              v-model="queryForm.status"
              placeholder="选择状态"
              clearable
              @change="handleSearch"
              style="width: 120px"
            >
              <el-option label="活跃" value="ACTIVE" />
              <el-option label="未激活" value="INACTIVE" />
              <el-option label="禁用" value="BANNED" />
            </el-select>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><RefreshRight /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 用户列表 -->
      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        stripe
      >
        <el-table-column type="index" width="50" label="#" />

        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar
              :src="row.avatar"
              :alt="row.nickname || row.username"
              size="large"
            >
              {{ (row.nickname || row.username).charAt(0).toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>

        <el-table-column label="用户信息" min-width="200">
          <template #default="{ row }">
            <div>
              <div class="username">{{ row.username }}</div>
              <div class="nickname" v-if="row.nickname">{{ row.nickname }}</div>
              <div class="email">{{ row.email }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
              {{ row.role === 'ADMIN' ? '管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <div class="status-cell">
              <el-tag
                :type="getStatusType(row.status)"
                effect="light"
              >
                {{ getStatusText(row.status) }}
              </el-tag>
              <el-switch
                v-model="row.active"
                :loading="row.statusLoading"
                @change="() => toggleUserStatus(row)"
                style="margin-left: 10px"
                :disabled="row.id === currentUserId"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="最后登录" width="180">
          <template #default="{ row }">
            {{ row.lastLoginTime ? formatDateTime(row.lastLoginTime) : '从未登录' }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="editUser(row)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteUser(row)"
              :disabled="row.id === currentUserId"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑用户对话框 -->
    <UserEditDialog
      v-model="editDialogVisible"
      :user="editingUser"
      @success="handleEditSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type MessageBoxData } from 'element-plus'
import { Refresh, Search, RefreshRight } from '@element-plus/icons-vue'
import UserStats from '@/components/admin/UserStats.vue'
import UserEditDialog from '@/components/admin/UserEditDialog.vue'
import adminService, { type User, type UserQueryParams } from '@/services/adminService'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const userStatsRef = ref()
const loading = ref(false)
const userList = ref<User[]>([])
const editDialogVisible = ref(false)
const editingUser = ref<User>()

const currentUserId = userStore.userInfo?.id || 0

const queryForm = reactive<UserQueryParams>({
  keyword: '',
  role: '',
  status: '',
  page: 0,
  size: 20
})

const pagination = reactive({
  current: 1,
  size: 20,
  total: 0
})

const loadUserList = async () => {
  try {
    loading.value = true
    const params: UserQueryParams = {
      page: pagination.current - 1,
      size: pagination.size,
      keyword: queryForm.keyword,
      role: queryForm.role,
      status: queryForm.status
    }

    const response = await adminService.getUserList(params)
    userList.value = response.records.map(user => ({
      ...user,
      active: user.status === 'ACTIVE',
      statusLoading: false
    }))
    pagination.total = response.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  loadUserList()
}

const handleReset = () => {
  queryForm.keyword = ''
  queryForm.role = ''
  queryForm.status = ''
  pagination.current = 1
  loadUserList()
}

const handleSizeChange = () => {
  pagination.current = 1
  loadUserList()
}

const handleCurrentChange = () => {
  loadUserList()
}

const refreshStats = () => {
  userStatsRef.value?.loadStats()
}

const editUser = (user: User) => {
  editingUser.value = { ...user }
  editDialogVisible.value = true
}

const handleEditSuccess = () => {
  loadUserList()
  refreshStats()
}

const toggleUserStatus = async (user: any) => {
  try {
    user.statusLoading = true
    await adminService.toggleUserStatus(user.id)
    ElMessage.success(`用户状态已${user.active ? '启用' : '禁用'}`)

    // 更新本地状态
    const index = userList.value.findIndex(u => u.id === user.id)
    if (index !== -1) {
      userList.value[index].status = user.active ? 'ACTIVE' : 'BANNED'
    }

    refreshStats()
  } catch (error: any) {
    console.error('切换用户状态失败:', error)
    ElMessage.error(error.message || '切换用户状态失败')
    // 恢复开关状态
    user.active = !user.active
  } finally {
    user.statusLoading = false
  }
}

const deleteUser = async (user: User) => {
  try {
    const result: MessageBoxData = await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '删除用户',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )

    if (result === 'confirm') {
      await adminService.deleteUser(user.id)
      ElMessage.success('用户已删除')
      loadUserList()
      refreshStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除用户失败:', error)
      ElMessage.error('删除用户失败')
    }
  }
}

const getStatusType = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'warning'
    case 'BANNED':
      return 'danger'
    default:
      return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'ACTIVE':
      return '活跃'
    case 'INACTIVE':
      return '未激活'
    case 'BANNED':
      return '禁用'
    default:
      return '未知'
  }
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  loadUserList()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-section {
  margin: 20px 0;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.username {
  font-weight: bold;
  color: #303133;
}

.nickname {
  color: #606266;
  font-size: 14px;
  margin-top: 2px;
}

.email {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.status-cell {
  display: flex;
  align-items: center;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>