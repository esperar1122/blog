<template>
  <div class="relative">
    <!-- 搜索输入框 -->
    <div class="relative">
      <input
        v-model="searchQuery"
        @input="handleInput"
        @keyup.enter="handleSearch"
        @focus="showSuggestions = true"
        @blur="hideSuggestions"
        type="text"
        :placeholder="placeholder"
        class="w-full px-4 py-2 pl-10 pr-4 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 transition-all"
        :class="{ 'border-blue-500': isFocused }"
      />
      <i class="fas fa-search absolute left-3 top-3 text-gray-400"></i>

      <!-- 清空按钮 -->
      <button
        v-if="searchQuery"
        @click="clearSearch"
        class="absolute right-3 top-3 text-gray-400 hover:text-gray-600 transition-colors"
      >
        <i class="fas fa-times"></i>
      </button>
    </div>

    <!-- 搜索建议 -->
    <div
      v-if="showSuggestions && (suggestions.length > 0 || popularSearches.length > 0)"
      class="absolute top-full left-0 right-0 mt-1 bg-white border rounded-lg shadow-lg z-50"
    >
      <!-- 搜索建议列表 -->
      <div v-if="suggestions.length > 0" class="py-2">
        <div class="px-4 py-2 text-xs text-gray-500 border-b">搜索建议</div>
        <div
          v-for="(suggestion, index) in suggestions"
          :key="index"
          @mousedown="selectSuggestion(suggestion)"
          class="px-4 py-2 hover:bg-gray-50 cursor-pointer flex items-center gap-2"
        >
          <i class="fas fa-history text-gray-400 text-sm"></i>
          <span class="flex-1">{{ suggestion }}</span>
        </div>
      </div>

      <!-- 热门搜索 -->
      <div v-if="popularSearches.length > 0 && suggestions.length === 0" class="py-2">
        <div class="px-4 py-2 text-xs text-gray-500 border-b">热门搜索</div>
        <div class="px-4 py-2 flex flex-wrap gap-2">
          <span
            v-for="(tag, index) in popularSearches"
            :key="index"
            @mousedown="selectSuggestion(tag)"
            class="px-3 py-1 bg-blue-100 text-blue-600 text-sm rounded-full cursor-pointer hover:bg-blue-200 transition-colors"
          >
            {{ tag }}
          </span>
        </div>
      </div>
    </div>

    <!-- 搜索历史（移动端） -->
    <div
      v-if="showMobileHistory && searchHistory.length > 0"
      class="absolute top-full left-0 right-0 mt-1 bg-white border rounded-lg shadow-lg z-50"
    >
      <div class="py-2">
        <div class="px-4 py-2 text-xs text-gray-500 border-b flex justify-between items-center">
          <span>搜索历史</span>
          <button
            @click="clearHistory"
            class="text-blue-500 hover:text-blue-700 transition-colors"
          >
            清空
          </button>
        </div>
        <div
          v-for="(item, index) in searchHistory"
          :key="index"
          @mousedown="selectSuggestion(item)"
          class="px-4 py-2 hover:bg-gray-50 cursor-pointer flex items-center gap-2"
        >
          <i class="fas fa-clock text-gray-400 text-sm"></i>
          <span class="flex-1">{{ item }}</span>
          <button
            @mousedown.stop="removeFromHistory(item)"
            class="text-gray-400 hover:text-gray-600"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'

interface Props {
  placeholder?: string
  popularSearches?: string[]
  maxSuggestions?: number
  debounceTime?: number
}

interface Emits {
  (e: 'search', query: string): void
  (e: 'suggestion-select', suggestion: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '搜索文章、标签...',
  popularSearches: () => [],
  maxSuggestions: 8,
  debounceTime: 300
})

const emit = defineEmits<Emits>()

const searchQuery = ref('')
const suggestions = ref<string[]>([])
const searchHistory = ref<string[]>([])
const showSuggestions = ref(false)
const showMobileHistory = ref(false)
const isFocused = ref(false)
const debounceTimer = ref<number>()

// 从本地存储加载搜索历史
const loadSearchHistory = () => {
  const history = localStorage.getItem('search-history')
  if (history) {
    searchHistory.value = JSON.parse(history)
  }
}

// 保存搜索历史到本地存储
const saveSearchHistory = (query: string) => {
  const history = searchHistory.value.filter(item => item !== query)
  history.unshift(query)

  // 只保留最近20条记录
  if (history.length > 20) {
    history.length = 20
  }

  searchHistory.value = history
  localStorage.setItem('search-history', JSON.stringify(history))
}

// 处理输入
const handleInput = () => {
  isFocused.value = true

  // 清除之前的防抖定时器
  if (debounceTimer.value) {
    clearTimeout(debounceTimer.value)
  }

  if (searchQuery.value.trim()) {
    // 防抖处理搜索建议
    debounceTimer.value = setTimeout(() => {
      generateSuggestions()
    }, props.debounceTime)
  } else {
    suggestions.value = []
  }
}

// 生成搜索建议
const generateSuggestions = () => {
  const query = searchQuery.value.toLowerCase()
  const allSuggestions = [...searchHistory.value, ...props.popularSearches]

  suggestions.value = allSuggestions
    .filter(item => item.toLowerCase().includes(query))
    .slice(0, props.maxSuggestions)
}

// 处理搜索
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    saveSearchHistory(searchQuery.value.trim())
    emit('search', searchQuery.value.trim())
    showSuggestions.value = false
  }
}

// 选择建议
const selectSuggestion = (suggestion: string) => {
  searchQuery.value = suggestion
  saveSearchHistory(suggestion)
  emit('suggestion-select', suggestion)
  showSuggestions.value = false
  emit('search', suggestion)
}

// 清空搜索
const clearSearch = () => {
  searchQuery.value = ''
  suggestions.value = []
  emit('search', '')
}

// 清空搜索历史
const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('search-history')
  showMobileHistory.value = false
}

// 从搜索历史中移除
const removeFromHistory = (item: string) => {
  const index = searchHistory.value.indexOf(item)
  if (index > -1) {
    searchHistory.value.splice(index, 1)
    localStorage.setItem('search-history', JSON.stringify(searchHistory.value))
  }
}

// 隐藏建议
const hideSuggestions = () => {
  setTimeout(() => {
    showSuggestions.value = false
    isFocused.value = false
  }, 200)
}

// 监听点击外部
document.addEventListener('click', (e) => {
  if (!(e.target as Element).closest('.relative')) {
    showSuggestions.value = false
    isFocused.value = false
  }
})

// 初始化
loadSearchHistory()

// 监听搜索查询变化
watch(searchQuery, (newQuery) => {
  if (!newQuery.trim()) {
    suggestions.value = []
    emit('search', '')
  }
})
</script>