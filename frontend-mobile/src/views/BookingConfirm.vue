<template>
  <div class="booking-page">
    <van-nav-bar title="预约确认" left-arrow @click-left="router.back()" />

    <!-- Scrollable body -->
    <div class="scroll-area">

      <!-- Selected Info -->
      <div class="card">
        <div class="section-title">已选信息</div>
        <div class="info-row">
          <span class="info-key">自习室</span>
          <span class="info-val">{{ route.params.roomId }}</span>
        </div>
        <div class="info-row">
          <span class="info-key">座位号</span>
          <span class="info-val seat-code">{{ route.params.seatId }}</span>
        </div>
      </div>

      <!-- Date Picker -->
      <div class="card picker-card" @click="showDatePicker = true">
        <div class="section-title">选择日期</div>
        <div class="pick-row">
          <span class="pick-val" :class="{ placeholder: !selectedDate }">
            {{ selectedDate || '点击选择日期' }}
          </span>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#c0c4cc" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
        </div>
      </div>

      <van-calendar
        v-model:show="showDatePicker"
        :min-date="minDate"
        :max-date="maxDate"
        color="#003893"
        teleport=".phone-screen"
        @confirm="onDateConfirm"
      />

      <!-- Time Slots -->
      <div class="card" v-if="selectedDate">
        <div class="section-title">
          选择时段
          <span class="hint">最多连续4小时</span>
        </div>
        <div v-if="slotsLoading" style="display:flex;justify-content:center;padding:16px">
          <van-loading size="28" color="#003893" />
        </div>
        <div v-else class="slot-grid">
          <div
            v-for="h in 15"
            :key="h"
            class="slot-cell"
            :class="slotClass(h)"
            @click="toggleSlot(h)"
          >
            <span class="slot-hour">{{ h + 6 }}:00</span>
          </div>
        </div>
        <div class="slot-summary" v-if="startSlot !== null && endSlot !== null">
          {{ startSlot + 6 }}:00 — {{ endSlot + 7 }}:00，共 {{ endSlot - startSlot + 1 }} 小时
        </div>
      </div>

      <div style="height: 8px"></div>
    </div>

    <!-- Submit bar — flex item, not fixed -->
    <div class="submit-bar">
      <van-button
        block
        round
        type="primary"
        color="linear-gradient(135deg, #003893 0%, #1a5cc8 100%)"
        :loading="submitting"
        :disabled="!canSubmit"
        loading-text="提交中..."
        @click="handleSubmit"
      >
        {{ canSubmit ? '确认预约' : '请选择日期和时段' }}
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showSuccessToast } from 'vant'
import { createReservation } from '../api/reservations'
import { getSeatSlots } from '../api/rooms'

const router = useRouter()
const route = useRoute()

const selectedDate = ref('')
const showDatePicker = ref(false)
const slots = ref([])
const slotsLoading = ref(false)
const startSlot = ref(null)
const endSlot = ref(null)
const submitting = ref(false)

const minDate = new Date()
const maxDate = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)

const canSubmit = computed(() =>
  selectedDate.value && startSlot.value !== null && endSlot.value !== null
)

function onDateConfirm(date) {
  selectedDate.value = date.toISOString().split('T')[0]
  showDatePicker.value = false
  startSlot.value = null
  endSlot.value = null
}

function slotClass(h) {
  const slot = slots.value.find(s => s.hour === h + 6)
  const isFree = !slot || slot.status === 'free'
  if (!isFree) return 'slot-busy'
  if (startSlot.value !== null && endSlot.value !== null &&
      h >= startSlot.value && h <= endSlot.value) return 'slot-selected'
  return 'slot-free'
}

function toggleSlot(h) {
  const slot = slots.value.find(s => s.hour === h + 6)
  if (slot && slot.status !== 'free') return
  if (startSlot.value === null) {
    startSlot.value = h
    endSlot.value = h
  } else if (h < startSlot.value) {
    startSlot.value = h
  } else {
    endSlot.value = Math.min(h, startSlot.value + 3)
  }
}

watch(selectedDate, async (date) => {
  if (!date) return
  slotsLoading.value = true
  try {
    const data = await getSeatSlots(route.params.seatId, date)
    slots.value = data || []
  } catch {
    slots.value = []
  } finally {
    slotsLoading.value = false
  }
})

async function handleSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await createReservation({
      seatId: route.params.seatId,
      date: selectedDate.value,
      startHour: startSlot.value + 6,
      endHour: endSlot.value + 7,
    })
    showSuccessToast('预约成功！')
    setTimeout(() => router.replace('/reservations'), 1500)
  } catch { /* toast shown by request.js */ }
  finally { submitting.value = false }
}
</script>

<style scoped>
.booking-page {
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
  padding-top: 12px;
}

.picker-card {
  cursor: pointer;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  color: #4a5568;
  margin-bottom: 12px;
}

.hint {
  font-size: 11px;
  color: #a0aec0;
  font-weight: 400;
  margin-left: 6px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f2f8;
}

.info-row:last-child { border-bottom: none; }
.info-key { font-size: 13px; color: #6b7a99; }
.info-val { font-size: 13px; color: #1a202c; font-weight: 500; }
.seat-code { font-size: 16px; font-weight: 800; color: #003893; letter-spacing: 2px; }

.pick-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pick-val { font-size: 15px; font-weight: 600; color: #1a202c; }
.pick-val.placeholder { color: #c0c4cc; font-weight: 400; }

.slot-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 12px;
}

.slot-cell {
  border-radius: 10px;
  padding: 10px 0;
  text-align: center;
  cursor: pointer;
}

.slot-cell:active { opacity: 0.7; }

.slot-hour {
  display: block;
  font-size: 12px;
  font-weight: 600;
}

.slot-free { background: #f0fdf4; border: 1.5px solid #86efac; color: #15803d; }
.slot-busy { background: #f3f4f6; border: 1.5px solid #e5e7eb; color: #9ca3af; cursor: not-allowed; }
.slot-selected { background: #003893; border: 1.5px solid #003893; color: #fff; }

.slot-summary {
  text-align: center;
  font-size: 13px;
  color: #003893;
  font-weight: 600;
  padding: 8px;
  background: #eef4ff;
  border-radius: 8px;
}

/* Submit bar — no position:fixed */
.submit-bar {
  flex-shrink: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 16px rgba(0,56,147,0.1);
}
</style>
