<template>
  <div class="search-box">
    <div class="search-input-wrapper">
      <el-input
        v-model="searchQuery"
        :placeholder="placeholder"
        :prefix-icon="Search"
        clearable
        size="large"
        @input="handleInput"
        @keyup.enter="handleSearch"
        @focus="handleFocus"
        @blur="handleBlur"
      />
      <el-button
        type="primary"
        size="large"
        :icon="Search"
        @click="handleSearch"
        :loading="loading"
      >
        搜索
      </el-button>
    </div>

    <!-- 搜索建议 -->
    <div
      v-if="showSuggestions && suggestions.length > 0"
      class="search-suggestions"
    >
      <div
        v-for="(suggestion, index) in suggestions"
        :key="index"
        class="suggestion-item"
        @click="selectSuggestion(suggestion)"
      >
        <el-icon><Search /></el-icon>
        <span v-html="highlightSuggestion(suggestion)"></span>
      </div>
    </div>

    <!-- 搜索历史 -->
    <div
      v-if="showHistory && searchHistory.length > 0"
      class="search-history"
    >
      <div class="history-header">
        <span>搜索历史</span>
        <el-button
          type="text"
          size="small"
          @click="clearHistory"
        >
          清空
        </el-button>
      </div>
      <div class="history-list">
        <el-tag
          v-for="(item, index) in searchHistory"
          :key="item.id || index"
          class="history-tag"
          @click="selectHistory(item.keyword)"
          closable
          @close="removeHistory(item.id)"
        >
          {{ item.keyword }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { searchArticles, getSearchSuggestions } from '@/api/search'
import { useSearchStore } from '@/stores/searchStore'
import type { SearchHistory } from 'blog-shared'

interface Props {
  placeholder?: string
  showHistory?: boolean
  showSuggestions?: boolean
  maxSuggestions?: number
  modelValue?: string
}

interface Emits {
  (e: 'search', query: string): void
  (e: 'suggestion-select', suggestion: string): void
  (e: 'update:modelValue', value: string): void
}

const props = withDefaults(defineProps<Props>(), {
  placeholder: '请输入搜索关键词...',
  showHistory: true,
  showSuggestions: true,
  maxSuggestions: 10
})

const emit = defineEmits<Emits>()

const searchStore = useSearchStore()
const searchQuery = ref(props.modelValue || '')
const suggestions = ref<string[]>([])
const loading = ref(false)
const showSuggestions = ref(false)
const isFocused = ref(false)

// 计算属性
const searchHistory = computed(() => searchStore.recentHistory)

// 监听外部值变化
watch(() => props.modelValue, (newValue) => {
  if (newValue !== undefined) {
    searchQuery.value = newValue
  }
})

// 防抖处理
let debounceTimer: NodeJS.Timeout | null = null
const handleInput = (value: string) => {
  emit('update:modelValue', value)

  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  if (value.trim().length >= 2 && props.showSuggestions) {
    debounceTimer = setTimeout(() => {
      fetchSuggestions(value)
    }, 300)
  } else {
    suggestions.value = []
    showSuggestions.value = false
  }
}

// 获取搜索建议
const fetchSuggestions = async (keyword: string) => {
  try {
    const data = await getSearchSuggestions(keyword, props.maxSuggestions)
    suggestions.value = data
    showSuggestions.value = true
  } catch (error) {
    console.error('Failed to fetch search suggestions:', error)
  }
}

// 高亮搜索建议
const highlightSuggestion = (suggestion: string) => {
  const regex = new RegExp(`(${searchQuery.value})`, 'gi')
  return suggestion.replace(regex, '<mark>$1</mark>')
}

// 执行搜索
const handleSearch = () => {
  const query = searchQuery.value.trim()
  if (!query) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  if (query.length < 2) {
    ElMessage.warning('搜索关键词至少需要2个字符')
    return
  }

  showSuggestions.value = false
  emit('search', query)
}

// 选择搜索建议
const selectSuggestion = (suggestion: string) => {
  searchQuery.value = suggestion
  emit('update:modelValue', suggestion)
  emit('suggestion-select', suggestion)
  showSuggestions.value = false
  handleSearch()
}

// 选择搜索历史
const selectHistory = (keyword: string) => {
  searchQuery.value = keyword
  emit('update:modelValue', keyword)
  showSuggestions.value = false
  handleSearch()
}

// 焦点事件处理
const handleFocus = () => {
  isFocused.value = true
  if (props.showHistory && searchHistory.value.length > 0) {
    // 这里可以显示搜索历史
  }
}

const handleBlur = () => {
  isFocused.value = false
  setTimeout(() => {
    showSuggestions.value = false
  }, 200) // 延迟隐藏，以便点击建议项
}

// 清空搜索历史
const clearHistory = async () => {
  try {
    await searchStore.clearHistory()
    ElMessage.success('搜索历史已清空')
  } catch (error) {
    ElMessage.error('清空搜索历史失败')
  }
}

// 移除单个搜索历史
const removeHistory = async (id: number) => {
  try {
    await searchStore.removeHistory(id)
  } catch (error) {
    ElMessage.error('删除搜索历史失败')
  }
}
</script>

<style scoped>
.search-box {
  position: relative;
  width: 100%;
  max-width: 600px;
}

.search-input-wrapper {
  display: flex;
  gap: 8px;
}

.search-input-wrapper :deep(.el-input__wrapper) {
  border-radius: 8px 0 0 8px;
}

.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 300px;
  overflow-y: auto;
  margin-top: 4px;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.suggestion-item:hover {
  background-color: var(--el-fill-color-light);
}

.suggestion-item :deep(mark) {
  background-color: #ffeb3b;
  color: inherit;
  font-weight: 600;
}

.search-history {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 999;
  padding: 16px;
  margin-top: 4px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--el-text-color-regular);
}

.history-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.history-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.history-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>