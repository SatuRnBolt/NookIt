<template>
  <div class="home-page">
    <!-- Full-width hero background section -->
    <div class="hero-bg" :style="heroBgStyle">
      <div class="hero-overlay"></div>
      <div class="hero-body">
        <div class="hero-left">
          <div class="hero-welcome">校园自习空间</div>
          <h1 class="hero-title">欢迎回来，{{ auth.user?.nickname || auth.user?.name || '' }} 同学</h1>
          <p class="hero-subtitle">找一个安静的角落，开始今天的学习之旅</p>
          <div class="hero-search-row">
            <el-input
              v-model="search"
              placeholder="输入您想搜索的自习室关键词"
              size="large"
              clearable
              class="hero-input"
            >
              <template #append>
                <el-button type="primary" :icon="Search" @click="doSearch" class="hero-search-btn">搜索</el-button>
              </template>
            </el-input>
          </div>
          <div class="recent-bar">
            <span class="recent-label">最近搜索：</span>
            <span
              v-for="tag in recentSearches"
              :key="tag"
              class="recent-chip"
              @click="search = tag"
            >{{ tag }} ›</span>
          </div>
          <button class="back-home-btn" @click="router.push('/bookings')">
            <span class="btn-icon">↩</span> 我的预约记录
          </button>
        </div>
        <div class="hero-right">
          <div class="stats-card">
            <div class="stats-item">
              <span class="stats-dot dot-orange"></span>
              <span class="stats-num">{{ pendingCount }}</span>
              <span class="stats-label">待处理预约</span>
            </div>
            <div class="stats-sep"></div>
            <div class="stats-item">
              <span class="stats-dot dot-blue"></span>
              <span class="stats-num">{{ todayCount }}</span>
              <span class="stats-label">今日预约</span>
            </div>
            <div class="stats-sep"></div>
            <div class="stats-item">
              <span class="stats-dot dot-green"></span>
              <span class="stats-num">{{ availableRooms }}</span>
              <span class="stats-label">可用自习室</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- AI Assistant Panel -->
    <div class="panel ai-panel">
      <div class="ai-panel-body">
        <div class="ai-panel-left">
          <div class="ai-avatar-wrap">
            <img src="../assets/deepseek-color.svg" style="width:26px;height:26px;" />
          </div>
          <div class="ai-panel-info">
            <div class="ai-panel-name">
              Nookit 智能体
              <span class="ai-online-tag"><span class="ai-green-dot"></span>在线</span>
            </div>
            <div class="ai-panel-caps">
              <span class="ai-cap">预约助手</span>
              <span class="ai-cap">信息查询</span>
              <span class="ai-cap">申诉指导</span>
            </div>
          </div>
        </div>
        <div class="ai-panel-sep"></div>
        <div class="ai-panel-right">
          <el-input
            v-model="aiQuestion"
            placeholder="告诉我你想完成的任务，如：帮我查明天图书馆的可用座位"
            size="large"
            clearable
            class="ai-quick-input"
            @keyup.enter="goAiChat"
          />
          <el-button type="primary" size="large" class="ai-quick-btn" @click="goAiChat">
            发起对话
          </el-button>
        </div>
      </div>
      <div class="ai-panel-footer">
        <span class="ai-sug-label">快速任务：</span>
        <span
          v-for="sug in aiSuggestions"
          :key="sug"
          class="ai-sug-chip"
          @click="aiQuestion = sug"
        >{{ sug }}</span>
      </div>
    </div>

    <!-- My Bookings Section -->

    <div class="panel">
      <div class="panel-header">
        <span class="panel-title-bar"></span>
        <h2 class="panel-title">我的预约</h2>
      </div>
      <div class="booking-grid">
        <div
          v-for="booking in displayBookings"
          :key="booking.id"
          class="booking-card"
          @click="router.push('/bookings')"
        >
          <div class="booking-card-icon" :style="{ background: bookingGradient(booking.id) }">
            <el-icon :size="30" color="rgba(255,255,255,0.9)"><OfficeBuilding /></el-icon>
          </div>
          <div class="booking-card-body">
            <div class="booking-card-name">{{ booking.roomName || booking.room_name || booking.name }}</div>
            <div class="booking-card-sub">{{ booking.type || '学习服务' }}</div>
          </div>
        </div>
        <div
          v-for="i in emptySlots"
          :key="'e' + i"
          class="booking-card booking-card-add"
          @click="router.push('/rooms')"
        >
          <div class="booking-card-icon icon-add">
            <el-icon :size="26" color="rgba(0,56,147,0.5)"><Plus /></el-icon>
          </div>
          <div class="booking-card-body">
            <div class="booking-card-name">新建预约</div>
            <div class="booking-card-sub">学习服务</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Browse Rooms Section -->
    <div class="panel" v-loading="loading">
      <div class="panel-header">
        <span class="panel-title-bar"></span>
        <h2 class="panel-title">自习区域</h2>
        <div class="filter-tabs">
          <span
            v-for="tab in filterTabs"
            :key="tab.value"
            :class="['ftab', { 'ftab-active': activeFilter === tab.value }]"
            @click="activeFilter = tab.value"
          >{{ tab.label }}</span>
        </div>
      </div>
      <div class="browse-layout">
        <!-- Sidebar -->
        <div class="browse-sidebar">
          <div
            v-for="item in campusList"
            :key="item.value"
            :class="['sidebar-item', { 'sidebar-active': activeCampus === item.value }]"
            @click="activeCampus = item.value"
          >
            <el-icon :size="15"><Grid /></el-icon>
            <span class="sidebar-label">{{ item.label }}</span>
            <span class="sidebar-badge">{{ item.count }}</span>
          </div>
        </div>

        <!-- Room List -->
        <div class="room-list">
          <div
            v-for="room in filteredRooms"
            :key="room.id"
            class="room-row"
          >
            <div class="room-row-icon" :style="{ background: bookingGradient(room.id) }">
              <el-icon :size="22" color="rgba(255,255,255,0.9)"><OfficeBuilding /></el-icon>
            </div>
            <div class="room-row-info">
              <div class="room-row-name">{{ room.roomName || room.name || room.room_name }}</div>
              <div class="room-row-meta">
                <el-tag
                  size="small"
                  :type="(room.room_status || room.status) === 'active' ? 'success' : 'info'"
                  effect="light"
                  round
                  class="room-tag"
                >{{ (room.room_status || room.status) === 'active' ? '开放中' : '维护中' }}</el-tag>
                <span class="room-row-sub">
                  <el-icon :size="12"><View /></el-icon>
                  {{ room.availableSeats ?? room.available_seats ?? 0 }}/{{ room.totalCapacity ?? room.total_capacity ?? 0 }} 座
                </span>
              </div>
            </div>
            <div class="room-row-actions">
              <el-button size="small" type="primary" @click="goRoom(room.id)">进入预约</el-button>
              <el-button size="small" plain @click="goRoom(room.id)">查看</el-button>
            </div>
          </div>
          <el-empty
            v-if="!loading && filteredRooms.length === 0"
            description="暂无匹配的自习室"
            :image-size="80"
            style="padding: 40px 0"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStudentAuthStore } from '../stores/auth'
import { getRooms } from '../api/rooms'
import { getMyReservations } from '../api/reservations'
import { Search, OfficeBuilding, Plus, Grid, View } from '@element-plus/icons-vue'
import { useThemeStore } from '../stores/theme'
import bgBlue from '../assets/background.png'
import bgGreen from '../assets/xiantiao_lv.png'
import bgRed from '../assets/xiantiao_hong.png'

const themeStore = useThemeStore()
const heroBgStyle = computed(() => {
  const map = { green: bgGreen, red: bgRed }
  return { backgroundImage: `url(${map[themeStore.theme] || bgBlue})` }
})

const router = useRouter()
const auth = useStudentAuthStore()

const rooms = ref([])
const myBookings = ref([])
const search = ref('')
const aiQuestion = ref('')
const aiSuggestions = ['帮我查可用座位', '分析我的预约状态', '信用积分还够吗', '协助违规申诉']

function goAiChat() {
  router.push(aiQuestion.value.trim()
    ? { path: '/ai-chat', query: { q: aiQuestion.value.trim() } }
    : '/ai-chat'
  )
}
const loading = ref(false)
const activeCampus = ref('all')
const activeFilter = ref('category')

const recentSearches = ['图书馆', '理科楼', '文科楼', '逸夫楼', '自习室']

const filterTabs = [
  { label: '服务类别', value: 'category' },
  { label: '所属校区', value: 'campus' },
  { label: '开放时段', value: 'time' },
]

const gradients = [
  'linear-gradient(135deg,#0f2557 0%,#1a3f8a 100%)',
  'linear-gradient(135deg,#0d4e3b 0%,#1a7a58 100%)',
  'linear-gradient(135deg,#3b1f5c 0%,#5c2d8a 100%)',
  'linear-gradient(135deg,#5c1f1f 0%,#8a3030 100%)',
  'linear-gradient(135deg,#1a365d 0%,#2a5296 100%)',
  'linear-gradient(135deg,#2d4020 0%,#4a6630 100%)',
]

function bookingGradient(id) {
  return gradients[(id - 1) % gradients.length]
}

const pendingCount = computed(() =>
  myBookings.value.filter(b => b.status === 'pending' || b.status === 'confirmed').length
)

const todayCount = computed(() => {
  const today = new Date().toISOString().slice(0, 10)
  return myBookings.value.filter(b => (b.date || b.bookingDate || '').startsWith(today)).length
})

const availableRooms = computed(() =>
  rooms.value.filter(r => (r.room_status || r.status) === 'active').length
)

const totalAvailableSeats = computed(() =>
  rooms.value.reduce((sum, r) => sum + (r.availableSeats ?? r.available_seats ?? 0), 0)
)

const displayBookings = computed(() => myBookings.value.slice(0, 5))

const emptySlots = computed(() => Math.max(0, 5 - displayBookings.value.length))

const campusList = computed(() => {
  const map = {}
  rooms.value.forEach(r => {
    const c = r.campus || '邯郸校区'
    map[c] = (map[c] || 0) + 1
  })
  const items = [{ label: '全部', value: 'all', count: rooms.value.length }]
  Object.entries(map).forEach(([label, count]) => items.push({ label, value: label, count }))
  return items
})

const filteredRooms = computed(() => {
  let list = rooms.value
  if (activeCampus.value !== 'all') {
    list = list.filter(r => (r.campus || '邯郸校区') === activeCampus.value)
  }
  if (search.value) {
    const kw = search.value.toLowerCase()
    list = list.filter(r => {
      const name = (r.roomName || r.name || r.room_name || '').toLowerCase()
      const loc = (r.location || r.location_detail || r.building || r.campus || '').toLowerCase()
      return name.includes(kw) || loc.includes(kw)
    })
  }
  return list
})

function doSearch() {
  if (search.value && !recentSearches.includes(search.value)) {
    recentSearches.unshift(search.value)
    if (recentSearches.length > 5) recentSearches.pop()
  }
}

function goRoom(id) {
  router.push(`/room/${id}`)
}

onMounted(async () => {
  loading.value = true
  try {
    const [roomData, bookingData] = await Promise.all([
      getRooms({ page: 1, pageSize: 50 }),
      getMyReservations({ page: 1, pageSize: 10 }).catch(() => ({ records: [] })),
    ])
    rooms.value = roomData.records || roomData || []
    myBookings.value = (bookingData.records || bookingData || []).slice(0, 5)
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-top: 0;
}

/* ── Hero ── */
.hero-bg {
  position: relative;
  margin: -24px -40px 0;
  padding: 56px 40px 64px;
  background: url('../assets/background.png') center top / cover no-repeat;
  overflow: hidden;
}

/* Two-layer overlay:
   1. Blue tint for brand color + readability
   2. Vertical fade: transparent at top → page bg (#f4f6fb) at bottom */
.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    transparent 0%,
    transparent 50%,
    #f4f6fb 100%
  );
}

.hero-body {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  gap: 40px;
}

.hero-left {
  flex: 1;
  min-width: 0;
}

.hero-welcome {
  font-size: 14px;
  font-weight: 600;
  color: rgba(255,255,255,0.75);
  letter-spacing: 3px;
  text-transform: uppercase;
  margin: 0 0 10px;
  text-shadow: 0 1px 4px rgba(0,0,0,0.4);
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2px;
  margin: 0 0 14px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.55), 0 4px 24px rgba(0,0,0,0.4);
}

.hero-subtitle {
  font-size: 17px;
  color: rgba(255,255,255,0.8);
  margin: 0 0 28px;
  text-shadow: 0 1px 4px rgba(0,0,0,0.4);
  letter-spacing: 0.5px;
}

.hero-search-row {
  max-width: 620px;
  margin-bottom: 18px;
}

.hero-input :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
  padding: 8px 14px;
  box-shadow: 0 4px 24px rgba(0,0,0,0.18);
  background: #fff;
}

.hero-input :deep(.el-input-group__append) {
  padding: 0;
  border: none;
  background: transparent;
  box-shadow: none;
  border-radius: 0 10px 10px 0;
  overflow: hidden;
}

.hero-search-btn {
  height: 100%;
  border-radius: 0 10px 10px 0 !important;
  padding: 0 32px;
  font-size: 16px;
  letter-spacing: 1px;
  background: var(--nt-primary) !important;
  border-color: var(--nt-primary) !important;
  color: #fff !important;
}

.hero-search-btn:hover {
  background: var(--nt-primary-dark) !important;
  border-color: var(--nt-primary-dark) !important;
}

.hero-input :deep(.el-input-group__append .el-button) {
  --el-button-bg-color: var(--nt-primary);
  --el-button-text-color: #fff;
  --el-button-border-color: var(--nt-primary);
  --el-button-hover-bg-color: var(--nt-primary-dark);
  --el-button-hover-border-color: var(--nt-primary-dark);
  background-color: var(--nt-primary) !important;
  color: #fff !important;
  border: none !important;
}

.recent-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 28px;
}

.recent-label {
  font-size: 14px;
  color: rgba(255,255,255,0.9);
  white-space: nowrap;
  text-shadow: 0 1px 4px rgba(0,0,0,0.5);
}

.recent-chip {
  font-size: 14px;
  color: rgba(255,255,255,0.85);
  background: rgba(255,255,255,0.12);
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 20px;
  padding: 4px 16px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}

.recent-chip:hover {
  background: rgba(255,255,255,0.22);
}

.back-home-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--nt-primary-dark);
  background: #fff;
  border: 1px solid rgba(255,255,255,0.8);
  border-radius: 8px;
  padding: 9px 20px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.back-home-btn:hover {
  background: var(--nt-primary-light);
  border-color: var(--nt-primary-border);
}

.btn-icon {
  font-size: 16px;
}

/* Stats card */
.hero-right {
  flex-shrink: 0;
}

.stats-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 14px;
  border: 1px solid rgba(255,255,255,0.7);
  box-shadow: 0 8px 32px rgba(0,0,0,0.18);
  padding: 14px 8px;
  min-width: 380px;
}

.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 2px 16px;
}

.stats-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot-orange { background: #f59e0b; }
.dot-blue   { background: var(--nt-primary); }
.dot-green  { background: #22c55e; }

.stats-num {
  font-size: 26px;
  font-weight: 800;
  color: #1a202c;
  line-height: 1;
  letter-spacing: -1px;
}

.stats-label {
  font-size: 12px;
  color: #8492a6;
  white-space: nowrap;
}

.stats-sep {
  width: 1px;
  height: 44px;
  background: #eef0f6;
  flex-shrink: 0;
}

/* ── Panels ── */
.panel {
  background: #fff;
  border-radius: 14px;
  padding: 24px 28px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  border: 1px solid #f0f2f8;
}

.panel-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.panel-title-bar {
  display: inline-block;
  width: 4px;
  height: 20px;
  border-radius: 2px;
  background: var(--nt-primary);
  flex-shrink: 0;
}

.panel-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a202c;
  margin: 0;
}

.filter-tabs {
  display: flex;
  gap: 0;
  margin-left: auto;
}

.ftab {
  font-size: 13px;
  color: #8492a6;
  padding: 4px 14px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.18s;
}

.ftab:hover {
  color: var(--nt-primary);
  background: var(--nt-primary-light);
}

.ftab-active {
  color: var(--nt-primary);
  background: var(--nt-primary-light);
  font-weight: 500;
}

/* ── Booking Grid ── */
.booking-grid {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.booking-card {
  display: flex;
  align-items: center;
  gap: 14px;
  width: calc(20% - 13px);
  min-width: 160px;
  background: #f8f9fc;
  border: 1px solid #eef0f6;
  border-radius: 12px;
  padding: 16px 14px;
  cursor: pointer;
  transition: all 0.22s;
}

.booking-card:hover {
  border-color: var(--nt-primary);
  box-shadow: 0 4px 16px var(--nt-primary-shadow);
  background: #fff;
}

.booking-card-add {
  opacity: 0.7;
}

.booking-card-add:hover {
  opacity: 1;
}

.booking-card-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-add {
  background: var(--nt-primary-light);
  border: 2px dashed var(--nt-primary-border);
}

.booking-card-body {
  min-width: 0;
}

.booking-card-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a202c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.booking-card-sub {
  font-size: 11px;
  color: #8492a6;
  margin-top: 3px;
}

/* ── Browse Layout ── */
.browse-layout {
  display: flex;
  gap: 0;
  min-height: 320px;
}

.browse-sidebar {
  width: 160px;
  flex-shrink: 0;
  border-right: 1px solid #f0f2f8;
  padding-right: 0;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  font-size: 14px;
  color: #4a5568;
  cursor: pointer;
  border-radius: 8px 0 0 8px;
  transition: all 0.18s;
  margin-bottom: 2px;
}

.sidebar-item:hover {
  background: var(--nt-primary-light);
  color: var(--nt-primary);
}

.sidebar-active {
  background: var(--nt-primary-light);
  color: var(--nt-primary);
  font-weight: 600;
}

.sidebar-label {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-badge {
  font-size: 12px;
  background: var(--nt-primary-light);
  color: var(--nt-primary);
  border-radius: 10px;
  padding: 1px 8px;
  font-weight: 600;
  flex-shrink: 0;
}

.sidebar-active .sidebar-badge {
  background: var(--nt-primary-border);
}

/* ── Room List ── */
.room-list {
  flex: 1;
  padding-left: 20px;
}

.room-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f6fa;
  transition: background 0.18s;
}

.room-row:last-child {
  border-bottom: none;
}

.room-row:hover {
  background: #fafbff;
  margin: 0 -8px;
  padding-left: 8px;
  padding-right: 8px;
  border-radius: 8px;
}

.room-row-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.room-row-info {
  flex: 1;
  min-width: 0;
}

.room-row-name {
  font-size: 15px;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 5px;
}

.room-row-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.room-tag {
  font-size: 11px;
}

.room-row-sub {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #8492a6;
}

.room-row-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* ── AI Panel ── */
.ai-panel {
  position: relative;
  padding: 0;
  overflow: hidden;
}

.ai-panel::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(180deg, var(--nt-primary) 0%, var(--nt-primary-dark) 100%);
  border-radius: 14px 0 0 14px;
}

.ai-panel-body {
  display: flex;
  align-items: center;
  padding: 20px 24px 20px 32px;
}

.ai-panel-left {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
  padding-right: 28px;
}

.ai-avatar-wrap {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: #eef3ff;
  border: 1.5px solid #d0def8;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.ai-panel-info {
  min-width: 0;
}

.ai-panel-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 7px;
  white-space: nowrap;
}

.ai-online-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 500;
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 1px 8px;
}

.ai-green-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #22c55e;
}

.ai-panel-caps {
  display: flex;
  gap: 5px;
}

.ai-cap {
  font-size: 11px;
  color: #6b7a99;
  background: #f4f6fb;
  border: 1px solid #eef0f6;
  border-radius: 4px;
  padding: 2px 8px;
  white-space: nowrap;
}

.ai-panel-sep {
  width: 1px;
  height: 52px;
  background: #eef0f6;
  flex-shrink: 0;
  margin-right: 28px;
}

.ai-panel-right {
  flex: 1;
  display: flex;
  gap: 10px;
  align-items: center;
  min-width: 0;
}

.ai-quick-input {
  flex: 1;
  min-width: 0;
}

.ai-quick-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1.5px #e8eaf2;
  background: #fff;
}

.ai-quick-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1.5px var(--nt-primary);
}

.ai-quick-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px var(--nt-primary-shadow);
}

.ai-quick-input :deep(.el-input__inner) {
  color: #1a202c;
}

.ai-quick-input :deep(.el-input__inner::placeholder) {
  color: #b0bac9;
}

.ai-quick-btn {
  border-radius: 8px !important;
  padding: 0 22px !important;
  font-weight: 600 !important;
  flex-shrink: 0;
  white-space: nowrap;
}

.ai-panel-footer {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 11px 24px 13px 32px;
  border-top: 1px solid #f0f2f8;
  background: #fafbfd;
}

.ai-sug-label {
  font-size: 12px;
  color: #8492a6;
  white-space: nowrap;
}

.ai-sug-chip {
  font-size: 12px;
  color: #4a5568;
  background: #fff;
  border: 1px solid #eaecf4;
  border-radius: 20px;
  padding: 3px 12px;
  cursor: pointer;
  transition: all 0.18s;
  white-space: nowrap;
}

.ai-sug-chip:hover {
  background: var(--nt-primary-light);
  color: var(--nt-primary);
  border-color: var(--nt-primary-border);
}

/* ── Responsive ── */
@media (max-width: 1100px) {
  .booking-card {
    width: calc(25% - 12px);
  }
}

@media (max-width: 900px) {
  .hero-body {
    flex-direction: column;
    padding: 32px 28px;
  }

  .stats-card {
    min-width: unset;
    width: 100%;
  }

  .booking-card {
    width: calc(50% - 8px);
  }

  .hero-title {
    font-size: 26px;
  }
}

@media (max-width: 600px) {
  .booking-card {
    width: calc(50% - 8px);
  }

  .browse-sidebar {
    width: 120px;
  }

  .hero-body {
    padding: 24px 20px;
  }
}
</style>
