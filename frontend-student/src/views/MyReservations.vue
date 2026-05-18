<template>
  <div class="reservations-page">
    <div class="page-header">
      <el-icon class="page-icon" :size="22"><Calendar /></el-icon>
      <h2>我的预约</h2>
      <span class="page-subtitle">共 {{ total }} 条</span>
    </div>

    <!-- Status Tabs -->
    <div class="status-tabs">
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

    <div class="content-card" v-loading="loading">
      <el-table :data="records" stripe style="width: 100%">
        <el-table-column label="自习室" min-width="140">
          <template #default="{ row }">
            <span class="cell-room">{{ row.roomName || row.room_name || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="座位号" width="100">
          <template #default="{ row }">
            <span class="cell-seat">{{ row.seatCode || row.seat_code || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="日期" width="120">
          <template #default="{ row }">
            {{ row.date || row.reservation_date || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="时段" width="140">
          <template #default="{ row }">
            {{ row.startTime || row.start_time || '-' }} — {{ row.endTime || row.end_time || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" size="small">{{ statusText(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到码" width="110">
          <template #default="{ row }">
            <span v-if="rowStatus(row) === 'pending_checkin'" class="checkin-code">
              {{ row.checkinCode || row.checkin_code || row.code || '------' }}
            </span>
            <span v-else style="color:#a0aec0">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="rowStatus(row) === 'pending_checkin'"
              type="danger"
              text
              size="small"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
            <span v-else style="color:#a0aec0">-</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        class="pagination"
        @change="fetchData"
      />
      <el-empty v-if="!loading && records.length === 0" description="暂无预约记录" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getMyReservations, cancelReservation } from '../api/reservations'

const tabs = [
  { label: '全部', value: '' },
  { label: '待签到', value: 'pending_checkin' },
  { label: '已签到', value: 'checked_in' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' },
]

const currentTab = ref('')
const records = ref([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const loading = ref(false)

function rowStatus(row) {
  return row.status || row.reservation_status || ''
}

function statusText(row) {
  const map = {
    pending_checkin: '待签到',
    checked_in: '已签到',
    completed: '已完成',
    cancelled: '已取消',
    violated: '已违约',
  }
  return map[rowStatus(row)] || rowStatus(row) || '-'
}

function statusType(row) {
  const map = {
    pending_checkin: 'warning',
    checked_in: 'primary',
    completed: 'success',
    cancelled: 'info',
    violated: 'danger',
  }
  return map[rowStatus(row)] || 'info'
}

function switchTab(val) {
  currentTab.value = val
  page.value = 1
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getMyReservations({
      page: page.value,
      pageSize: pageSize.value,
      status: currentTab.value,
    })
    records.value = data.records || []
    total.value = data.total || 0
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消这个预约吗？', '取消预约', {
      confirmButtonText: '确定取消',
      cancelButtonText: '再想想',
      type: 'warning',
    })
    await cancelReservation(row.id)
    ElMessage.success('预约已取消')
    fetchData()
  } catch {
    // cancelled or error handled
  }
}

onMounted(fetchData)
</script>

<style scoped>
.status-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 10px;
  padding: 4px;
  box-shadow: 0 2px 12px rgba(0,56,147,0.06);
  display: inline-flex;
}

.tab-item {
  padding: 8px 20px;
  border-radius: 8px;
  font-size: 13px;
  color: #4a5568;
  cursor: pointer;
  transition: all 0.15s;
}

.tab-item:hover {
  color: #003893;
  background: #eef2fa;
}

.tab-item.active {
  background: #003893;
  color: #fff;
  font-weight: 600;
}

.cell-room {
  font-weight: 500;
}

.cell-seat {
  font-weight: 600;
  color: #003893;
}

.checkin-code {
  font-size: 16px;
  font-weight: 700;
  color: #003893;
  letter-spacing: 3px;
  font-family: 'Courier New', monospace;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
