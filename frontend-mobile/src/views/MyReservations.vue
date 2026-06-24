<template>
  <div class="page-wrap">
    <div class="page-title">我的预约</div>

    <!-- Status Tabs -->
    <div class="tab-scroll">
      <div class="tab-list">
        <span
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: currentTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </span>
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMore"
      >
        <div class="reservation-cards">
          <div
            v-for="item in records"
            :key="item.id"
            class="res-card"
          >
            <div class="res-card-header">
              <div class="res-room">{{ item.roomName || item.room_name || '—' }}</div>
              <span class="status-tag" :class="item.status || item.reservation_status">
                {{ statusText(item) }}
              </span>
            </div>

            <div class="res-detail">
              <div class="res-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="18" rx="2"/><line x1="2" y1="9" x2="22" y2="9"/></svg>
                {{ item.date || item.reservation_date || '—' }}
              </div>
              <div class="res-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>
                {{ item.startTime || item.start_time || '—' }} — {{ item.endTime || item.end_time || '—' }}
              </div>
              <div class="res-item seat-item">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>
                座位 <strong>{{ item.seatCode || item.seat_code || '—' }}</strong>
              </div>
            </div>

            <!-- Check-in Code (for pending_checkin status) -->
            <div
              v-if="rowStatus(item) === 'pending_checkin'"
              class="checkin-code-box"
            >
              <span class="code-label">签到码</span>
              <span class="code-value">{{ item.checkinCode || item.checkin_code || item.code || '——' }}</span>
            </div>

            <!-- Actions -->
            <div class="res-card-footer" v-if="rowStatus(item) === 'pending_checkin'">
              <van-button
                size="small"
                plain
                type="danger"
                @click="handleCancel(item)"
              >
                取消预约
              </van-button>
            </div>
          </div>
        </div>

        <van-empty v-if="!loading && records.length === 0" description="暂无预约记录" />
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { showToast, showConfirmDialog } from 'vant'
import { getMyReservations, cancelReservation } from '../api/reservations'

const tabs = [
  { label: '全部', value: '' },
  { label: '待签到', value: 'pending_checkin' },
  { label: '已签到', value: 'checked_in' },
  { label: '已完成', value: 'completed' },
  { label: '已违约', value: 'violated' },
  { label: '已取消', value: 'cancelled' },
]

const currentTab = ref('')
const records = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)

const statusMap = {
  pending_checkin: '待签到',
  checked_in: '已签到',
  completed: '已完成',
  cancelled: '已取消',
  violated: '已违约',
}

function rowStatus(item) {
  return item.status || item.reservation_status || ''
}

function statusText(item) {
  return statusMap[rowStatus(item)] || rowStatus(item) || '—'
}

async function fetchData(reset = false) {
  if (reset) {
    page.value = 1
    records.value = []
    finished.value = false
  }
  try {
    const data = await getMyReservations({
      page: page.value,
      pageSize: 10,
      status: currentTab.value,
    })
    const list = data?.records || []
    if (reset) {
      records.value = list
    } else {
      records.value.push(...list)
    }
    if (list.length < 10 || records.value.length >= (data?.total || 0)) {
      finished.value = true
    } else {
      page.value++
    }
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function loadMore() { fetchData() }

function onRefresh() {
  page.value = 1
  records.value = []
  finished.value = false
  loadMore()
}

function switchTab(val) {
  currentTab.value = val
  page.value = 1
  records.value = []
  finished.value = false
  loadMore()
}

async function handleCancel(item) {
  try {
    await showConfirmDialog({
      title: '取消预约',
      message: '确定要取消这个预约吗？',
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      confirmButtonColor: '#ef4444',
    })
    await cancelReservation(item.id)
    showToast('预约已取消')
    fetchData(true)
  } catch {
    // user cancelled dialog or request error
  }
}

</script>

<style scoped>
.tab-scroll {
  overflow-x: auto;
  padding: 0 12px 8px;
  -webkit-overflow-scrolling: touch;
}

.tab-list {
  display: inline-flex;
  gap: 6px;
  white-space: nowrap;
}

.tab-item {
  padding: 7px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #4a5568;
  cursor: pointer;
  background: #fff;
  border: 1.5px solid #e8eaf2;
  transition: all 0.15s;
  flex-shrink: 0;
}

.tab-item.active {
  background: #003893;
  border-color: #003893;
  color: #fff;
  font-weight: 600;
}

.reservation-cards {
  padding: 4px 12px 0;
}

.res-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: 0 2px 10px rgba(0,56,147,0.06);
}

.res-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.res-room {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
}

.res-detail {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
}

.res-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7a99;
}

.seat-item strong {
  color: #003893;
  font-size: 14px;
  letter-spacing: 1px;
}

.checkin-code-box {
  display: flex;
  align-items: center;
  gap: 10px;
  background: #eef4ff;
  border-radius: 10px;
  padding: 10px 14px;
  margin-bottom: 10px;
}

.code-label {
  font-size: 12px;
  color: #6b7a99;
  flex-shrink: 0;
}

.code-value {
  font-size: 26px;
  font-weight: 900;
  color: #003893;
  letter-spacing: 8px;
  font-family: 'Courier New', monospace;
}

.res-card-footer {
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #f0f2f8;
  padding-top: 10px;
}
</style>
