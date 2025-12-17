<template>
  <div class="system-settings">
    <el-card>
      <template #header>
        <div class="card-header">
          <h3>系统设置</h3>
          <el-button type="primary" @click="handleSaveAll" :loading="saving">
            保存所有设置
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="站点设置" name="site">
          <div class="settings-group">
            <h4>基本信息</h4>
            <el-form :model="settings.site" label-width="120px">
              <el-form-item label="网站标题">
                <el-input
                  v-model="settings.site['site.title']"
                  placeholder="请输入网站标题"
                  maxlength="50"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="网站描述">
                <el-input
                  v-model="settings.site['site.description']"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入网站描述"
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="关键词">
                <el-input
                  v-model="settings.site['site.keywords']"
                  placeholder="请输入网站关键词，用逗号分隔"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>

              <el-form-item label="站长">
                <el-input
                  v-model="settings.site['site.author']"
                  placeholder="请输入站长姓名"
                  maxlength="20"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="评论设置" name="comment">
          <div class="settings-group">
            <h4>评论功能</h4>
            <el-form :model="settings.comment" label-width="120px">
              <el-form-item label="启用评论">
                <el-switch
                  v-model="settings.comment['comment.enabled']"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>

              <el-form-item label="需要登录">
                <el-switch
                  v-model="settings.comment['comment.require_login']"
                  active-text="需要"
                  inactive-text="不需要"
                />
              </el-form-item>

              <el-form-item label="自动审核">
                <el-switch
                  v-model="settings.comment['comment.auto_approve']"
                  active-text="自动通过"
                  inactive-text="需要审核"
                />
              </el-form-item>

              <el-form-item label="评论长度限制">
                <el-input-number
                  v-model="settings.comment['comment.max_length']"
                  :min="10"
                  :max="10000"
                  :step="10"
                />
                <span class="input-suffix">字符</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="文章设置" name="article">
          <div class="settings-group">
            <h4>文章功能</h4>
            <el-form :model="settings.article" label-width="120px">
              <el-form-item label="每页显示">
                <el-input-number
                  v-model="settings.article['article.per_page']"
                  :min="5"
                  :max="50"
                  :step="5"
                />
                <span class="input-suffix">篇</span>
              </el-form-item>

              <el-form-item label="允许评论">
                <el-switch
                  v-model="settings.article['article.allow_comment']"
                  active-text="允许"
                  inactive-text="不允许"
                />
              </el-form-item>

              <el-form-item label="自动保存">
                <el-switch
                  v-model="settings.article['article.auto_save']"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="上传设置" name="upload">
          <div class="settings-group">
            <h4>文件上传</h4>
            <el-form :model="settings.upload" label-width="120px">
              <el-form-item label="文件大小限制">
                <el-input-number
                  v-model="settings.upload['upload.max_file_size']"
                  :min="1024"
                  :max="10485760"
                  :step="1024"
                />
                <span class="input-suffix">字节</span>
                <div class="form-tip">
                  当前限制: {{ formatFileSize(settings.upload['upload.max_file_size']) }}
                </div>
              </el-form-item>

              <el-form-item label="允许文件类型">
                <el-input
                  v-model="settings.upload['upload.allowed_types']"
                  placeholder="jpg,jpeg,png,gif,pdf,doc,docx"
                />
                <div class="form-tip">
                  用逗号分隔文件扩展名，不区分大小写
                </div>
              </el-form-item>

              <el-form-item label="图片最大宽度">
                <el-input-number
                  v-model="settings.upload['upload.image_max_width']"
                  :min="100"
                  :max="4000"
                  :step="100"
                />
                <span class="input-suffix">像素</span>
              </el-form-item>

              <el-form-item label="图片最大高度">
                <el-input-number
                  v-model="settings.upload['upload.image_max_height']"
                  :min="100"
                  :max="4000"
                  :step="100"
                />
                <span class="input-suffix">像素</span>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <div class="settings-group">
            <h4>登录安全</h4>
            <el-form :model="settings.security" label-width="120px">
              <el-form-item label="登录尝试次数">
                <el-input-number
                  v-model="settings.security['security.login_attempts']"
                  :min="3"
                  :max="10"
                  :step="1"
                />
                <span class="input-suffix">次</span>
              </el-form-item>

              <el-form-item label="锁定时长">
                <el-input-number
                  v-model="settings.security['security.lockout_duration']"
                  :min="300"
                  :max="7200"
                  :step="300"
                />
                <span class="input-suffix">秒</span>
                <div class="form-tip">
                  {{ formatDuration(settings.security['security.lockout_duration']) }}
                </div>
              </el-form-item>

              <el-form-item label="会话超时">
                <el-input-number
                  v-model="settings.security['security.session_timeout']"
                  :min="600"
                  :max="86400"
                  :step="600"
                />
                <span class="input-suffix">秒</span>
                <div class="form-tip">
                  {{ formatDuration(settings.security['security.session_timeout']) }}
                </div>
              </el-form-item>
            </el-form>
          </div>

          <div class="settings-group">
            <h4>敏感词过滤</h4>
            <el-form :model="settings.sensitive" label-width="120px">
              <el-form-item label="启用敏感词">
                <el-switch
                  v-model="settings.sensitive['sensitive_word.enabled']"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>

              <el-form-item label="替换字符">
                <el-input
                  v-model="settings.sensitive['sensitive_word.replace_with']"
                  placeholder="***"
                  maxlength="10"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane label="缓存设置" name="cache">
          <div class="settings-group">
            <h4>缓存配置</h4>
            <el-form :model="settings.cache" label-width="120px">
              <el-form-item label="启用缓存">
                <el-switch
                  v-model="settings.cache['cache.enabled']"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>

              <el-form-item label="缓存时间">
                <el-input-number
                  v-model="settings.cache['cache.ttl']"
                  :min="60"
                  :max="3600"
                  :step="60"
                />
                <span class="input-suffix">秒</span>
                <div class="form-tip">
                  {{ formatDuration(settings.cache['cache.ttl']) }}
                </div>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import adminContentService from '@/services/adminContentService'

interface SettingsGroup {
  [key: string]: string | boolean | number
}

interface Settings {
  site: SettingsGroup
  comment: SettingsGroup
  article: SettingsGroup
  upload: SettingsGroup
  security: SettingsGroup
  sensitive: SettingsGroup
  cache: SettingsGroup
}

const activeTab = ref('site')
const saving = ref(false)

const settings = reactive<Settings>({
  site: {},
  comment: {},
  article: {},
  upload: {},
  security: {},
  sensitive: {},
  cache: {}
})

onMounted(() => {
  loadSettings()
})

const loadSettings = async () => {
  try {
    const response = await adminContentService.getAllSystemSettings()
    const allSettings = response.data

    Object.keys(settings).forEach(group => {
      settings[group as keyof Settings] = {}
    })

    allSettings.forEach((item: any) => {
      const key = item.settingKey
      const value = parseValue(item.settingValue, item.type)

      if (key.startsWith('site.')) {
        settings.site[key] = value
      } else if (key.startsWith('comment.')) {
        settings.comment[key] = value
      } else if (key.startsWith('article.')) {
        settings.article[key] = value
      } else if (key.startsWith('upload.')) {
        settings.upload[key] = value
      } else if (key.startsWith('security.')) {
        settings.security[key] = value
      } else if (key.startsWith('sensitive_word.')) {
        settings.sensitive[key] = value
      } else if (key.startsWith('cache.')) {
        settings.cache[key] = value
      }
    })

    const defaultSettings = getDefaultSettings()
    Object.keys(defaultSettings).forEach(group => {
      const groupKey = group as keyof Settings
      const groupSettings = defaultSettings[groupKey] as SettingsGroup
      Object.keys(groupSettings).forEach(key => {
        if (settings[groupKey][key] === undefined) {
          settings[groupKey][key] = groupSettings[key]
        }
      })
    })

  } catch (error) {
    ElMessage.error('加载设置失败')
    console.error('加载系统设置错误:', error)
  }
}

const handleSaveAll = async () => {
  try {
    await ElMessageBox.confirm('确定要保存所有设置吗？', '确认保存', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    saving.value = true

    const allSettings: Record<string, string> = {}

    Object.keys(settings).forEach(group => {
      const groupKey = group as keyof Settings
      const groupSettings = settings[groupKey]
      Object.keys(groupSettings).forEach(key => {
        const value = groupSettings[key]
        allSettings[key] = String(value)
      })
    })

    await adminContentService.batchUpdateSystemSettings(allSettings)

    ElMessage.success('设置保存成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('保存设置失败')
      console.error('保存设置错误:', error)
    }
  } finally {
    saving.value = false
  }
}

const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
}

const parseValue = (value: string, type: string): string | boolean | number => {
  if (!value) return ''

  switch (type) {
    case 'BOOLEAN':
      return value === 'true'
    case 'NUMBER':
      return Number(value)
    case 'JSON':
      try {
        return JSON.parse(value)
      } catch {
        return value
      }
    default:
      return value
  }
}

const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatDuration = (seconds: number): string => {
  if (seconds < 60) return `${seconds}秒`
  if (seconds < 3600) return `${Math.floor(seconds / 60)}分钟`
  return `${Math.floor(seconds / 3600)}小时${Math.floor((seconds % 3600) / 60)}分钟`
}

const getDefaultSettings = (): Settings => ({
  site: {
    'site.title': '博客系统',
    'site.description': '一个简单的博客管理系统',
    'site.keywords': '博客,文章,评论',
    'site.author': '管理员'
  },
  comment: {
    'comment.enabled': true,
    'comment.require_login': true,
    'comment.auto_approve': false,
    'comment.max_length': 1000
  },
  article: {
    'article.per_page': 10,
    'article.allow_comment': true,
    'article.auto_save': true
  },
  upload: {
    'upload.max_file_size': 5242880,
    'upload.allowed_types': 'jpg,jpeg,png,gif,pdf,doc,docx',
    'upload.image_max_width': 1920,
    'upload.image_max_height': 1080
  },
  security: {
    'security.login_attempts': 5,
    'security.lockout_duration': 1800,
    'security.session_timeout': 3600
  },
  sensitive: {
    'sensitive_word.enabled': true,
    'sensitive_word.replace_with': '***'
  },
  cache: {
    'cache.enabled': true,
    'cache.ttl': 300
  }
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.settings-group {
  margin-bottom: 30px;
}

.settings-group h4 {
  margin: 0 0 20px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
  color: #303133;
}

.input-suffix {
  margin-left: 8px;
  color: #909399;
  font-size: 14px;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}

:deep(.el-tabs__content) {
  padding-top: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 22px;
}

:deep(.el-input-number) {
  width: 200px;
}

:deep(.el-input-number .el-input__inner) {
  text-align: left;
}
</style>