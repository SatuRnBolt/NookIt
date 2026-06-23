<template>
  <div>
    <van-nav-bar :title="notice.title ? '公告详情' : '加载中...'" left-arrow @click-left="router.back()" />
    <div style="padding: 12px">
      <div class="notice-detail-card" v-if="notice.id">
        <h2 class="nd-title">{{ notice.title }}</h2>
        <div class="nd-meta">
          <span>{{ notice.author || '管理员' }}</span>
          <span>{{ notice.publishedAt || notice.published_at || '' }}</span>
        </div>
        <div class="nd-content">{{ notice.content }}</div>
      </div>
      <van-loading v-else size="32" color="#003893" style="display:flex;justify-content:center;padding:60px" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getNoticeDetail } from '../api/notices'

const router = useRouter()
const route = useRoute()
const notice = ref({})

onMounted(async () => {
  try {
    notice.value = await getNoticeDetail(route.params.id) || {}
  } catch { /* handled */ }
})
</script>

<style scoped>
.notice-detail-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,56,147,0.06);
}

.nd-title {
  font-size: 18px;
  font-weight: 800;
  color: #1a202c;
  margin: 0 0 12px;
  line-height: 1.4;
}

.nd-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #a0aec0;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f2f8;
}

.nd-content {
  font-size: 14px;
  color: #4a5568;
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
