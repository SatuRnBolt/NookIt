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
              v-for="seat in seats"
              :key="seat.id"
              class="seat-block"
              :class="{
                'seat-available': isSeatAvailable(seat),
                'seat-occupied': isSeatOccupied(seat),
                'seat-maintenance': isSeatMaintenance(seat),
                'seat-selected': selectedSeat?.id === seat.id,
                'seat-dimmed': isSeatDimmed(seat),
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
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Select, Close } from '@element-plus/icons-vue'
import { getRoomDetail, getSeatMap, getSeatSlots } from '../api/rooms'
import { createReservation } from '../api/reservations'
import defaultClassroomBg from '../assets/classroom_background.png'

const route = useRoute()
const router = useRouter()
const roomId = route.params.id

const room = ref({})
const seats = ref([])
const selectedSeat = ref(null)
const mapLoading = ref(false)
const filterPower = ref(false)
const filterWindow = ref(false)
const mapContainer = ref(null)

const MAP_W = ref(1200)
const MAP_H = ref(800)
const SCALE = ref(1)
const containerW = ref(0)
const mapBg = ref('')   // 教室背景图

// 后端字段命名混用（snake_case / camelCase），归一化为统一字段
function normalizeSeat(s) {
  const pick = (...keys) => {
    for (const k of keys) if (s[k] !== undefined && s[k] !== null) return s[k]
    return undefined
  }
  const status = pick('seat_status', 'seatStatus', 'status') ?? 'active'
  const bookable = pick('is_bookable', 'isBookable')
  return {
    ...s,
    seat_code: pick('seat_code', 'seatCode'),
    display_label: pick('display_label', 'displayLabel') ?? pick('seat_code', 'seatCode'),
    map_x: Number(pick('map_x', 'mapX') ?? 0),
    map_y: Number(pick('map_y', 'mapY') ?? 0),
    map_width: Number(pick('map_width', 'mapWidth') ?? 48),
    map_height: Number(pick('map_height', 'mapHeight') ?? 48),
    map_rotation: Number(pick('map_rotation', 'mapRotation') ?? 0),
    seat_status: status,
    is_bookable: bookable === undefined ? true : Boolean(bookable),
    has_power: Boolean(pick('has_power', 'hasPower')),
    is_window_side: Boolean(pick('is_window_side', 'nearWindow', 'isWindowSide')),
    is_accessible: Boolean(pick('is_accessible', 'accessible', 'isAccessible')),
    occupied: Boolean(pick('occupied')),
  }
}

const scaledW = computed(() => Math.round(MAP_W.value * SCALE.value))
const scaledH = computed(() => Math.round(MAP_H.value * SCALE.value))

const mapStyle = computed(() => ({
  width: MAP_W.value + 'px',
  height: MAP_H.value + 'px',
  transform: `scale(${SCALE.value})`,
  transformOrigin: 'top left',
  // 缩放后若有富余宽度则水平居中
  marginLeft: Math.max(0, (containerW.value - 32 - scaledW.value) / 2) + 'px',
  // 教室背景图（contain 不裁切不变形），座位叠在其上
  ...(mapBg.value
    ? {
        backgroundImage: `url(${mapBg.value})`,
        backgroundSize: 'contain',
        backgroundRepeat: 'no-repeat',
        backgroundPosition: 'center',
      }
    : {}),
}))

// 容器高度匹配缩放后的地图；宽度由 flex 布局撑满
const containerStyle = computed(() => ({
  height: scaledH.value + 32 + 'px',
}))

const availableCount = computed(() =>
  seats.value.filter(s => isSeatAvailable(s)).length
)

const anyFilterActive = computed(() => filterPower.value || filterWindow.value)

function seatMatchesFilter(seat) {
  if (filterPower.value && !seat.has_power) return false
  if (filterWindow.value && !seat.is_window_side) return false
  return true
}

function isSeatDimmed(seat) {
  return anyFilterActive.value && !seatMatchesFilter(seat)
}

function isSeatAvailable(seat) {
  return seat.seat_status === 'active' && seat.is_bookable
}

function isSeatOccupied(seat) {
  return seat.occupied
}

function isSeatMaintenance(seat) {
  return seat.seat_status === 'maintenance'
}

function seatStyle(seat) {
  return {
    left: seat.map_x + 'px',
    top: seat.map_y + 'px',
    width: seat.map_width + 'px',
    height: seat.map_height + 'px',
    transform: seat.map_rotation ? `rotate(${seat.map_rotation}deg)` : undefined,
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
  return `${String(h).padStart(2, '0')}:${m}`
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
      seatId:     selectedSeat.value.id,
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

// 后端可能返回不存在的背景图路径（如 /static/...），加载失败则回退到默认教室图
function resolveBackground(url) {
  mapBg.value = defaultClassroomBg
  if (!url) return
  const img = new Image()
  img.onload = () => { mapBg.value = url }
  img.onerror = () => { mapBg.value = defaultClassroomBg }
  img.src = url
}

function computeScale() {
  const el = mapContainer.value
  if (!el) return
  containerW.value = el.clientWidth
  const availW = el.clientWidth - 32
  // 适应宽度，但限制最大放大倍数与可视高度，避免过大
  const maxByHeight = (window.innerHeight * 0.72) / MAP_H.value
  const s = Math.min(availW / MAP_W.value, 1.15, maxByHeight)
  SCALE.value = Math.max(0.2, s)
}

async function fetchData() {
  mapLoading.value = true
  try {
    const [roomData, mapData] = await Promise.all([
      getRoomDetail(roomId),
      getSeatMap(roomId, new Date().toISOString().slice(0, 10)),
    ])
    room.value = roomData
    seats.value = (mapData.seats || []).map(normalizeSeat)
    MAP_W.value = mapData.map_width || mapData.mapWidth || 1200
    MAP_H.value = mapData.map_height || mapData.mapHeight || 800
    // 未设置或后端图加载失败时回退到默认教室图
    resolveBackground(mapData.background_url || mapData.backgroundUrl)
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

onBeforeUnmount(() => {
  window.removeEventListener('resize', computeScale)
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
}

/* Seat Map Panel */
.seatmap-panel {
  flex: 1;
  min-width: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0,56,147,0.06);
  overflow: hidden;
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
  position: relative;
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
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: clamp(11px, 1vw, 15px);
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, opacity 0.15s;
  user-select: none;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

.seat-available {
  background: #e1f3d8;
  color: #3d7a1f;
  border: 1px solid #b3e19d;
}

.seat-available:hover {
  background: #c7e8b3;
  transform: scale(1.08);
  box-shadow: 0 4px 12px rgba(82,155,46,0.25);
  z-index: 4;
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
  box-shadow: 0 0 0 3px rgba(0,56,147,0.18), 0 6px 16px rgba(0,56,147,0.25);
  z-index: 5;
}

/* 筛选未命中：淡化但保持布局 */
.seat-dimmed {
  opacity: 0.28;
  filter: saturate(0.5);
}

.seat-dimmed:hover {
  opacity: 0.55;
}

.seat-selected.seat-dimmed {
  opacity: 1;
  filter: none;
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
