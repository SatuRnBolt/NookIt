<template>
  <div class="bookings-page">

    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">
          <el-icon class="title-icon"><Calendar /></el-icon>
          预约记录
        </div>
        <div class="page-header-sub">查看与管理全校自习室预约情况，支持手动签到与取消</div>
      </div>
    </div>

    <!-- Filter Card: tabs + filters together -->
    <div class="filter-card">
      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          class="tab-btn"
          :class="{ active: activeTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
          <span class="tab-badge" :class="{ 'tab-badge-active': activeTab === tab.value }">{{ tab.count }}</span>
        </button>
      </div>

      <div class="filter-divider"></div>

      <div class="filter-row">
        <div class="filter-left">
          <el-input v-model="search" placeholder="搜索学生姓名/学号" prefix-icon="Search" clearable style="width:200px" />
          <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:120px">
            <el-option label="待签到" value="pending_checkin" />
            <el-option label="已签到" value="checked_in" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已违约" value="violated" />
          </el-select>
          <el-date-picker
            v-model="dateFilter"
            type="date"
            placeholder="选择日期"
            clearable
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width:160px"
            :disabled="activeTab !== 'all'"
          />
        </div>
        <span class="filter-count">共 {{ total }} 条</span>
      </div>
    </div>

    <!-- Table Card -->
    <div class="content-card">
      <el-table :data="bookings" stripe style="width:100%">
        <el-table-column prop="reservationNo" label="预约编号" min-width="124">
          <template #default="{ row }">
            <span class="reservation-no">{{ row.reservationNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生" min-width="130">
          <template #default="{ row }">
            <div class="student-name">{{ row.studentName }}</div>
            <div class="student-id">{{ row.studentId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="roomName" label="自习室" min-width="160" />
        <el-table-column prop="seatNo" label="座位" width="76" align="center" />
        <el-table-column prop="date" label="日期" width="112" />
        <el-table-column label="时间段" width="144">
          <template #default="{ row }">
            {{ row.startTime || '-' }} – {{ row.endTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="96" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="预约时间" min-width="152" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div style="display: flex; gap: 8px;">
              <el-button v-if="row.status === 'pending_checkin'" link type="success" @click="manualCheckIn(row)">手动签到</el-button>
              <el-button v-if="row.status === 'pending_checkin'" link type="danger" @click="cancelBooking(row)">取消预约</el-button>
              <span v-if="row.status !== 'pending_checkin'" class="no-action">—</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </div>
  </div><!-- bookings-page -->
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookings, getBookingStats, cancelBooking as cancelBookingApi, checkinBooking } from '../api/bookings'

const bookings = ref([])
const total = ref(0)
const search = ref('')
const statusFilter = ref('')
const dateFilter = ref('')
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

const stats = ref({ todayCount: 0, weekCount: 0, totalCount: 0 })

const tabs = computed(() => [
  { value: 'today', label: '今日',    count: stats.value.todayCount },
  { value: 'week',  label: '本周',    count: stats.value.weekCount },
  { value: 'all',   label: '全部预约', count: stats.value.totalCount },
])

async function loadBookings() {
  try {
    const data = await getBookings({
      page: currentPage.value,
      pageSize: pageSize.value,
      search: search.value,
      status: statusFilter.value,
      date: dateFilter.value,
      tab: activeTab.value,
    })
    bookings.value = data.records || []
    total.value = data.total || 0
  } catch { bookings.value = [] }
}

async function loadStats() {
  try { stats.value = await getBookingStats(activeTab.value) } catch {}
}

watch([search, statusFilter, dateFilter], () => { currentPage.value = 1; loadBookings() })
watch([currentPage, pageSize], loadBookings)

onMounted(() => { loadBookings(); loadStats() })

function switchTab(val) {
  activeTab.value = val
  if (val !== 'all') dateFilter.value = ''
  currentPage.value = 1
  loadBookings()
  loadStats()
}

function statusText(s) {
  return {
    pending_checkin: '待签到',
    checked_in: '已签到',
    completed: '已完成',
    cancelled: '已取消',
    violated: '已违约',
  }[s] || s
}

function statusTagType(s) {
  return {
    pending_checkin: 'warning',
    checked_in: 'success',
    completed: 'info',
    cancelled: 'info',
    violated: 'danger',
  }[s] || ''
}

async function cancelBooking(b) {
  try {
    await ElMessageBox.confirm(`确定取消 ${b.studentName} 的预约吗？`, '确认取消', { type: 'warning' })
    await cancelBookingApi(b.id)
    ElMessage.success('预约已取消')
    loadBookings()
    loadStats()
  } catch {}
}

async function manualCheckIn(b) {
  try {
    await checkinBooking(b.id)
    ElMessage.success('手动签到成功')
    loadBookings()
  } catch {}
}

</script>

<style scoped>
.bookings-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ── Page Header ── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.page-header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}
.title-icon { color: #003893; }
.page-header-sub {
  margin-top: 5px;
  font-size: 13px;
  color: #94a3b8;
}

/* ── Stats Row ── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  border: 1px solid #f0f2f8;
}
.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-blue   { background: #eff3ff; color: #003893; }
.stat-purple { background: #f5f3ff; color: #7c3aed; }
.stat-green  { background: #f0fdf4; color: #16a34a; }
.stat-orange { background: #fff7ed; color: #ea580c; }
.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}
.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* ── Filter Card ── */
.filter-card {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f0f2f8;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  overflow: hidden;
}

.tab-bar {
  display: flex;
  gap: 4px;
  padding: 14px 16px 0;
}

.tab-btn {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 8px 18px;
  border: none;
  background: transparent;
  color: #64748b;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.15s, border-color 0.15s;
}
.tab-btn:hover { color: #003893; }
.tab-btn.active {
  color: #003893;
  font-weight: 700;
  border-bottom-color: #003893;
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  background: #f0f2f8;
  color: #8492a6;
}
.tab-badge-active {
  background: #dce8ff;
  color: #003893;
}

.filter-divider {
  height: 1px;
  background: #f0f2f8;
  margin: 0 16px;
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.filter-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-count {
  font-size: 13px;
  color: #94a3b8;
  white-space: nowrap;
}

/* ── Table Card ── */
.content-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0,0,0,0.05);
  border: 1px solid #f0f2f8;
}

.student-name { font-size: 13.5px; font-weight: 600; color: #111827; }
.student-id   { font-size: 11px; color: #9ca3af; margin-top: 2px; }
.reservation-no { font-size: 12.5px; font-weight: 600; color: #475569; font-family: ui-monospace, SFMono-Regular, Menlo, monospace; }
.no-action { color: #cbd5e1; }

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f2f8;
}
</style>

<style>
/* Global styles for dialog centering fix */
.el-overlay-dialog {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.el-dialog {
  margin-top: 0 !important;
}
</style>
