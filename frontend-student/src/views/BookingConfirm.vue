<template>
  <div class="booking-page">
    <div class="page-header">
      <el-button text @click="router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>预约确认</h2>
    </div>

    <div class="booking-body">
      <!-- Step 1: Selected Info -->
      <div class="content-card selected-card">
        <h3>已选信息</h3>
        <div class="selected-detail">
          <div class="detail-item">
            <span class="d-label">自习室</span>
            <span class="d-value">{{ route.params.roomId }}</span>
          </div>
          <div class="detail-item">
            <span class="d-label">座位号</span>
            <span class="d-value seat-code">{{ route.params.seatId }}</span>
          </div>
        </div>
      </div>

      <!-- Step 2: Date -->
      <div class="content-card">
        <h3>选择日期</h3>
        <el-date-picker
          v-model="selectedDate"
          type="date"
          placeholder="选择日期"
          :disabled-date="disabledDate"
          value-format="YYYY-MM-DD"
          style="width: 240px"
        />
      </div>

      <!-- Step 3: Time Slots -->
      <div class="content-card">
        <h3>选择时段 <span class="hint">（点击起始格和结束格，最多连续4小时）</span></h3>
        <div class="slot-grid" v-loading="slotsLoading">
          <div
            v-for="h in 15"
            :key="h"
            class="slot-cell"
            :class="{
              'slot-free': isFree(h),
              'slot-busy': !isFree(h) && !isOccupied(h),
              'slot-occupied': isOccupied(h),
              'slot-selected': isSelected(h),
            }"
            @click="toggleSlot(h)"
          >
            <span class="slot-hour">{{ h + 6 }}:00</span>
            <span class="slot-status-text">
              {{ isOccupied(h) ? '已占用' : isSelected(h) ? '已选' : isFree(h) ? '可选' : '' }}
            </span>
          </div>
        </div>
        <p class="slot-summary" v-if="startSlot && endSlot">
          已选：{{ startSlot + 6 }}:00 - {{ endSlot + 7 }}:00，共 {{ endSlot - startSlot + 1 }} 小时
        </p>
      </div>

      <!-- Submit -->
      <el-button
        type="primary"
        size="large"
        :loading="submitting"
        :disabled="!canSubmit"
        class="submit-btn"
        @click="handleSubmit"
      >
        {{ submitting ? '提交中...' : '确认预约' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSeatSlots } from '../api/rooms'
import { createReservation } from '../api/reservations'

const route = useRoute()
const router = useRouter()
const roomId = route.params.roomId
const seatId = route.params.seatId

const selectedDate = ref('')
const startSlot = ref(null)
const endSlot = ref(null)
const occupiedSlots = ref([])
const slotsLoading = ref(false)
const submitting = ref(false)

const maxHours = 4
const totalSlots = 15 // 07:00-21:00

function slotLabel(slot) {
  return `${String(slot + 6).padStart(2, '0')}:00`
}

function disabledDate(date) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const max = new Date(today)
  max.setDate(max.getDate() + 6)
  return date.getTime() < today.getTime() || date.getTime() > max.getTime()
}

function isOccupied(h) {
  return occupiedSlots.value.includes(h)
}

function isFree(h) {
  if (!startSlot.value) return !isOccupied(h)
  if (!endSlot.value) {
    // Only start selected, allow adjacent within maxHours
    if (h === startSlot.value) return true
    const dist = h - startSlot.value
    if (dist < 0 || dist > maxHours - 1) return false
    // Check no occupied between
    const lo = Math.min(h, startSlot.value)
    const hi = Math.max(h, startSlot.value)
    for (let i = lo; i <= hi; i++) {
      if (isOccupied(i)) return false
    }
    return true
  }
  return false
}

function isSelected(h) {
  if (startSlot.value === null) return false
  if (endSlot.value === null) {
    const lo = Math.min(h, startSlot.value)
    const hi = Math.max(h, startSlot.value)
    return h >= lo && h <= hi
  }
  return h >= startSlot.value && h <= endSlot.value
}

function toggleSlot(h) {
  if (isOccupied(h)) return
  if (!startSlot.value) {
    startSlot.value = h
    endSlot.value = null
  } else if (!endSlot.value) {
    if (h === startSlot.value) {
      startSlot.value = null
    } else {
      const lo = Math.min(h, startSlot.value)
      const hi = Math.max(h, startSlot.value)
      startSlot.value = lo
      endSlot.value = hi
    }
  } else {
    // Reset selection
    startSlot.value = h
    endSlot.value = null
  }
}

const canSubmit = computed(() =>
  selectedDate.value && startSlot.value !== null && endSlot.value !== null
)

async function handleSubmit() {
  if (!canSubmit.value) return
  submitting.value = true
  try {
    await createReservation({
      seatId: Number(seatId),
      date: selectedDate.value,
      startTime: slotLabel(startSlot.value),
      endTime: slotLabel(endSlot.value + 1),
    })
    ElMessage.success('预约成功！')
    router.push('/reservations')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

import { watch } from 'vue'
watch(selectedDate, async (d) => {
  if (!d) return
  slotsLoading.value = true
  startSlot.value = null
  endSlot.value = null
  try {
    const data = await getSeatSlots(seatId, d)
    occupiedSlots.value = (data.occupied || data.occupiedSlots || [])
  } catch {
    // ignore
  } finally {
    slotsLoading.value = false
  }
})
</script>

<style scoped>
.booking-body {
  max-width: 680px;
}

.selected-card .selected-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: #f8f9fc;
  border-radius: 8px;
}

.d-label {
  font-size: 13px;
  color: #8492a6;
}

.d-value {
  font-size: 14px;
  font-weight: 600;
  color: #1a202c;
}

.seat-code {
  font-size: 20px;
  color: #003893;
}

.content-card {
  margin-bottom: 16px;
}

.content-card h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 14px;
}

.content-card .hint {
  font-weight: 400;
  font-size: 12px;
  color: #a0aec0;
}

.slot-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 10px;
}

.slot-cell {
  padding: 16px 12px;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;
  border: 2px solid transparent;
}

.slot-free {
  background: #e1f3d8;
  color: #529b2e;
  border-color: #b3e19d;
}

.slot-free:hover {
  background: #b3e19d;
  transform: scale(1.03);
}

.slot-busy {
  background: #edeff2;
  color: #b0b8c5;
}

.slot-occupied {
  background: #fde2e2;
  color: #f56c6c;
  border-color: #fab6b6;
  cursor: not-allowed;
}

.slot-selected {
  background: #d9ecff;
  color: #003893;
  border-color: #003893;
  box-shadow: 0 0 0 3px rgba(0,56,147,0.12);
}

.slot-hour {
  display: block;
  font-weight: 600;
  font-size: 14px;
}

.slot-status-text {
  display: block;
  font-size: 11px;
  margin-top: 4px;
}

.slot-summary {
  margin-top: 14px;
  font-size: 14px;
  font-weight: 600;
  color: #003893;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 2px;
  border-radius: 12px;
}
</style>
