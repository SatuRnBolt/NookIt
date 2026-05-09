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
            <el-option label="待签到" value="confirmed" />
            <el-option label="已签到" value="checkedin" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已违约" value="missed" />
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
        <span class="filter-count">共 {{ filteredBookings.length }} 条</span>
      </div>
    </div>

    <!-- Table Card -->
    <div class="content-card">
      <el-table :data="pagedBookings" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="64" />
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
            {{ String(row.startHour).padStart(2,'0') }}:00 – {{ String(row.endHour).padStart(2,'0') }}:00
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
              <el-button v-if="row.status === 'confirmed'" link type="success" @click="manualCheckIn(row)">手动签到</el-button>
              <el-button v-if="row.status === 'confirmed'" link type="danger" @click="cancelBooking(row)">取消预约</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredBookings.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </div>
  </div><!-- bookings-page -->
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockBookings, mockRooms } from '../mock/data'

const bookings = ref([...mockBookings])
const allRooms = mockRooms

const search = ref('')
const statusFilter = ref('')
const dateFilter = ref('')
const activeTab = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

const today = new Date().toISOString().slice(0, 10)

function getWeekRange() {
  const now = new Date()
  const day = now.getDay() || 7
  const mon = new Date(now); mon.setDate(now.getDate() - day + 1); mon.setHours(0,0,0,0)
  const sun = new Date(mon); sun.setDate(mon.getDate() + 6); sun.setHours(23,59,59,999)
  return [mon.toISOString().slice(0,10), sun.toISOString().slice(0,10)]
}
const [weekStart, weekEnd] = getWeekRange()

const todayCount = computed(() => bookings.value.filter(b => b.date === today).length)
const weekCount  = computed(() => bookings.value.filter(b => b.date >= weekStart && b.date <= weekEnd).length)
const checkedInCount = computed(() => bookings.value.filter(b => b.status === 'checkedin').length)
const missedCount    = computed(() => bookings.value.filter(b => b.status === 'missed').length)

const tabs = computed(() => [
  { value: 'today', label: '今日',   count: todayCount.value },
  { value: 'week',  label: '本周',   count: weekCount.value },
  { value: 'all',   label: '全部预约', count: bookings.value.length },
])

const filteredBookings = computed(() => bookings.value.filter(b => {
  if (activeTab.value === 'today' && b.date !== today) return false
  if (activeTab.value === 'week'  && (b.date < weekStart || b.date > weekEnd)) return false
  if (search.value && !b.studentName.includes(search.value) && !b.studentId.includes(search.value)) return false
  if (statusFilter.value && b.status !== statusFilter.value) return false
  if (dateFilter.value && activeTab.value === 'all' && b.date !== dateFilter.value) return false
  return true
}))

watch([activeTab, search, statusFilter, dateFilter], () => { currentPage.value = 1 })

const pagedBookings = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredBookings.value.slice(start, start + pageSize.value)
})

function switchTab(val) {
  activeTab.value = val
  if (val !== 'all') dateFilter.value = ''
}

function statusText(s) {
  return { confirmed: '待签到', checkedin: '已签到', cancelled: '已取消', missed: '已违约' }[s] || s
}

function statusTagType(s) {
  return { confirmed: 'primary', checkedin: 'success', cancelled: 'info', missed: 'danger' }[s] || ''
}

async function cancelBooking(b) {
  try {
    await ElMessageBox.confirm(`确定取消 ${b.studentName} 的预约吗？`, '确认取消', { type: 'warning' })
    b.status = 'cancelled'
    ElMessage.success('预约已取消')
  } catch {}
}

async function manualCheckIn(b) {
  b.status = 'checkedin'
  ElMessage.success('手动签到成功')
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
