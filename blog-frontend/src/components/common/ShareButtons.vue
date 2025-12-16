<template>
  <div class="share-buttons">
    <button
      @click="showShareModal = true"
      class="px-4 py-2 bg-gray-100 text-gray-600 rounded-lg hover:bg-gray-200 transition-colors font-medium"
    >
      <i class="fas fa-share-alt mr-2"></i>
      分享
    </button>

    <!-- 分享弹窗 -->
    <div
      v-if="showShareModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showShareModal = false"
    >
      <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">分享文章</h3>
          <button
            @click="showShareModal = false"
            class="text-gray-400 hover:text-gray-600"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <!-- 社交媒体分享 -->
        <div class="grid grid-cols-4 gap-4 mb-6">
          <!-- 微信 -->
          <div
            @click="shareToWechat"
            class="flex flex-col items-center gap-2 cursor-pointer hover:bg-gray-50 p-3 rounded-lg transition-colors"
          >
            <div class="w-12 h-12 bg-green-500 rounded-full flex items-center justify-center">
              <i class="fab fa-weixin text-white text-xl"></i>
            </div>
            <span class="text-xs text-gray-600">微信</span>
          </div>

          <!-- 微博 -->
          <div
            @click="shareToWeibo"
            class="flex flex-col items-center gap-2 cursor-pointer hover:bg-gray-50 p-3 rounded-lg transition-colors"
          >
            <div class="w-12 h-12 bg-red-500 rounded-full flex items-center justify-center">
              <i class="fab fa-weibo text-white text-xl"></i>
            </div>
            <span class="text-xs text-gray-600">微博</span>
          </div>

          <!-- QQ -->
          <div
            @click="shareToQQ"
            class="flex flex-col items-center gap-2 cursor-pointer hover:bg-gray-50 p-3 rounded-lg transition-colors"
          >
            <div class="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
              <i class="fab fa-qq text-white text-xl"></i>
            </div>
            <span class="text-xs text-gray-600">QQ</span>
          </div>

          <!-- 复制链接 -->
          <div
            @click="copyLink"
            class="flex flex-col items-center gap-2 cursor-pointer hover:bg-gray-50 p-3 rounded-lg transition-colors"
          >
            <div class="w-12 h-12 bg-gray-500 rounded-full flex items-center justify-center">
              <i class="fas fa-link text-white text-xl"></i>
            </div>
            <span class="text-xs text-gray-600">复制链接</span>
          </div>
        </div>

        <!-- 链接复制 -->
        <div class="border-t pt-4">
          <div class="flex items-center gap-2">
            <input
              ref="linkInput"
              :value="shareUrl"
              readonly
              class="flex-1 px-3 py-2 border rounded-lg bg-gray-50 text-sm"
            />
            <button
              @click="copyLink"
              :class="[
                'px-4 py-2 rounded-lg font-medium transition-colors',
                copySuccess
                  ? 'bg-green-500 text-white'
                  : 'bg-blue-500 text-white hover:bg-blue-600'
              ]"
            >
              <i :class="copySuccess ? 'fas fa-check' : 'fas fa-copy'" class="mr-2"></i>
              {{ copySuccess ? '已复制' : '复制' }}
            </button>
          </div>
        </div>

        <!-- 二维码 -->
        <div class="border-t pt-4 mt-4">
          <h4 class="text-sm font-medium mb-3 text-gray-700">扫码分享</h4>
          <div class="flex justify-center">
            <div class="bg-white p-4 border rounded-lg">
              <!-- 这里应该集成二维码生成库，如 qrcode -->
              <div class="w-32 h-32 bg-gray-100 flex items-center justify-center">
                <i class="fas fa-qrcode text-4xl text-gray-400"></i>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 微信二维码弹窗 -->
    <div
      v-if="showWechatQR"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click.self="showWechatQR = false"
    >
      <div class="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold">分享到微信</h3>
          <button
            @click="showWechatQR = false"
            class="text-gray-400 hover:text-gray-600"
          >
            <i class="fas fa-times"></i>
          </button>
        </div>

        <div class="text-center">
          <div class="w-48 h-48 bg-gray-100 mx-auto mb-4 flex items-center justify-center">
            <!-- 这里应该生成微信分享二维码 -->
            <i class="fas fa-qrcode text-6xl text-gray-400"></i>
          </div>
          <p class="text-sm text-gray-600">
            使用微信扫描二维码分享
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

interface Props {
  title: string
  url: string
  description?: string
}

defineProps<Props>()

const showShareModal = ref(false)
const showWechatQR = ref(false)
const copySuccess = ref(false)
const linkInput = ref<HTMLInputElement>()

const shareUrl = ref(window.location.href)

// 分享到微信
const shareToWechat = () => {
  showWechatQR.value = true
}

// 分享到微博
const shareToWeibo = () => {
  const text = encodeURIComponent(`${title} - ${description}`)
  const url = encodeURIComponent(shareUrl.value)
  window.open(
    `https://service.weibo.com/share/share.php?title=${text}&url=${url}`,
    '_blank',
    'width=600,height=400'
  )
}

// 分享到QQ
const shareToQQ = () => {
  const title = encodeURIComponent(title)
  const url = encodeURIComponent(shareUrl.value)
  window.open(
    `https://connect.qq.com/widget/shareqq/index.html?title=${title}&url=${url}`,
    '_blank',
    'width=600,height=400'
  )
}

// 复制链接
const copyLink = async () => {
  try {
    if (linkInput.value) {
      linkInput.value.select()
      await navigator.clipboard.writeText(shareUrl.value)
      copySuccess.value = true
      setTimeout(() => {
        copySuccess.value = false
      }, 2000)
    }
  } catch (error) {
    console.error('复制失败:', error)
  }
}

// 获取分享标题（需要从props中获取）
const title = '文章标题'
const description = '文章描述'
</script>

<style scoped>
.share-buttons button {
  @apply flex items-center;
}
</style>