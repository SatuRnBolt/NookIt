<template>
  <div class="room-detail-page">
    <van-nav-bar
      :title="room.roomName || room.room_name || '自习室详情'"
      left-arrow
      @click-left="router.back()"
    />

    <div v-if="pageLoading" class="center-loading">
      <van-loading size="32" color="#003893" />
    </div>

    <template v-else>
      <div class="scroll-area">

        <!-- Room Info -->
        <div class="card room-info-card">
          <div class="room-header">
            <div>
              <div class="room-name">{{ room.roomName || room.room_name || room.name }}</div>
              <div class="room-location">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/><circle cx="12" cy="10" r="3"/></svg>
                {{ room.building || room.campus || room.location_detail || '—' }}
              </div>
            </div>
            <span class="room-status-tag" :class="isOpen ? 'open' : 'closed'">
              {{ isOpen ? '开放中' : '已关闭' }}
            </span>
          </div>
          <div class="room-meta-row">
            <div class="meta-box">
              <div class="meta-val">{{ room.totalCapacity || room.total_capacity || seats.length }}</div>
              <div class="meta-key">总座位</div>
            </div>
            <div class="meta-box">
              <div class="meta-val" style="color:#16a34a">{{ availableCount }}</div>
              <div class="meta-key">可用</div>
            </div>
            <div class="meta-box">
              <div class="meta-val">{{ room.openTime || room.open_time || '07:00' }}</div>
              <div class="meta-key">开放时间</div>
            </div>
          </div>
        </div>

        <!-- Filter + Legend -->
        <div class="filter-legend-row">
          <div class="filter-chips">
            <span class="filter-chip" :class="{ active: filterPower }" @click="filterPower = !filterPower">⚡ 有电源</span>
            <span class="filter-chip" :class="{ active: filterWindow }" @click="filterWindow = !filterWindow">🪟 靠窗</span>
          </div>
          <div class="legend">
            <span class="legend-item"><span class="dot dot-avail"></span>可选</span>
            <span class="legend-item"><span class="dot dot-occ"></span>已占</span>
            <span class="legend-item"><span class="dot dot-maint"></span>维护</span>
            <span class="legend-item"><span class="dot dot-sel"></span>已选</span>
          </div>
        </div>

        <!-- Seat Map -->
        <div class="seatmap-wrap" ref="mapWrapRef">
          <div v-if="mapLoading" class="center-loading" style="padding:40px 0">
            <van-loading size="28" color="#003893" />
          </div>
          <template v-else-if="seats.length > 0">
            <!-- scaled outer container -->
            <div class="seatmap-outer" :style="outerStyle">
              <div class="seatmap-area" :style="areaStyle">
                <div
                  v-for="seat in seats"
                  :key="seat.id"
                  class="seat-block"
                  :class="seatClass(seat)"
                  :style="seatBlockStyle(seat)"
                  @click="selectSeat(seat)"
                >
                  {{ seat.display_label || seat.seat_code }}
                </div>
              </div>
            </div>
          </template>
          <van-empty v-else description="暂无座位数据" image-size="70" />
        </div>

        <div style="height: 8px"></div>
      </div>

      <!-- Action Bar -->
      <div class="action-bar" v-if="selectedSeat">
        <div class="selected-info">
          <div class="selected-label">已选座位</div>
          <div class="selected-code">{{ selectedSeat.display_label || selectedSeat.seat_code }}</div>
          <div class="selected-tags">
            <span class="tag" v-if="selectedSeat.has_power">⚡电源</span>
            <span class="tag" v-if="selectedSeat.is_window_side">🪟靠窗</span>
          </div>
        </div>
        <van-button
          type="primary"
          round
          color="linear-gradient(135deg, #003893 0%, #1a5cc8 100%)"
          @click="showBookSheet = true"
        >
          立即预约
        </van-button>
      </div>
    </template>

    <!-- Booking Bottom Sheet -->
    <van-popup v-model:show="showBookSheet" position="bottom" round teleport=".phone-screen" :style="{ paddingBottom: '20px' }">
      <div class="book-sheet">
        <div class="sheet-handle"></div>
        <div class="sheet-title">
          <span class="sheet-seat-badge">{{ selectedSeat?.display_label || selectedSeat?.seat_code }}</span>
          <span>{{ room.roomName || room.room_name || room.name }}</span>
        </div>

        <van-cell-group inset>
          <van-cell
            title="预约日期"
            :value="selectedDate || '请选择'"
            :value-class="selectedDate ? 'cell-val-filled' : 'cell-val-placeholder'"
            is-link
            @click="showCal = true"
          />
          <van-cell
            title="开始时间"
            :value="startTimeLabel || '请选择'"
            :value-class="startTimeLabel ? 'cell-val-filled' : 'cell-val-placeholder'"
            is-link
            @click="showStartPicker = true"
          />
          <van-cell
            title="结束时间"
            :value="endTimeLabel || '请选择'"
            :value-class="endTimeLabel ? 'cell-val-filled' : 'cell-val-placeholder'"
            is-link
            :clickable="bookStartIdx !== null"
            @click="bookStartIdx !== null && (showEndPicker = true)"
          />
        </van-cell-group>

        <div class="duration-bar" v-if="canSubmit">
          共 <strong>{{ duration }}</strong> 小时（{{ startTimeLabel }} — {{ endTimeLabel }}）
        </div>

        <div class="sheet-submit">
          <van-button
            block round type="primary"
            color="linear-gradient(135deg, #003893 0%, #1a5cc8 100%)"
            :disabled="!canSubmit"
            :loading="submitting"
            loading-text="提交中..."
            @click="handleSubmit"
          >
            确认预约
          </van-button>
        </div>
      </div>
    </van-popup>

    <!-- Calendar -->
    <van-calendar
      v-model:show="showCal"
      :min-date="minDate"
      :max-date="maxDate"
      color="#003893"
      teleport=".phone-screen"
      @confirm="onDateConfirm"
    />

    <!-- Start Time Picker -->
    <van-popup v-model:show="showStartPicker" position="bottom" round teleport=".phone-screen">
      <van-picker
        title="选择开始时间"
        :columns="startColumns"
        @confirm="onStartConfirm"
        @cancel="showStartPicker = false"
        confirm-button-color="#003893"
      />
    </van-popup>

    <!-- End Time Picker -->
    <van-popup v-model:show="showEndPicker" position="bottom" round teleport=".phone-screen">
      <van-picker
        title="选择结束时间"
        :columns="endColumns"
        @confirm="onEndConfirm"
        @cancel="showEndPicker = false"
        confirm-button-color="#003893"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast } from 'vant'
import { getRoomDetail, getSeatMap } from '../api/rooms'
import { createReservation } from '../api/reservations'
import defaultBg from '../assets/classroom_background.png'

function toLocalDateStr(date = new Date()) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

const router = useRouter()
const route = useRoute()

// ── Room & Seat data ──
const room = ref({})
const seats = ref([])
const selectedSeat = ref(null)
const pageLoading = ref(true)
const mapLoading = ref(false)
const filterPower = ref(false)
const filterWindow = ref(false)

// ── Map scaling ──
const mapWrapRef = ref(null)
const MAP_W = ref(1200)
const MAP_H = ref(800)
const SCALE = ref(1)
const mapBg = ref(defaultBg)

const isOpen = computed(() => {
  const s = room.value.room_status || room.value.status
  return s === 'active'
})

const availableCount = computed(() =>
  seats.value.filter(s => isSeatAvailable(s)).length
)

// 与桌面端保持一致的字段归一化
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
    map_x:        Number(pick('map_x', 'mapX') ?? 0),
    map_y:        Number(pick('map_y', 'mapY') ?? 0),
    map_width:    Number(pick('map_width', 'mapWidth') ?? 48),
    map_height:   Number(pick('map_height', 'mapHeight') ?? 48),
    map_rotation: Number(pick('map_rotation', 'mapRotation') ?? 0),
    seat_status:  status,
    is_bookable:  bookable === undefined ? true : Boolean(bookable),
    has_power:    Boolean(pick('has_power', 'hasPower')),
    is_window_side: Boolean(pick('is_window_side', 'nearWindow', 'isWindowSide')),
    is_accessible:  Boolean(pick('is_accessible', 'accessible', 'isAccessible')),
    occupied:       Boolean(pick('occupied')),
  }
}

function isSeatAvailable(seat) {
  return seat.seat_status === 'active' && seat.is_bookable && !seat.occupied
}

function seatClass(seat) {
  if (selectedSeat.value?.id === seat.id) return 'seat-selected'
  if (seat.seat_status === 'maintenance')  return 'seat-maintenance'
  if (!isSeatAvailable(seat))              return 'seat-occupied'
  // 筛选未命中时淡化
  if ((filterPower.value && !seat.has_power) || (filterWindow.value && !seat.is_window_side))
    return 'seat-available seat-dimmed'
  return 'seat-available'
}

function seatBlockStyle(seat) {
  return {
    left:      seat.map_x + 'px',
    top:       seat.map_y + 'px',
    width:     seat.map_width + 'px',
    height:    seat.map_height + 'px',
    transform: seat.map_rotation ? `rotate(${seat.map_rotation}deg)` : undefined,
  }
}

function selectSeat(seat) {
  if (!isSeatAvailable(seat)) return
  selectedSeat.value = selectedSeat.value?.id === seat.id ? null : seat
}

// Map container styles — same scale trick as desktop
const outerStyle = computed(() => ({
  width:  Math.round(MAP_W.value * SCALE.value) + 'px',
  height: Math.round(MAP_H.value * SCALE.value) + 'px',
  overflow: 'hidden',
}))

const areaStyle = computed(() => ({
  position: 'relative',
  width:  MAP_W.value + 'px',
  height: MAP_H.value + 'px',
  transform: `scale(${SCALE.value})`,
  transformOrigin: 'top left',
  backgroundImage: `url(${mapBg.value})`,
  backgroundSize: 'contain',
  backgroundRepeat: 'no-repeat',
  backgroundPosition: 'center',
}))

function computeScale() {
  const el = mapWrapRef.value
  if (!el) return
  const availW = el.clientWidth - 24  // 12px padding each side
  SCALE.value = Math.max(0.15, Math.min(availW / MAP_W.value, 1))
}

// ── Booking sheet ──
const SLOT_START = 7
const MAX_SLOTS  = 8  // 最多 4 小时 = 8 个半小时槽

function slotLabel(n) {
  const h = SLOT_START + Math.floor(n / 2)
  const m = n % 2 === 0 ? '00' : '30'
  return `${String(h).padStart(2, '0')}:${m}`
}

const showBookSheet   = ref(false)
const showCal         = ref(false)
const showStartPicker = ref(false)
const showEndPicker   = ref(false)
const selectedDate    = ref('')
const bookStartIdx    = ref(null)  // 0-29
const bookEndIdx      = ref(null)  // 1-30
const submitting      = ref(false)

const minDate = new Date()
const maxDate = new Date(Date.now() + 6 * 24 * 60 * 60 * 1000)

// 开始时间列：0-29
const startColumns = Array.from({ length: 30 }, (_, i) => ({
  text: slotLabel(i), value: i,
}))

// 结束时间列：依赖 startIdx
const endColumns = computed(() => {
  if (bookStartIdx.value === null) return []
  return Array.from({ length: MAX_SLOTS }, (_, i) => {
    const e = bookStartIdx.value + i + 1
    return e <= 30 ? { text: slotLabel(e), value: e } : null
  }).filter(Boolean)
})

const startTimeLabel = computed(() =>
  bookStartIdx.value !== null ? slotLabel(bookStartIdx.value) : ''
)
const endTimeLabel = computed(() =>
  bookEndIdx.value !== null ? slotLabel(bookEndIdx.value) : ''
)
const duration = computed(() => {
  if (bookStartIdx.value === null || bookEndIdx.value === null) return 0
  return (bookEndIdx.value - bookStartIdx.value) * 0.5
})
const canSubmit = computed(() =>
  selectedDate.value && bookStartIdx.value !== null && bookEndIdx.value !== null
)

function onDateConfirm(date) {
  selectedDate.value = toLocalDateStr(date)
  showCal.value = false
}

function onStartConfirm({ selectedOptions }) {
  bookStartIdx.value = selectedOptions[0].value
  bookEndIdx.value = null  // reset end when start changes
  showStartPicker.value = false
}

function onEndConfirm({ selectedOptions }) {
  bookEndIdx.value = selectedOptions[0].value
  showEndPicker.value = false
}

async function handleSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await createReservation({
      seatId:    selectedSeat.value.id,
      date:      selectedDate.value,
      startTime: slotLabel(bookStartIdx.value),
      endTime:   slotLabel(bookEndIdx.value),
    })
    showSuccessToast('预约成功！')
    showBookSheet.value = false
    setTimeout(() => router.replace('/reservations'), 1200)
  } catch { /* handled by interceptor */ }
  finally { submitting.value = false }
}

// ── Lifecycle ──
onMounted(async () => {
  try {
    const today = toLocalDateStr()
    const [roomData, mapData] = await Promise.all([
      getRoomDetail(route.params.id),
      getSeatMap(route.params.id, today).catch(() => null),
    ])
    room.value = roomData || {}
    seats.value = (mapData?.seats || []).map(normalizeSeat)
    MAP_W.value = mapData?.map_width  || mapData?.mapWidth  || 1200
    MAP_H.value = mapData?.map_height || mapData?.mapHeight || 800
    // 如果后端有自定义背景图则尝试加载，失败退回默认
    const bgUrl = mapData?.background_url || mapData?.backgroundUrl
    if (bgUrl) {
      const img = new Image()
      img.onload  = () => { mapBg.value = bgUrl }
      img.onerror = () => { mapBg.value = defaultBg }
      img.src = bgUrl
    }
  } finally {
    pageLoading.value = false          // v-else 模板此时才渲染
    await nextTick()                   // 等 seatmap-wrap ref 挂载
    computeScale()
  }
  window.addEventListener('resize', computeScale)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', computeScale)
})
</script>

<style scoped>
.room-detail-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--nt-bg);
  overflow: hidden;
}

.scroll-area {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.center-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 60px 0;
}

/* Room Info */
.room-info-card { margin: 12px; }

.room-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 14px;
}

.room-name { font-size: 16px; font-weight: 700; margin-bottom: 4px; }

.room-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7a99;
}

.room-status-tag {
  font-size: 11px;
  font-weight: 600;
  padding: 3px 9px;
  border-radius: 10px;
  flex-shrink: 0;
}
.room-status-tag.open   { background: #f0fdf4; color: #16a34a; }
.room-status-tag.closed { background: #f3f4f6; color: #9ca3af; }

.room-meta-row { display: flex; }

.meta-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  border-right: 1px solid #f0f2f8;
}
.meta-box:last-child { border-right: none; }
.meta-val { font-size: 18px; font-weight: 700; color: #1a202c; }
.meta-key { font-size: 11px; color: #a0aec0; }

/* Filter + Legend */
.filter-legend-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 12px 8px;
  flex-wrap: wrap;
  gap: 6px;
}

.filter-chips { display: flex; gap: 6px; }

.filter-chip {
  padding: 5px 11px;
  border-radius: 20px;
  background: #fff;
  border: 1.5px solid #e8eaf2;
  font-size: 12px;
  color: #6b7a99;
  cursor: pointer;
}
.filter-chip.active { background: #eef4ff; border-color: #003893; color: #003893; font-weight: 600; }

.legend { display: flex; gap: 8px; }

.legend-item {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 10px;
  color: #6b7a99;
}

.dot { width: 8px; height: 8px; border-radius: 2px; }
.dot-avail { background: #22c55e; }
.dot-occ   { background: #c0c4cc; }
.dot-maint { background: #f56c6c; }
.dot-sel   { background: #003893; }

/* Seat Map */
.seatmap-wrap {
  padding: 0 12px 12px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.seatmap-outer {
  margin: 0 auto;
}

.seatmap-area {
  background-color: #fafbfc;
  border: 1px dashed #dce1eb;
  border-radius: 8px;
}

.seat-block {
  position: absolute;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.12s, box-shadow 0.12s, opacity 0.12s;
  user-select: none;
  box-shadow: 0 1px 2px rgba(0,0,0,0.06);
}

.seat-available {
  background: #e1f3d8;
  color: #3d7a1f;
  border: 1px solid #b3e19d;
}
.seat-available:active { transform: scale(1.1); }

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
  box-shadow: 0 0 0 3px rgba(0,56,147,0.18);
  z-index: 5;
}

.seat-dimmed { opacity: 0.3; }

/* Action Bar */
.action-bar {
  flex-shrink: 0;
  background: #fff;
  padding: 10px 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 -2px 16px rgba(0,56,147,0.1);
}

.selected-info { display: flex; flex-direction: column; gap: 2px; }
.selected-label { font-size: 11px; color: #a0aec0; }
.selected-code  { font-size: 20px; font-weight: 800; color: #003893; letter-spacing: 1px; }

.selected-tags { display: flex; gap: 4px; margin-top: 2px; }

.tag {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 6px;
  background: #eef4ff;
  color: #003893;
}

/* Booking Sheet */
.book-sheet {
  padding: 0 0 4px;
}

.sheet-handle {
  width: 36px;
  height: 4px;
  background: #e8eaf2;
  border-radius: 2px;
  margin: 12px auto 16px;
}

.sheet-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px 14px;
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
}

.sheet-seat-badge {
  min-width: 40px;
  height: 40px;
  background: #eef4ff;
  border: 1.5px solid #c7d9f8;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 800;
  color: #003893;
  padding: 0 8px;
  flex-shrink: 0;
}

:deep(.cell-val-filled)      { color: #1a202c; font-weight: 500; }
:deep(.cell-val-placeholder) { color: #c0c4cc; }

.duration-bar {
  margin: 10px 16px;
  padding: 8px 14px;
  background: #eef4ff;
  border-radius: 10px;
  font-size: 13px;
  color: #1a5cc8;
  text-align: center;
}

.duration-bar strong { color: #003893; }

.sheet-submit {
  padding: 12px 16px 4px;
}
</style>
