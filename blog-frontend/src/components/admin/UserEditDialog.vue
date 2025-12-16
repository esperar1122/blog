<template>
  <el-dialog
    v-model="visible"
    title="编辑用户"
    width="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="80px"
    >
      <el-form-item label="用户名">
        <el-input v-model="user.username" disabled />
      </el-form-item>

      <el-form-item label="昵称" prop="nickname">
        <el-input
          v-model="form.nickname"
          placeholder="请输入昵称"
          maxlength="50"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input
          v-model="form.email"
          placeholder="请输入邮箱"
          maxlength="100"
        />
      </el-form-item>

      <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="请选择角色">
          <el-option label="普通用户" value="USER" />
          <el-option label="管理员" value="ADMIN" />
        </el-select>
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态">
          <el-option label="活跃" value="ACTIVE" />
          <el-option label="未激活" value="INACTIVE" />
          <el-option label="禁用" value="BANNED" />
        </el-select>
      </el-form-item>

      <el-form-item label="个人简介">
        <el-input
          v-model="form.bio"
          type="textarea"
          :rows="3"
          placeholder="请输入个人简介"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import adminService from '@/services/adminService'
import type { User, UserUpdateData } from '@/services/adminService'

interface Props {
  modelValue: boolean
  user?: User
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const loading = ref(false)
const visible = ref(false)

const user = ref<User>({
  id: 0,
  username: '',
  email: '',
  nickname: '',
  avatar: '',
  bio: '',
  role: 'USER',
  status: 'ACTIVE',
  createTime: '',
  updateTime: '',
  lastLoginTime: ''
})

const form = reactive<UserUpdateData>({
  nickname: '',
  email: '',
  bio: '',
  role: 'USER',
  status: 'ACTIVE'
})

const rules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择用户角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择用户状态', trigger: 'change' }
  ]
}

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(() => props.user, (newUser) => {
  if (newUser) {
    user.value = { ...newUser }
    form.nickname = newUser.nickname || ''
    form.email = newUser.email || ''
    form.bio = newUser.bio || ''
    form.role = newUser.role
    form.status = newUser.status
  }
}, { immediate: true })

const handleClose = () => {
  emit('update:modelValue', false)
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    loading.value = true

    await adminService.updateUser(user.value.id, form)

    ElMessage.success('用户信息更新成功')
    emit('success')
    handleClose()
  } catch (error: any) {
    console.error('更新用户失败:', error)
    ElMessage.error(error.message || '更新用户失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>