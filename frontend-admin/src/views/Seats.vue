<template>
  <div>
    <div class="page-title">
      <el-icon><Grid /></el-icon> 座位管理
    </div>

    <div class="toolbar">
      <el-input v-model="search" placeholder="搜索座位号" prefix-icon="Search" clearable style="width:180px" />
      <el-select v-model="roomFilter" placeholder="按自习室筛选" clearable style="width:180px">
        <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width:120px">
        <el-option label="可用" value="available" />
        <el-option label="已预约" value="booked" />
        <el-option label="不可用" value="unavailable" />
      </el-select>
      <el-checkbox v-model="powerFilter">仅有插座</el-checkbox>
      <el-button type="primary" icon="Plus" @click="openDialog()">新增座位</el-button>
    </div>

    <div class="content-card">
      <el-table :data="filteredSeats" stripe style="width:100%" row-key="id">
        <el-table-column prop="seatNo" label="座位号" width="90" />
        <el-table-column prop="roomName" label="所属自习室" min-width="150" />
        <el-table-column label="有插座" width="90" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.hasPower" color="#f59e0b" :size="18"><SuccessFilled /></el-icon>
            <el-icon v-else color="#e5e7eb" :size="18"><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="靠窗" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.nearWindow" color="#3b82f6" :size="18"><SuccessFilled /></el-icon>
            <el-icon v-else color="#e5e7eb" :size="18"><CircleCloseFilled /></el-icon>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="seatStatusType(row.status)" size="small">{{ seatStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="登记日期" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'unavailable' ? 'success' : 'warning'"
              @click="toggleSeatStatus(row)"
            >
              {{ row.status === 'unavailable' ? '启用' : '停用' }}
            </el-button>
            <el-button link type="danger" @click="deleteSeat(row)">注销</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="table-footer">
        共 {{ total }} 个座位
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="editing ? '编辑座位' : '新增座位'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="所属自习室">
          <el-select v-model="form.roomId" placeholder="选择自习室" @change="onRoomChange">
            <el-option v-for="r in rooms" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="座位号"><el-input v-model="form.seatNo" placeholder="如 A01" /></el-form-item>
        <el-form-item label="有插座">
          <el-switch v-model="form.hasPower" />
        </el-form-item>
        <el-form-item label="靠窗">
          <el-switch v-model="form.nearWindow" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSeat">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSeats, updateSeatStatus, deleteSeat as deleteSeatApi, getRoomsSimple } from '../api/seats'

const seats = ref([])
const rooms = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const search = ref('')
const roomFilter = ref('')
const statusFilter = ref('')
const powerFilter = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)

const form = reactive({ roomId: '', roomName: '', seatNo: '', hasPower: false, nearWindow: false })

const filteredSeats = computed(() => seats.value)

async function loadSeats() {
  try {
    const data = await getSeats({
      page: currentPage.value, pageSize: pageSize.value,
      search: search.value, roomId: roomFilter.value,
      status: statusFilter.value, hasPower: powerFilter.value || undefined,
    })
    seats.value = data.records || []
    total.value = data.total || 0
  } catch { seats.value = [] }
}

onMounted(async () => {
  await loadSeats()
  try { rooms.value = await getRoomsSimple() } catch {}
})

watch([search, roomFilter, statusFilter, powerFilter], () => { currentPage.value = 1; loadSeats() })
watch([currentPage, pageSize], loadSeats)

function seatStatusText(s) {
  return { available: '可用', booked: '已预约', unavailable: '不可用' }[s] || s
}

function seatStatusType(s) {
  return { available: 'success', booked: 'primary', unavailable: 'info' }[s] || ''
}

function onRoomChange(id) {
  const r = rooms.value.find(r => r.id === id)
  form.roomName = r?.name || ''
}

function openDialog(seat = null) {
  editing.value = seat
  Object.assign(form, seat
    ? { roomId: seat.roomId, roomName: seat.roomName, seatNo: seat.seatNo, hasPower: seat.hasPower, nearWindow: seat.nearWindow }
    : { roomId: '', roomName: '', seatNo: '', hasPower: false, nearWindow: false })
  dialogVisible.value = true
}

function saveSeat() {
  ElMessage.info('请通过自习室管理页面的座位地图编辑功能管理座位')
  dialogVisible.value = false
}

async function toggleSeatStatus(seat) {
  const next = seat.status === 'unavailable' ? 'active' : 'inactive'
  try {
    await updateSeatStatus(seat.id, next)
    seat.status = seat.status === 'unavailable' ? 'available' : 'unavailable'
    ElMessage.success('状态已更新')
  } catch {}
}

async function deleteSeat(seat) {
  try {
    await ElMessageBox.confirm(`确定注销座位 ${seat.seatNo} 吗？`, '注销确认', { type: 'warning' })
    await deleteSeatApi(seat.id)
    ElMessage.success('座位已注销')
    loadSeats()
  } catch {}
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}

.table-footer {
  padding: 12px 20px;
  font-size: 13px;
  color: #6b7280;
  border-top: 1px solid #f3f4f6;
}
</style>
