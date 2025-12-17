<template>
  <div class="tag-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>标签管理</h2>
          <span class="text-sm text-gray-500">
            管理博客标签，包括创建、编辑和删除操作
          </span>
        </div>
      </template>

      <TagList
        ref="tagListRef"
        :show-create="true"
        :show-actions="true"
        :auto-load="true"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import TagList from '@/components/tag/TagList.vue';

const tagListRef = ref();
</script>

<style scoped>
.tag-management {
  padding: 20px;
}

.card-header h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
}

.text-sm {
  font-size: 14px;
}

.text-gray-500 {
  color: #6b7280;
}
</style>