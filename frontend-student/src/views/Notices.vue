<template>
  <div class="notices-page">
    <div class="page-header">
      <el-icon class="page-icon" :size="22"><Bell /></el-icon>
      <h2>通知公告</h2>
    </div>

    <div class="notices-layout" v-loading="loading">
      <!-- Left: List -->
      <div class="notice-list-panel">
        <div class="list-filter">
          <span
            v-for="t in typeFilters"
            :key="t.value"
            class="filter-tag"
            :class="{ active: currentType === t.value }"
            @click="switchType(t.value)"
          >
            {{ t.label }}
          </span>
        </div>

        <div class="notice-items">
          <div
            v-for="item in notices"
            :key="item.id"
            class="notice-item"
            :class="{ active: selectedId === item.id }"
            @click="selectNotice(item)"
          >
            <div class="item-icon">
              <el-icon :size="16">
                <Notebook v-if="(item.notice_type || item.type) === 'system'" />
                <InfoFilled v-else-if="(item.notice_type || item.type) === 'rule'" />
                <Present v-else-if="(item.notice_type || item.type) === 'event'" />
                <Setting v-else />
              </el-icon>
            </div>
            <div class="item-body">
              <div class="item-title">{{ item.title }}</div>
              <div class="item-meta">
                <span>{{ formatDateTime(item.published_at || item.publishedAt || item.created_at) }}</span>
                <el-tag size="small" :type="noticeTypeTag(item)">{{ noticeTypeText(item) }}</el-tag>
              </div>
            </div>
          </div>
          <el-empty v-if="notices.length === 0" description="暂无通知" :image-size="80" />
        </div>
      </div>

      <!-- Right: Detail -->
      <div class="notice-detail-panel" v-if="selectedNotice">
        <div class="detail-article">
          <h2>{{ selectedNotice.title }}</h2>
          <div class="detail-meta">
            <span>{{ formatDateTime(selectedNotice.published_at || selectedNotice.publishedAt || selectedNotice.created_at) }}</span>
            <el-tag size="small" :type="noticeTypeTag(selectedNotice)">
              {{ noticeTypeText(selectedNotice) }}
            </el-tag>
            <span v-if="selectedNotice.author_name || selectedNotice.authorName">
              发布人：{{ selectedNotice.author_name || selectedNotice.authorName }}
            </span>
          </div>
          <div class="detail-content" v-html="renderedContent"></div>
        </div>
      </div>
      <div class="notice-detail-panel empty-detail" v-else>
        <el-empty description="请选择左侧通知查看详情" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getNotices } from '../api/notices'

const typeFilters = [
  { label: '全部', value: '' },
  { label: '系统通知', value: 'system' },
  { label: '规则变更', value: 'rule' },
  { label: '活动通知', value: 'event' },
  { label: '维护公告', value: 'maintenance' },
]

const notices = ref([])
const selectedNotice = ref(null)
const selectedId = ref(null)
const currentType = ref('')
const loading = ref(false)

const renderedContent = computed(() => {
  if (!selectedNotice.value) return ''
  const content = selectedNotice.value.content || ''
  return content.replace(/\n/g, '<br>')
})

function formatDateTime(value) {
  if (!value) return '-'
  return String(value)
    .replace('T', ' ')
    .replace(/\.\d+$/, '')
}

function noticeTypeText(item) {
  const type = item.notice_type || item.type || ''
  const map = { system: '系统', rule: '规则', event: '活动', maintenance: '维护' }
  return map[type] || type
}

function noticeTypeTag(item) {
  const type = item.notice_type || item.type || ''
  const map = { system: '', rule: 'warning', event: 'success', maintenance: 'info' }
  return map[type] || ''
}

function switchType(val) {
  currentType.value = val
  fetchNotices()
}

function selectNotice(item) {
  selectedNotice.value = item
  selectedId.value = item.id
}

async function fetchNotices() {
  loading.value = true
  try {
    const data = await getNotices({ type: currentType.value, page: 1, pageSize: 100 })
    notices.value = data.records || data || []
    if (notices.value.length > 0 && !selectedNotice.value) {
      selectNotice(notices.value[0])
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

onMounted(fetchNotices)
</script>

<style scoped>
.notices-layout {
  display: flex;
  gap: 0;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,56,147,0.06);
  min-height: 500px;
}

/* Left panel */
.notice-list-panel {
  width: 340px;
  flex-shrink: 0;
  border-right: 1px solid #e8eaf2;
  display: flex;
  flex-direction: column;
}

.list-filter {
  padding: 16px;
  border-bottom: 1px solid #e8eaf2;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.filter-tag {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  color: #4a5568;
  background: #f4f6fb;
  transition: all 0.15s;
}

.filter-tag:hover {
  background: #e8edf9;
  color: #003893;
}

.filter-tag.active {
  background: #003893;
  color: #fff;
}

.notice-items {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.notice-item {
  display: flex;
  gap: 10px;
  padding: 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.12s;
}

.notice-item:hover {
  background: #f4f6fb;
}

.notice-item.active {
  background: #e8edf9;
}

.item-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #eef2fa;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #003893;
  flex-shrink: 0;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a202c;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 6px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #a0aec0;
}

/* Right panel */
.notice-detail-panel {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
}

.notice-detail-panel.empty-detail {
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-article h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 16px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #8492a6;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8eaf2;
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: #2d3748;
}
</style>
