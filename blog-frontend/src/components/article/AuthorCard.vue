<template>
  <div class="author-card bg-white rounded-lg border p-6">
    <h3 class="text-lg font-semibold mb-4">作者信息</h3>

    <div class="flex items-center gap-4 mb-4">
      <img
        :src="author.avatar || '/default-avatar.png'"
        :alt="author.nickname"
        class="w-16 h-16 rounded-full"
      />
      <div class="flex-1">
        <h4 class="font-medium text-gray-900">{{ author.nickname }}</h4>
        <p class="text-sm text-gray-500">{{ author.username }}</p>
      </div>
    </div>

    <p v-if="author.bio" class="text-gray-600 text-sm mb-4">
      {{ author.bio }}
    </p>

    <div class="flex gap-2">
      <button
        @click="handleFollow"
        :class="[
          'flex-1 px-4 py-2 rounded-lg font-medium transition-colors',
          isFollowing
            ? 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            : 'bg-blue-500 text-white hover:bg-blue-600'
        ]"
      >
        <i :class="isFollowing ? 'fas fa-user-check' : 'fas fa-user-plus'" class="mr-2"></i>
        {{ isFollowing ? '已关注' : '关注' }}
      </button>
      <button
        @click="handleMessage"
        class="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition-colors"
      >
        <i class="fas fa-envelope"></i>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { UserSummary } from '@/types/article'

interface Props {
  author: UserSummary
}

interface Emits {
  (e: 'follow', authorId: number): void
  (e: 'unfollow', authorId: number): void
  (e: 'message', authorId: number): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const isFollowing = ref(false)

const handleFollow = () => {
  if (isFollowing.value) {
    emit('unfollow', props.author.id)
  } else {
    emit('follow', props.author.id)
  }
  isFollowing.value = !isFollowing.value
}

const handleMessage = () => {
  emit('message', props.author.id)
}
</script>