<template>
  <div class="toc-container sticky top-4">
    <h3 class="text-lg font-semibold mb-4 flex items-center">
      <i class="fas fa-list-ul mr-2 text-blue-500"></i>
      文章目录
    </h3>

    <!-- 目录内容为空时显示 -->
    <div v-if="headings.length === 0" class="text-gray-500 text-sm">
      暂无目录
    </div>

    <!-- 目录列表 -->
    <nav v-else class="space-y-1">
      <a
        v-for="heading in headings"
        :key="heading.id"
        :href="`#${heading.id}`"
        @click="scrollToHeading(heading)"
        :class="[
          'block px-3 py-2 text-sm rounded-lg transition-all hover:bg-gray-100',
          'border-l-2 border-transparent hover:border-blue-500',
          activeHeading === heading.id
            ? 'bg-blue-50 text-blue-600 border-blue-500 font-medium'
            : 'text-gray-600'
        ]"
        :style="{ paddingLeft: `${(heading.level - 1) * 12 + 12}px` }"
      >
        {{ heading.text }}
      </a>
    </nav>

    <!-- 返回顶部按钮 -->
    <button
      v-if="showBackToTop"
      @click="scrollToTop"
      class="mt-4 w-full px-4 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition-colors flex items-center justify-center"
    >
      <i class="fas fa-arrow-up mr-2"></i>
      返回顶部
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'

interface Heading {
  id: string
  text: string
  level: number
  element: HTMLElement
}

interface Props {
  headings: Heading[]
}

defineProps<Props>()

const activeHeading = ref<string>('')
const showBackToTop = ref(false)

// 滚动到指定标题
const scrollToHeading = (heading: Heading) => {
  const element = document.getElementById(heading.id)
  if (element) {
    const offset = 80 // 导航栏高度
    const elementPosition = element.getBoundingClientRect().top
    const offsetPosition = elementPosition + window.pageYOffset - offset

    window.scrollTo({
      top: offsetPosition,
      behavior: 'smooth'
    })
  }
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

// 检查标题是否在视口中
const isElementInViewport = (element: HTMLElement) => {
  const rect = element.getBoundingClientRect()
  const offset = 100 // 提前激活的偏移量

  return rect.top <= offset && rect.bottom > offset
}

// 更新活跃标题
const updateActiveHeading = () => {
  const headings = Array.from(document.querySelectorAll('.article-content h1, .article-content h2, .article-content h3, .article-content h4, .article-content h5, .article-content h6'))

  let currentHeading = ''
  for (let i = headings.length - 1; i >= 0; i--) {
    const heading = headings[i] as HTMLElement
    if (isElementInViewport(heading)) {
      currentHeading = heading.id
      break
    }
  }

  activeHeading.value = currentHeading

  // 更新返回顶部按钮显示状态
  showBackToTop.value = window.pageYOffset > 300
}

// 滚动事件监听
const handleScroll = () => {
  updateActiveHeading()
}

// 生命周期
onMounted(() => {
  window.addEventListener('scroll', handleScroll)
  // 初始检查
  setTimeout(updateActiveHeading, 100)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.toc-container {
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

/* 滚动条样式 */
.toc-container::-webkit-scrollbar {
  width: 4px;
}

.toc-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.toc-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.toc-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>