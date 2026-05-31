<template>
  <div class="notice-detail-page" v-loading="loading">
    <div class="page-header">
      <el-button text @click="router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        返回
      </el-button>
    </div>

    <div class="content-card" v-if="notice">
      <h2>{{ notice.title }}</h2>
      <div class="detail-meta">
        <span>{{ formatDateTime(notice.published_at || notice.publishedAt || notice.created_at) }}</span>
        <el-tag size="small">{{ notice.notice_type || notice.type || '' }}</el-tag>
        <span v-if="notice.author_name || notice.authorName">
          发布人：{{ notice.author_name || notice.authorName }}
        </span>
      </div>
      <div class="detail-body" v-html="renderedContent"></div>
    </div>

    <el-empty v-if="!loading && !notice" description="通知不存在或已删除" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoticeDetail } from '../api/notices'

const route = useRoute()
const router = useRouter()
const noticeId = route.params.id
const notice = ref(null)
const loading = ref(false)

const renderedContent = computed(() => {
  if (!notice.value) return ''
  return (notice.value.content || '').replace(/\n/g, '<br>')
})

function formatDateTime(value) {
  if (!value) return '-'
  return String(value)
    .replace('T', ' ')
    .replace(/\.\d+$/, '')
}

onMounted(async () => {
  loading.value = true
  try {
    notice.value = await getNoticeDetail(noticeId)
  } catch {
    // handled
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.notice-detail-page h2 {
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 14px;
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

.detail-body {
  font-size: 15px;
  line-height: 1.8;
  color: #2d3748;
}
</style>
