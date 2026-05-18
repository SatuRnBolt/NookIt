<template>
  <div class="room-detail-page">
    <!-- Back + Info header -->
    <div class="detail-header">
      <el-button text @click="router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        返回
      </el-button>
      <div class="header-info">
        <h2>{{ room.roomName || room.name || room.room_name || '加载中...' }}</h2>
        <p>{{ room.location || room.location_detail || '' }}</p>
      </div>
      <div class="header-tags">
        <el-tag type="success" v-if="room.room_status === 'active' || room.status === 'active'">开放中</el-tag>
        <el-tag v-else type="info">已关闭</el-tag>
      </div>
    </div>

    <div class="detail-body">
      <!-- Left: Seat Map -->
      <div class="seatmap-panel" v-loading="mapLoading">
        <div class="seatmap-toolbar">
          <span class="toolbar-title">座位地图</span>
          <div class="toolbar-filters">
            <el-checkbox v-model="filterPower" size="small">电源</el-checkbox>
            <el-checkbox v-model="filterWindow" size="small">靠窗</el-checkbox>
          </div>
          <div class="toolbar-legend">
            <span class="legend-dot" style="background:#67c23a"></span>可选
            <span class="legend-dot" style="background:#c0c4cc"></span>已占
            <span class="legend-dot" style="background:#f56c6c"></span>维护
            <span class="legend-dot" style="background:#409eff;border:2px solid #003893;"></span>已选
          </div>
        </div>

        <div class="seatmap-container" ref="mapContainer" :style="containerStyle">
          <div
            class="seatmap-area"
            :style="mapStyle"
            v-if="!mapLoading && seats.length > 0"
          >
            <div
              v-for="seat in filteredSeats"
              :key="seat.id"
              class="seat-block"
              :class="{
                'seat-available': isSeatAvailable(seat),
                'seat-occupied': isSeatOccupied(seat),
                'seat-maintenance': isSeatMaintenance(seat),
                'seat-selected': selectedSeat?.id === seat.id,
              }"
              :style="seatStyle(seat)"
              :title="seat.display_label || seat.seat_code"
              @click="selectSeat(seat)"
            >
              {{ seat.display_label || seat.seat_code }}
            </div>
          </div>
          <el-empty v-if="!mapLoading && seats.length === 0" description="暂无座位数据" />
        </div>
      </div>

      <!-- Right: Info + Action Panel -->
      <div class="info-panel">
        <div class="panel-card">
          <h3>自习室信息</h3>
          <div class="info-row">
            <span class="info-label">开放时间</span>
            <span class="info-value">{{ room.openTime || room.open_time || '07:00 - 22:00' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">总座位数</span>
            <span class="info-value">{{ room.totalCapacity || room.total_capacity || seats.length }} 座</span>
          </div>
          <div class="info-row">
            <span class="info-label">可用座位</span>
            <span class="info-value" style="color:#67c23a;font-weight:600">{{ availableCount }} 座</span>
          </div>
        </div>

        <div class="panel-card" v-if="selectedSeat">
          <h3>已选座位</h3>
          <div class="selected-seat-info">
            <div class="selected-label">{{ selectedSeat.display_label || selectedSeat.seat_code }}</div>
            <div class="selected-tags">
              <el-tag v-if="selectedSeat.has_power || selectedSeat.hasPower" size="small" type="success">电源</el-tag>
              <el-tag v-if="selectedSeat.is_window_side || selectedSeat.nearWindow" size="small">靠窗</el-tag>
              <el-tag v-if="selectedSeat.is_accessible || selectedSeat.accessible" size="small" type="warning">无障碍</el-tag>
            </div>
          </div>
          <el-button type="primary" size="large" class="book-btn" @click="openBookModal">
            立即预约
          </el-button>
        </div>

        <div class="panel-card empty-hint" v-else>
          <el-icon :size="32" color="#c0c4cc"><Select /></el-icon>
          <p>请在左侧座位地图中<br/>点击选择一个座位</p>
        </div>
      </div>
    </div>
  </div>

  <!-- 预约模态框 -->
  <el-dialog
    v-model="showBookModal"
    width="460px"
    :show-close="false"
    @close="resetBook"
  >
    <template #header>
      <div class="bk-header">
        <div class="bk-seat-badge">{{ selectedSeat?.display_label || selectedSeat?.seat_code }}</div>
        <div class="bk-header-info">
          <div class="bk-room-name">{{ room.roomName || room.name || room.room_name }}</div>
          <div class="bk-seat-tags">
            <el-tag v-if="selectedSeat?.has_power || selectedSeat?.hasPower" size="small" type="success" effect="plain">电源</el-tag>
            <el-tag v-if="selectedSeat?.is_window_side || selectedSeat?.nearWindow" size="small" effect="plain">靠窗</el-tag>
            <el-tag v-if="selectedSeat?.is_accessible || selectedSeat?.accessible" size="small" type="warning" effect="plain">无障碍</el-tag>
          </div>
        </div>
        <el-button class="bk-close" text @click="showBookModal = false">
          <el-icon :size="16"><Close /></el-icon>
        </el-button>
      </div>
    </template>

    <div class="bk-body">
      <!-- 日期 -->
      <div class="bk-field">
        <label class="bk-label">预约日期</label>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="请选择日期"
          :disabled-date="disabledDate"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </div>

      <!-- 时段 -->
      <div class="bk-field" v-loading="slotsLoading">
        <label class="bk-label">
          预约时段
          <span class="bk-hint">最多 4 小时</span>
        </label>
        <div class="bk-time-row">
          <el-select v-model="bookStart" placeholder="开始时间" style="flex:1" @change="bookEnd = null">
            <el-option
              v-for="o in startOptions"
              :key="o.value"
              :label="o.label"
              :value="o.value"
              :disabled="o.disabled"
            />
          </el-select>
          <span class="bk-time-sep">→</span>
          <el-select v-model="bookEnd" placeholder="结束时间" style="flex:1" :disabled="bookStart === null">
            <el-option
              v-for="o in endOptions"
              :key="o.value"
              :label="o.label"
              :value="o.value"
              :disabled="o.disabled"
            />
          </el-select>
        </div>
        <div class="bk-duration" v-if="bookStart !== null && bookEnd !== null">
          共 {{ (bookEnd - bookStart) * 0.5 }} 小时
        </div>
      </div>
    </div>

    <template #footer>
      <div class="bk-footer">
        <el-button @click="showBookModal = false" style="flex:1">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="!canSubmit"
          @click="handleSubmit"
          style="flex:2"
        >
          {{ submitting ? '提交中...' : '确认预约' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Select, Close } from '@element-plus/icons-vue'
import { getRoomDetail, getSeatMap, getSeatSlots } from '../api/rooms'
import { createReservation } from '../api/reservations'

const route = useRoute()
const router = useRouter()
const roomId = route.params.id

const room = ref({})
const seats = ref([])
const selectedSeat = ref(null)
const mapLoading = ref(false)
const filterPower = ref(false)
const filterWindow = ref(false)

const MAP_W = ref(1200)
const MAP_H = ref(800)
const SCALE = ref(1)

const mapStyle = computed(() => ({
  width: MAP_W.value + 'px',
  height: MAP_H.value + 'px',
  transform: `scale(${SCALE.value})`,
  transformOrigin: 'top left',
}))

// 容器尺寸精确匹配缩放后的地图，不留空白
const containerStyle = computed(() => ({
  width:  Math.round(MAP_W.value * SCALE.value) + 32 + 'px',
  height: Math.round(MAP_H.value * SCALE.value) + 32 + 'px',
}))

const availableCount = computed(() =>
  seats.value.filter(s => isSeatAvailable(s)).length
)

const filteredSeats = computed(() => {
  return seats.value.filter(s => {
    if (filterPower.value && !(s.has_power || s.hasPower)) return false
    if (filterWindow.value && !(s.is_window_side || s.nearWindow)) return false
    return true
  })
})

function isSeatAvailable(seat) {
  const status = seat.seat_status || seat.seatStatus || seat.status
  const bookable = seat.is_bookable !== undefined ? seat.is_bookable : (seat.isBookable !== undefined ? seat.isBookable : true)
  return status === 'active' && bookable
}

function isSeatOccupied(seat) {
  return seat.occupied || false
}

function isSeatMaintenance(seat) {
  const status = seat.seat_status || seat.seatStatus || seat.status
  return status === 'maintenance'
}

function seatStyle(seat) {
  return {
    left: (seat.map_x || seat.mapX || 0) + 'px',
    top: (seat.map_y || seat.mapY || 0) + 'px',
    width: (seat.map_width || seat.mapWidth || 48) + 'px',
    height: (seat.map_height || seat.mapHeight || 48) + 'px',
  }
}

function selectSeat(seat) {
  if (!isSeatAvailable(seat)) return
  selectedSeat.value = seat
}

// ── 预约 Modal ──
// 时间槽：index 0 = 7:00, 1 = 7:30, ..., 29 = 21:30, 30 = 22:00（仅作结束时间）
const SLOT_START = 7
const MAX_SLOTS  = 8  // 最多 4 小时 = 8 个半小时槽

function slotLabel(n) {
  const h = SLOT_START + Math.floor(n / 2)
  const m = n % 2 === 0 ? '00' : '30'
  return `${h}:${m}`
}

const showBookModal = ref(false)
const selectedDate  = ref('')
const bookStart     = ref(null)   // 0-29
const bookEnd       = ref(null)   // 1-30
const occupiedSlots = ref([])     // 已占用的半小时槽 index 列表
const slotsLoading  = ref(false)
const submitting    = ref(false)

// 开始时间选项：0-29 (7:00-21:30)
const startOptions = computed(() =>
  Array.from({ length: 30 }, (_, i) => ({
    value: i,
    label: slotLabel(i),
    disabled: occupiedSlots.value.includes(i),
  }))
)

// 结束时间选项：bookStart+1 到 bookStart+MAX_SLOTS，最多到 30 (22:00)
const endOptions = computed(() => {
  if (bookStart.value === null) return []
  return Array.from({ length: MAX_SLOTS }, (_, i) => {
    const e = bookStart.value + i + 1
    if (e > 30) return null
    // 检查范围内是否有占用
    const conflict = Array.from({ length: e - bookStart.value }, (_, j) => bookStart.value + j)
      .some(s => occupiedSlots.value.includes(s))
    return { value: e, label: slotLabel(e), disabled: conflict }
  }).filter(Boolean)
})

function openBookModal() {
  if (!selectedSeat.value) return
  resetBook()
  showBookModal.value = true
}

function resetBook() {
  selectedDate.value = ''
  bookStart.value    = null
  bookEnd.value      = null
  occupiedSlots.value = []
}

function disabledDate(date) {
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const max   = new Date(today); max.setDate(max.getDate() + 6)
  return date < today || date > max
}

const canSubmit = computed(() =>
  selectedDate.value && bookStart.value !== null && bookEnd.value !== null
)

async function handleSubmit() {
  submitting.value = true
  try {
    await createReservation({
      seatId:     Number(selectedSeat.value.id),
      date:       selectedDate.value,
      startTime:  slotLabel(bookStart.value),
      endTime:    slotLabel(bookEnd.value),
    })
    ElMessage.success('预约成功！')
    showBookModal.value = false
    router.push('/reservations')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

watch(selectedDate, async (d) => {
  if (!d) return
  slotsLoading.value = true
  bookStart.value = null; bookEnd.value = null
  try {
    const data = await getSeatSlots(selectedSeat.value.id, d)
    occupiedSlots.value = data.occupied || data.occupiedSlots || []
  } catch { /* ignore */ } finally {
    slotsLoading.value = false
  }
})

function computeScale() {
  const container = document.querySelector('.seatmap-container')
  if (container) {
    const cw = container.clientWidth - 32
    SCALE.value = Math.min(0.65, cw / MAP_W.value)
  }
}

async function fetchData() {
  mapLoading.value = true
  try {
    const [roomData, mapData] = await Promise.all([
      getRoomDetail(roomId),
      getSeatMap(roomId, new Date().toISOString().slice(0, 10)),
    ])
    room.value = roomData
    seats.value = mapData.seats || []
    MAP_W.value = mapData.map_width || mapData.mapWidth || 1200
    MAP_H.value = mapData.map_height || mapData.mapHeight || 800
    await nextTick()
    computeScale()
  } catch {
    // handled by interceptor
  } finally {
    mapLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', computeScale)
})
</script>

<style scoped>
.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.header-info h2 {
  font-size: 20px;
  font-weight: 700;
  color: #1a202c;
}

.header-info p {
  font-size: 13px;
  color: #8492a6;
  margin-top: 2px;
}

.detail-body {
  display: flex;
  gap: 24px;
  align-items: flex-start;
  justify-content: center;
}

/* Seat Map Panel */
.seatmap-panel {
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,56,147,0.06);
  overflow: hidden;
  width: fit-content;
}

.seatmap-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 20px;
  border-bottom: 1px solid #e8eaf2;
  flex-wrap: wrap;
}

.toolbar-title {
  font-weight: 600;
  font-size: 15px;
  color: #1a202c;
}

.toolbar-filters {
  display: flex;
  gap: 8px;
  margin-left: 16px;
}

.toolbar-legend {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px 12px;
  font-size: 12px;
  color: #8492a6;
  flex-wrap: wrap;
}

.legend-dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 3px;
  margin-right: 2px;
}

.seatmap-container {
  padding: 16px;
  overflow: hidden;
}

.seatmap-area {
  position: relative;
  background: #fafbfc;
  border: 1px dashed #dce1eb;
  border-radius: 8px;
}

.seat-block {
  position: absolute;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  user-select: none;
}

.seat-available {
  background: #e1f3d8;
  color: #529b2e;
  border: 1px solid #b3e19d;
}

.seat-available:hover {
  background: #b3e19d;
  transform: scale(1.08);
  z-index: 2;
}

.seat-occupied {
  background: #edeff2;
  color: #b0b8c5;
  border: 1px solid #dce1eb;
  cursor: not-allowed;
}

.seat-maintenance {
  background: #fde2e2;
  color: #f56c6c;
  border: 1px solid #fab6b6;
  cursor: not-allowed;
}

.seat-selected {
  background: #d9ecff;
  color: #003893;
  border: 2px solid #003893;
  box-shadow: 0 0 0 3px rgba(0,56,147,0.15);
  z-index: 3;
}

/* Info Panel */
.info-panel {
  width: 300px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0,56,147,0.06);
}

.panel-card h3 {
  font-size: 15px;
  font-weight: 600;
  color: #1a202c;
  margin-bottom: 14px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  font-size: 13px;
}

.info-label {
  color: #8492a6;
}

.info-value {
  color: #1a202c;
  font-weight: 500;
}

.selected-seat-info {
  text-align: center;
  margin-bottom: 16px;
}

.selected-label {
  font-size: 28px;
  font-weight: 700;
  color: #003893;
  margin-bottom: 8px;
}

.selected-tags {
  display: flex;
  gap: 6px;
  justify-content: center;
}

.book-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}

.empty-hint {
  text-align: center;
  padding: 40px 20px;
  color: #a0aec0;
}

.empty-hint p {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.6;
}

/* ── 预约 Modal ── */
.bk-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.bk-seat-badge {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  background: var(--nt-primary-light);
  border: 2px solid var(--nt-primary-border);
  color: var(--nt-primary-dark);
  font-size: 16px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.bk-header-info {
  flex: 1;
  min-width: 0;
}

.bk-room-name {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bk-seat-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.bk-close {
  flex-shrink: 0;
  color: #a0aec0 !important;
}

.bk-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 4px 0;
}

.bk-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.bk-label {
  font-size: 13px;
  font-weight: 600;
  color: #4a5568;
  display: flex;
  align-items: center;
  gap: 6px;
}

.bk-hint {
  font-weight: 400;
  font-size: 11px;
  color: #b0bac9;
}

.bk-time-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bk-time-sep {
  color: #c0c4cc;
  font-size: 16px;
  flex-shrink: 0;
}

.bk-duration {
  font-size: 13px;
  font-weight: 600;
  color: var(--nt-primary);
  padding: 8px 12px;
  background: var(--nt-primary-light);
  border-radius: 8px;
  border-left: 3px solid var(--nt-primary);
}

.bk-footer {
  display: flex;
  gap: 10px;
}
</style>
