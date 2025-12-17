<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="80px"
    @submit.prevent="handleSubmit"
  >
    <el-form-item label="标签名称" prop="name">
      <el-input
        v-model="formData.name"
        placeholder="请输入标签名称"
        maxlength="30"
        show-word-limit
      />
    </el-form-item>

    <el-form-item label="标签颜色" prop="color">
      <el-color-picker
        v-model="formData.color"
        :predefine="predefineColors"
        show-alpha
        format="hex"
      />
      <span class="ml-2 text-gray-500">{{ formData.color }}</span>
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
      <el-button @click="handleReset">重置</el-button>
      <el-button @click="$emit('cancel')">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, computed } from 'vue';
import type { FormInstance, FormRules } from 'element-plus';
import type { Tag, CreateTagRequest, UpdateTagRequest } from '@blog/shared/types';
import { checkTagNameExists } from '@/api/tag';

interface Props {
  tag?: Tag | null;
  loading?: boolean;
}

interface Emits {
  (e: 'submit', data: CreateTagRequest | UpdateTagRequest): void;
  (e: 'cancel'): void;
}

const props = withDefaults(defineProps<Props>(), {
  tag: null,
  loading: false,
});

const emit = defineEmits<Emits>();

const formRef = ref<FormInstance>();
const isEdit = computed(() => !!props.tag);

const formData = reactive({
  name: '',
  color: '#1890ff',
});

// 预定义颜色
const predefineColors = [
  '#ff4500',
  '#ff8c00',
  '#ffd700',
  '#90ee90',
  '#00ced1',
  '#1e90ff',
  '#c71585',
  '#ff69b4',
  '#ba55d3',
  '#4169e1',
  '#1890ff',
  '#52c41a',
  '#fa541c',
  '#faad14',
  '#13c2c2',
  '#722ed1',
];

// 自定义验证：检查标签名称是否已存在
const validateName = async (rule: any, value: string, callback: any) => {
  if (!value) {
    return callback(new Error('请输入标签名称'));
  }

  try {
    const exists = await checkTagNameExists(
      value,
      isEdit.value ? props.tag?.id : undefined
    );
    if (exists) {
      callback(new Error('标签名称已存在'));
    } else {
      callback();
    }
  } catch (error) {
    callback(new Error('验证标签名称失败'));
  }
};

const rules: FormRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { max: 30, message: '标签名称长度不能超过30个字符', trigger: 'blur' },
    { validator: validateName, trigger: 'blur' },
  ],
  color: [
    {
      pattern: /^#[0-9A-Fa-f]{6}$/,
      message: '颜色必须是有效的十六进制格式',
      trigger: 'change',
    },
  ],
};

// 监听props变化，更新表单数据
watch(
  () => props.tag,
  (tag) => {
    if (tag) {
      formData.name = tag.name;
      formData.color = tag.color;
    } else {
      formData.name = '';
      formData.color = '#1890ff';
    }
  },
  { immediate: true }
);

const handleSubmit = async () => {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();

    const submitData = {
      name: formData.name,
      color: formData.color,
    };

    emit('submit', submitData);
  } catch (error) {
    // 表单验证失败
  }
};

const handleReset = () => {
  if (!formRef.value) return;

  formRef.value.resetFields();

  if (props.tag) {
    formData.name = props.tag.name;
    formData.color = props.tag.color;
  } else {
    formData.color = '#1890ff';
  }
};
</script>

<style scoped>
.ml-2 {
  margin-left: 0.5rem;
}
</style>