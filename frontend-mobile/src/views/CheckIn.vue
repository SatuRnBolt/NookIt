<template>
  <div class="checkin-page">
    <div class="checkin-hero">
      <div class="hero-icon">
        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 11l3 3L22 4"/>
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
        </svg>
      </div>
      <div class="hero-title">到场签到</div>
      <div class="hero-sub">输入预约码，确认您已到达自习室</div>
    </div>

    <!-- Result area -->
    <div class="result-area" v-if="result">
      <div class="result-card" :class="result.success ? 'result-success' : 'result-fail'">
        <div class="result-icon">
          <svg v-if="result.success" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <svg v-else width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="12" cy="12" r="10"/>
            <line x1="12" y1="8" x2="12" y2="12"/>
            <line x1="12" y1="16" x2="12.01" y2="16"/>
          </svg>
        </div>
        <div class="result-title">{{ result.title }}</div>
        <div class="result-msg">{{ result.message }}</div>
        <div class="result-detail" v-if="result.detail">
          <div class="detail-row">
            <span>自习室</span>
            <span>{{ result.detail.roomName }}</span>
          </div>
          <div class="detail-row">
            <span>座位号</span>
            <span class="detail-seat">{{ result.detail.seatCode }}</span>
          </div>
          <div class="detail-row">
            <span>时间段</span>
            <span>{{ result.detail.time }}</span>
          </div>
        </div>
        <van-button
          round
          block
          plain
          :type="result.success ? 'primary' : 'default'"
          @click="reset"
          style="margin-top: 16px"
        >
          重新签到
        </van-button>
      </div>
    </div>

    <!-- Input area -->
    <div class="input-area" v-else>
      <div class="code-label">请输入 4 位签到码</div>

      <!-- Password-style code display -->
      <div class="code-display" @click="focusInput">
        <div
          v-for="i in 4"
          :key="i"
          class="code-cell"
          :class="{
            'code-cell-filled': code.length >= i,
            'code-cell-active': code.length === i - 1,
          }"
        >
          <span v-if="code.length >= i" class="code-char">{{ code[i - 1] }}</span>
          <span v-else-if="code.length === i - 1" class="code-cursor">|</span>
        </div>
      </div>

      <!-- Hidden real input for keyboard trigger -->
      <input
        ref="hiddenInput"
        class="hidden-input"
        v-model="code"
        inputmode="numeric"
        maxlength="4"
        autocomplete="one-time-code"
        @input="onInput"
      />

      <div class="hint-text">
        签到码可在「我的预约」页面的待签到记录中查看
      </div>

      <div class="action-btns">
        <van-button
          round
          block
          type="primary"
          color="linear-gradient(135deg, #003893 0%, #1a5cc8 100%)"
          :loading="loading"
          :disabled="code.length < 4"
          loading-text="签到中..."
          size="large"
          @click="handleCheckin"
        >
          {{ code.length < 4 ? `还需输入 ${4 - code.length} 位` : '确认签到' }}
        </van-button>

        <van-button
          v-if="code.length > 0"
          round
          block
          plain
          type="default"
          @click="code = ''"
          style="margin-top: 10px"
        >
          清空
        </van-button>
      </div>

      <!-- Quick keyboard hint -->
      <div class="keyboard-hint" @click="focusInput">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="6" width="20" height="12" rx="2"/><path d="M6 10h.01M10 10h.01M14 10h.01M18 10h.01M8 14h8"/></svg>
        点击调出键盘输入
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { checkinByCode } from '../api/reservations'

const code = ref('')
const loading = ref(false)
const result = ref(null)
const hiddenInput = ref(null)

function focusInput() {
  hiddenInput.value?.focus()
}

function onInput(e) {
  code.value = e.target.value.replace(/\D/g, '').slice(0, 4)
}

function reset() {
  result.value = null
  code.value = ''
}

async function handleCheckin() {
  if (code.value.length < 4 || loading.value) return
  loading.value = true
  try {
    const data = await checkinByCode(code.value)
    result.value = {
      success: true,
      title: '签到成功！',
      message: '已确认您到达自习室，请找到对应座位就座。',
      detail: data ? {
        roomName: data.roomName || data.room_name || '—',
        seatCode: data.seatCode || data.seat_code || '—',
        time: `${data.startTime || data.start_time || '—'} — ${data.endTime || data.end_time || '—'}`,
      } : null,
    }
  } catch (err) {
    result.value = {
      success: false,
      title: '签到失败',
      message: err?.message || '签到码不正确，请核对后重试',
      detail: null,
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.checkin-page {
  min-height: 100%;
  background: var(--nt-bg);
  padding-bottom: 20px;
}

.checkin-hero {
  background: linear-gradient(135deg, #001f6b 0%, #003893 60%, #1a5cc8 100%);
  padding: 36px 24px 48px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  color: #fff;
}

.hero-icon {
  width: 80px;
  height: 80px;
  background: rgba(255,255,255,0.15);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}

.hero-title {
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 2px;
}

.hero-sub {
  font-size: 13px;
  color: rgba(255,255,255,0.7);
}

.input-area {
  margin: -20px 16px 0;
  background: #fff;
  border-radius: 20px;
  padding: 24px 20px;
  box-shadow: 0 8px 30px rgba(0,56,147,0.12);
}

.code-label {
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  text-align: center;
  margin-bottom: 20px;
}

.code-display {
  display: flex;
  gap: 10px;
  justify-content: center;
  margin-bottom: 12px;
  cursor: text;
}

.code-cell {
  width: 44px;
  height: 54px;
  border-radius: 12px;
  border: 2px solid #e8eaf2;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8faff;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.code-cell-filled {
  border-color: #003893;
  background: #eef4ff;
}

.code-cell-active {
  border-color: #1a5cc8;
  box-shadow: 0 0 0 3px rgba(26,92,200,0.15);
  background: #fff;
}

.code-char {
  font-size: 22px;
  font-weight: 800;
  color: #003893;
  font-family: 'Courier New', monospace;
}

.code-cursor {
  font-size: 20px;
  color: #1a5cc8;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.hidden-input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
  width: 1px;
  height: 1px;
}

.hint-text {
  font-size: 12px;
  color: #a0aec0;
  text-align: center;
  margin-bottom: 24px;
  line-height: 1.6;
}

.action-btns {
  margin-bottom: 16px;
}

.keyboard-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #a0aec0;
  cursor: pointer;
  padding: 8px;
}

/* Result Area */
.result-area {
  margin: -20px 16px 0;
}

.result-card {
  border-radius: 20px;
  padding: 32px 24px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.1);
  text-align: center;
}

.result-success {
  background: #fff;
}

.result-fail {
  background: #fff;
}

.result-icon {
  margin-bottom: 12px;
}

.result-success .result-icon { color: #16a34a; }
.result-fail .result-icon { color: #ef4444; }

.result-title {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 6px;
}

.result-success .result-title { color: #16a34a; }
.result-fail .result-title { color: #ef4444; }

.result-msg {
  font-size: 13px;
  color: #6b7a99;
  margin-bottom: 16px;
  line-height: 1.6;
}

.result-detail {
  background: #f8faff;
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 4px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
  border-bottom: 1px solid #f0f2f8;
}

.detail-row:last-child { border-bottom: none; }

.detail-row span:first-child { color: #6b7a99; }
.detail-row span:last-child { font-weight: 600; color: #1a202c; }

.detail-seat {
  font-size: 16px !important;
  font-weight: 800 !important;
  color: #003893 !important;
  letter-spacing: 2px;
}
</style>
