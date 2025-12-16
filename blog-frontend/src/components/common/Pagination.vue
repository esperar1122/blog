<template>
  <div class="flex items-center justify-between">
    <!-- 页面信息 -->
    <div class="text-sm text-gray-700">
      显示第
      <span class="font-medium">{{ startItem }}</span>
      至
      <span class="font-medium">{{ endItem }}</span>
      条，共
      <span class="font-medium">{{ totalItems }}</span>
      条记录
    </div>

    <!-- 分页按钮 -->
    <div class="flex items-center gap-2">
      <!-- 上一页 -->
      <button
        :disabled="currentPage === 1"
        @click="$emit('page-change', currentPage - 1)"
        :class="[
          'px-3 py-2 rounded-md text-sm font-medium transition-colors',
          currentPage === 1
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-white border border-gray-300 text-gray-500 hover:bg-gray-50'
        ]"
      >
        <i class="fas fa-chevron-left"></i>
      </button>

      <!-- 页码 -->
      <div class="flex items-center gap-1">
        <!-- 第一页 -->
        <button
          v-if="showFirstPage"
          @click="$emit('page-change', 1)"
          :class="[
            'px-3 py-2 rounded-md text-sm font-medium transition-colors',
            currentPage === 1
              ? 'bg-blue-500 text-white'
              : 'bg-white border border-gray-300 text-gray-500 hover:bg-gray-50'
          ]"
        >
          1
        </button>

        <!-- 省略号 -->
        <span
          v-if="showStartEllipsis"
          class="px-3 py-2 text-gray-500"
        >
          ...
        </span>

        <!-- 中间页码 -->
        <button
          v-for="page in visiblePages"
          :key="page"
          @click="$emit('page-change', page)"
          :class="[
            'px-3 py-2 rounded-md text-sm font-medium transition-colors',
            currentPage === page
              ? 'bg-blue-500 text-white'
              : 'bg-white border border-gray-300 text-gray-500 hover:bg-gray-50'
          ]"
        >
          {{ page }}
        </button>

        <!-- 省略号 -->
        <span
          v-if="showEndEllipsis"
          class="px-3 py-2 text-gray-500"
        >
          ...
        </span>

        <!-- 最后一页 -->
        <button
          v-if="showLastPage"
          @click="$emit('page-change', totalPages)"
          :class="[
            'px-3 py-2 rounded-md text-sm font-medium transition-colors',
            currentPage === totalPages
              ? 'bg-blue-500 text-white'
              : 'bg-white border border-gray-300 text-gray-500 hover:bg-gray-50'
          ]"
        >
          {{ totalPages }}
        </button>
      </div>

      <!-- 下一页 -->
      <button
        :disabled="currentPage === totalPages"
        @click="$emit('page-change', currentPage + 1)"
        :class="[
          'px-3 py-2 rounded-md text-sm font-medium transition-colors',
          currentPage === totalPages
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-white border border-gray-300 text-gray-500 hover:bg-gray-50'
        ]"
      >
        <i class="fas fa-chevron-right"></i>
      </button>

      <!-- 跳转输入框 -->
      <div class="flex items-center gap-2 ml-4">
        <span class="text-sm text-gray-500">跳转至</span>
        <input
          v-model.number="jumpPage"
          @keyup.enter="handleJump"
          type="number"
          :min="1"
          :max="totalPages"
          class="w-16 px-2 py-1 border border-gray-300 rounded text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
        />
        <span class="text-sm text-gray-500">页</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  currentPage: number
  totalPages: number
  totalItems?: number
  itemsPerPage?: number
}

interface Emits {
  (e: 'page-change', page: number): void
}

const props = withDefaults(defineProps<Props>(), {
  totalItems: 0,
  itemsPerPage: 10
})

const emit = defineEmits<Emits>()

const jumpPage = ref<number>()

// 计算显示的页码范围
const visiblePages = computed(() => {
  const current = props.currentPage
  const total = props.totalPages
  const range = 5 // 显示的页码数量

  if (total <= range) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }

  let start = Math.max(1, current - Math.floor(range / 2))
  let end = Math.min(total, start + range - 1)

  if (end - start < range - 1) {
    start = Math.max(1, end - range + 1)
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// 是否显示第一页
const showFirstPage = computed(() => {
  return props.totalPages > 1 && !visiblePages.value.includes(1)
})

// 是否显示最后一页
const showLastPage = computed(() => {
  return props.totalPages > 1 && !visiblePages.value.includes(props.totalPages)
})

// 是否显示开始省略号
const showStartEllipsis = computed(() => {
  return showFirstPage.value && visiblePages.value[0] > 2
})

// 是否显示结束省略号
const showEndEllipsis = computed(() => {
  return showLastPage.value && visiblePages.value[visiblePages.value.length - 1] < props.totalPages - 1
})

// 计算当前显示的项目范围
const startItem = computed(() => {
  if (props.totalItems === 0) return 0
  return (props.currentPage - 1) * props.itemsPerPage + 1
})

const endItem = computed(() => {
  if (props.totalItems === 0) return 0
  return Math.min(props.currentPage * props.itemsPerPage, props.totalItems)
})

const handleJump = () => {
  if (jumpPage.value && jumpPage.value >= 1 && jumpPage.value <= props.totalPages) {
    emit('page-change', jumpPage.value)
    jumpPage.value = undefined
  }
}
</script>

<style scoped>
input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
}
</style>