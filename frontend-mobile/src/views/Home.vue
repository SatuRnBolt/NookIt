<template>
  <div class="page-wrap">
    <!-- Hero Header -->
    <div class="home-hero">
      <div class="hero-greeting">欢迎回来，{{ auth.user?.nickname || auth.user?.name || '同学' }}</div>
      <div class="hero-title">找一个安静的角落</div>
      <div class="hero-stats">
        <div class="stat-item">
          <span class="stat-num">{{ stats.pending }}</span>
          <span class="stat-label">待处理</span>
        </div>
        <div class="stat-sep"></div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.today }}</span>
          <span class="stat-label">今日预约</span>
        </div>
        <div class="stat-sep"></div>
        <div class="stat-item">
          <span class="stat-num">{{ stats.available }}</span>
          <span class="stat-label">可用自习室</span>
        </div>
      </div>
    </div>

    <!-- Search -->
    <div class="search-wrap">
      <van-search
        v-model="search"
        placeholder="搜索自习室名称或地点"
        shape="round"
        background="transparent"
        @search="doSearch"
        @clear="clearSearch"
      />
    </div>

    <!-- Room List -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <div class="room-cards">
          <div
            v-for="room in rooms"
            :key="room.id"
            class="room-card"
            @click="router.push(`/room/${room.id}`)"
          >
            <div class="room-card-header">
              <div class="room-card-name">{{ room.roomName || room.room_name || room.name }}</div>
              <span
                class="room-badge"
                :class="room.room_status === 'active' || room.status === 'active' ? 'badge-open' : 'badge-closed'"
              >
                {{ room.room_status === 'active' || room.status === 'active' ? '开放中' : '已关闭' }}
              </span>
            </div>
            <div class="room-card-meta">
              <span class="room-meta-item">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                {{ room.building || room.campus || room.location_detail || '—' }}
              </span>
              <span class="room-meta-item">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18M9 21V9"/></svg>
                {{ room.totalCapacity || room.total_capacity || '—' }} 座
              </span>
            </div>
            <div class="room-card-footer">
              <span class="room-avail">
                <span class="avail-dot"></span>
                可用 {{ room.availableCount ?? room.available_count ?? '—' }} 座
              </span>
              <span class="room-arrow">›</span>
            </div>
          </div>
        </div>

        <van-empty v-if="!loading && rooms.length === 0" description="暂无自习室" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getRooms } from '../api/rooms'

const router = useRouter()
const auth = useAuthStore()

const search = ref('')
const rooms = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const stats = reactive({ pending: 0, today: 0, available: 0 })

async function fetchRooms(reset = false) {
  if (reset) {
    page.value = 1
    rooms.value = []
    finished.value = false
  }
  try {
    const data = await getRooms({ page: page.value, pageSize: 10, search: search.value })
    const list = data?.records || data?.list || []
    if (reset) {
      rooms.value = list
    } else {
      rooms.value.push(...list)
    }
    if (list.length < 10) {
      finished.value = true
    } else {
      page.value++
    }
    stats.available = data?.total || rooms.value.length
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function loadMore() {
  fetchRooms()
}

function onRefresh() {
  page.value = 1
  rooms.value = []
  finished.value = false
  loadMore()
}

function doSearch() {
  page.value = 1
  rooms.value = []
  finished.value = false
  loadMore()
}

function clearSearch() {
  search.value = ''
  page.value = 1
  rooms.value = []
  finished.value = false
  loadMore()
}
</script>

<style scoped>
.home-hero {
  background: linear-gradient(135deg, #001f6b 0%, #003893 60%, #1a5cc8 100%);
  padding: 24px 20px 32px;
  color: #fff;
}

.hero-greeting {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  margin-bottom: 4px;
}

.hero-title {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: 1px;
  margin-bottom: 20px;
}

.hero-stats {
  display: flex;
  align-items: center;
  gap: 0;
  background: rgba(255,255,255,0.12);
  border-radius: 12px;
  padding: 12px 0;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: 22px;
  font-weight: 800;
}

.stat-label {
  font-size: 11px;
  color: rgba(255,255,255,0.65);
}

.stat-sep {
  width: 1px;
  height: 28px;
  background: rgba(255,255,255,0.2);
}

.search-wrap {
  margin-top: -16px;
  padding: 0 4px;
}

.room-cards {
  padding: 4px 12px 0;
}

.room-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: 0 2px 10px rgba(0,56,147,0.06);
  cursor: pointer;
  transition: transform 0.15s;
  active: transform 0.15s;
}

.room-card:active {
  transform: scale(0.98);
}

.room-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.room-card-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  flex: 1;
  margin-right: 8px;
}

.room-badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 8px;
  flex-shrink: 0;
}

.badge-open {
  background: #f0fdf4;
  color: #16a34a;
}

.badge-closed {
  background: #f3f4f6;
  color: #9ca3af;
}

.room-card-meta {
  display: flex;
  gap: 14px;
  margin-bottom: 10px;
}

.room-meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7a99;
}

.room-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid #f0f2f8;
  padding-top: 10px;
}

.room-avail {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #16a34a;
  font-weight: 600;
}

.avail-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
}

.room-arrow {
  font-size: 18px;
  color: #c0c4cc;
}
</style>
