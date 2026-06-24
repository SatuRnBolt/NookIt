<template>
  <div class="page-wrap">
    <div class="page-title">通知公告</div>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <div style="padding: 0 12px">
          <div
            v-for="item in notices"
            :key="item.id"
            class="notice-card"
            @click="router.push(`/notice/${item.id}`)"
          >
            <div class="notice-header">
              <span class="notice-type" :class="item.type">{{ typeLabel(item.type) }}</span>
              <span class="notice-pinned" v-if="item.pinned">📌 置顶</span>
            </div>
            <div class="notice-title">{{ item.title }}</div>
            <div class="notice-meta">
              <span>{{ item.author || '管理员' }}</span>
              <span>{{ item.publishedAt || item.published_at || item.createdAt || item.created_at || '' }}</span>
            </div>
          </div>
        </div>
        <van-empty v-if="!loading && notices.length === 0" description="暂无公告" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getNotices } from '../api/notices'

const router = useRouter()
const notices = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const typeLabels = {
  system: '系统',
  rule: '规则',
  event: '活动',
  maintenance: '维护',
}

function typeLabel(t) {
  return typeLabels[t] || t || '通知'
}

async function fetchData(reset = false) {
  if (reset) { page.value = 1; notices.value = []; finished.value = false }
  try {
    const data = await getNotices({ page: page.value, pageSize: 15 })
    const list = data?.records || []
    if (reset) notices.value = list
    else notices.value.push(...list)
    if (list.length < 15) finished.value = true
    else page.value++
  } catch { finished.value = true }
  finally { loading.value = false; refreshing.value = false }
}

function loadMore() { fetchData() }

function onRefresh() {
  page.value = 1
  notices.value = []
  finished.value = false
  loadMore()
}
</script>

<style scoped>
.notice-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: 0 2px 10px rgba(0,56,147,0.06);
  cursor: pointer;
}

.notice-card:active { opacity: 0.85; }

.notice-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.notice-type {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
}

.notice-type.system { background: #eff6ff; color: #2563eb; }
.notice-type.rule { background: #fef3c7; color: #d97706; }
.notice-type.event { background: #f0fdf4; color: #16a34a; }
.notice-type.maintenance { background: #fef2f2; color: #dc2626; }
.notice-type:not(.system):not(.rule):not(.event):not(.maintenance) {
  background: #f3f4f6; color: #6b7280;
}

.notice-pinned {
  font-size: 11px;
  color: #d97706;
}

.notice-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #a0aec0;
}
</style>
