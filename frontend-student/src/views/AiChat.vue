<template>
  <div class="ai-chat-page">
    <!-- History Sidebar -->
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <button class="new-conv-btn" @click="newConversation">
          <el-icon :size="13"><Plus /></el-icon>
          <span>新建对话</span>
        </button>
      </div>
      <div class="conv-list">
        <div
          v-if="conversations.length === 0 && !convLoading"
          class="conv-empty"
        >还没有历史对话</div>
        <div
          v-for="conv in conversations"
          :key="conv.id"
          :class="['conv-item', { 'conv-active': conv.id === activeConvId }]"
          @click="switchConversation(conv.id)"
        >
          <div class="conv-item-main">
            <div class="conv-item-title">
              <el-icon v-if="conv.pinned" class="conv-pin-icon" :size="11"><Top /></el-icon>
              <span class="conv-item-title-text">{{ conv.title || '新对话' }}</span>
            </div>
            <div class="conv-item-time">{{ conv.timeLabel }}</div>
          </div>
          <el-dropdown
            trigger="click"
            placement="bottom-end"
            @command="cmd => onConvCommand(cmd, conv)"
          >
            <span class="conv-item-more" title="更多操作" @click.stop>
              <el-icon :size="15"><MoreFilled /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="pin">
                  {{ conv.pinned ? '取消置顶' : '置顶' }}
                </el-dropdown-item>
                <el-dropdown-item command="rename">重命名</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- Main Chat Column -->
    <div class="chat-main">
      <!-- Chat Header -->
      <div class="chat-header">
        <button class="chat-back-btn" @click="router.back()">
          <el-icon :size="16"><ArrowLeft /></el-icon>
          <span>返回</span>
        </button>
        <div class="chat-header-info">
          <div class="chat-ai-avatar">
            <img src="../assets/deepseek-color.svg" style="width:22px;height:22px;" />
          </div>
          <div>
            <div class="chat-header-title">
              Nookit 智能体
              <span class="agent-online">
                <span class="online-dot"></span>在线
              </span>
            </div>
            <div class="chat-header-sub">智能操作 · 信息查询 · 违规申诉 · 预约助手</div>
          </div>
        </div>
      </div>

      <div
        v-if="activityVisible"
        :class="['chat-activity-bar', `chat-activity-${activityState}`]"
      >
        <span
          v-if="activityState === 'thinking' || activityState === 'tool' || activityState === 'streaming'"
          class="activity-spinner"
        ></span>
        <span v-else class="activity-dot"></span>
        <span class="activity-text">{{ activityText }}</span>
      </div>

      <!-- Messages -->
      <div class="chat-messages" ref="messagesEl">
        <template v-for="msg in messages" :key="msg.id">
          <!-- pending confirm card -->
          <div v-if="msg.role === 'pending'" class="chat-msg chat-msg-ai">
            <div class="pending-card" :class="`pending-${msg.pending.status}`">
              <div class="pending-header">
                <el-icon :size="14"><Warning /></el-icon>
                <span>{{ pendingHeaderText(msg.pending) }}</span>
              </div>
              <div v-if="pendingPrimaryInfo(msg.pending)" class="pending-primary">
                <div v-if="pendingPrimaryInfo(msg.pending).eyebrow" class="pending-primary-eyebrow">
                  {{ pendingPrimaryInfo(msg.pending).eyebrow }}
                </div>
                <div class="pending-primary-title">{{ pendingPrimaryInfo(msg.pending).title }}</div>
                <div v-if="pendingPrimaryInfo(msg.pending).meta" class="pending-primary-meta">
                  {{ pendingPrimaryInfo(msg.pending).meta }}
                </div>
              </div>
              <div v-if="pendingSecondaryEntries(msg.pending).length" class="pending-detail-list">
                <div
                  v-for="item in pendingSecondaryEntries(msg.pending)"
                  :key="item.label"
                  class="pending-detail-row"
                >
                  <span class="pending-detail-label">{{ item.label }}</span>
                  <span class="pending-detail-value">{{ item.value }}</span>
                </div>
              </div>
              <div v-if="msg.pending.status === 'pending'" class="pending-actions">
                <el-button size="small" plain :disabled="loading || pendingActionBusyId === msg.pending.action_id" @click="cancelAction(msg)">取消</el-button>
                <el-button size="small" type="primary" :disabled="loading || pendingActionBusyId === msg.pending.action_id" @click="confirmAction(msg)">确认执行</el-button>
              </div>
              <div v-else-if="msg.pending.status === 'confirmed'" class="pending-status-tag pending-status-ok">
                ✓ 已执行
              </div>
              <div v-else-if="msg.pending.status === 'failed'" class="pending-status-tag pending-status-failed">
                执行失败
              </div>
              <div v-else class="pending-status-tag pending-status-cancel">
                已取消
              </div>
            </div>
          </div>

          <!-- normal user/assistant message -->
          <div
            v-else
            :class="['chat-msg', msg.role === 'user' ? 'chat-msg-user' : 'chat-msg-ai']"
          >
            <!-- user: chat bubble; assistant: plain text on the canvas (no bubble / avatar) -->
            <div v-if="msg.role === 'user'" class="chat-bubble bubble-user">
              <div class="bubble-text">{{ msg.content }}</div>
              <div v-if="msg.time" class="bubble-time">{{ msg.time }}</div>
            </div>
            <div v-else class="ai-answer">
              <div class="bubble-text bubble-md">
                <div class="md-body" v-html="renderMarkdown(msg.content)"></div><span v-if="msg.streaming" class="cursor-blink">▍</span>
              </div>
              <div v-if="msg.time" class="bubble-time">{{ msg.time }}</div>
            </div>
          </div>
        </template>

        <!-- thinking indicator: only before the first token arrives -->
        <div v-if="loading && !hasStreamingAssistant" class="chat-msg chat-msg-ai">
          <div class="ai-typing">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="chat-input-area">
        <div class="chat-input-row">
          <el-input
            v-model="inputText"
            placeholder="告诉我你想完成的任务，或直接提问…"
            size="large"
            clearable
            :disabled="loading"
            class="chat-input"
            @keyup.enter="sendMessage"
          />
          <el-button
            v-if="loading"
            size="large"
            class="chat-send-btn"
            @click="abortStream"
          >停止</el-button>
          <el-button
            v-else
            type="primary"
            size="large"
            :disabled="!inputText.trim()"
            class="chat-send-btn"
            @click="sendMessage"
          >发送</el-button>
        </div>
        <div class="chat-input-tip">Enter 发送 · 本智能体仅供学习参考</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, nextTick, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Warning, MoreFilled, Top } from '@element-plus/icons-vue'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import {
  cancelPendingAction,
  chatStream,
  deleteConversation,
  getConversations,
  getMessages,
  updateConversation,
} from '../api/ai'
import { getSeatDetail } from '../api/rooms'
import { getReservationDetail } from '../api/reservations'

marked.setOptions({ breaks: true, gfm: true })

function renderMarkdown(text) {
  if (!text) return ''
  return DOMPurify.sanitize(marked.parse(text))
}

const router = useRouter()
const route = useRoute()

const messagesEl = ref(null)
const inputText = ref('')
const loading = ref(false)
const convLoading = ref(false)
const activityState = ref('idle')
const activityText = ref('')

const conversations = ref([])
const activeConvId = ref(null)  // null = brand-new (not yet persisted)
const messages = ref([welcomeMsg()])
const pendingSeatDetails = reactive({})
const pendingSeatDetailLoading = reactive({})
// Cancel cards only carry a reservationId; resolve the rest from the reservations API.
const pendingReservationDetails = reactive({})
const pendingReservationLoading = reactive({})
const pendingActionBusyId = ref(null)

let msgSeq = 0
const nextId = () => `m-${++msgSeq}`

let abortController = null

function createMessage(message) {
  return reactive(message)
}

function setActivity(state, text) {
  activityState.value = state
  activityText.value = text
}

function clearActivity() {
  activityState.value = 'idle'
  activityText.value = ''
}

// Typewriter buffer: smooths bursty upstream into per-character drip.
// Per-message state: { pending: '', timer: number|null, done: boolean }.
const typewriterState = new Map()
const TYPE_INTERVAL_MS = 18

function pushTokens(msg, text) {
  if (!text) return
  let state = typewriterState.get(msg.id)
  if (!state) {
    state = { pending: '', timer: null, done: false }
    typewriterState.set(msg.id, state)
  }
  state.pending += text
  ensureTypewriter(msg, state)
}

function ensureTypewriter(msg, state) {
  if (state.timer != null) return
  state.timer = window.setInterval(() => {
    if (state.pending.length === 0) {
      if (state.done) {
        window.clearInterval(state.timer)
        state.timer = null
        typewriterState.delete(msg.id)
      }
      return
    }
    // Burst-adaptive chunk size: drain faster when buffer is big so we never
    // fall too far behind, but still slow enough to feel like typing.
    const take = Math.max(1, Math.ceil(state.pending.length / 24))
    msg.content += state.pending.slice(0, take)
    state.pending = state.pending.slice(take)
    scrollToBottom()
  }, TYPE_INTERVAL_MS)
}

function finishTypewriter(msg) {
  const state = typewriterState.get(msg.id)
  if (!state) return
  state.done = true
  // If buffer already empty, ensureTypewriter's tick will clear the timer.
}

function flushTypewriter(msg) {
  // Force-drain everything immediately (e.g. on abort or unmount).
  const state = typewriterState.get(msg.id)
  if (!state) return
  if (state.pending) {
    msg.content += state.pending
    state.pending = ''
  }
  if (state.timer != null) {
    window.clearInterval(state.timer)
    state.timer = null
  }
  typewriterState.delete(msg.id)
}

const hasStreamingAssistant = computed(() =>
  messages.value.some(m => m.role === 'assistant' && m.streaming)
)

const activityVisible = computed(() => activityState.value !== 'idle' && !!activityText.value)

function welcomeMsg() {
  return createMessage({
    id: 'welcome',
    role: 'assistant',
    content: '你好！我是 Nookit 校园智能体。我能帮你查询自习室和座位、协助预约和取消、查看违规和申诉，以及解答各类自习室相关的问题。\n\n有什么想完成的任务，直接告诉我吧。',
    time: formatTime(new Date()),
  })
}

function formatTime(d) {
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function parseApiDateTime(value) {
  if (!value) return null
  if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value

  const text = String(value).trim().replace(' ', 'T')
  const naiveMatch = text.match(
    /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{1,3}))?)?$/
  )
  if (naiveMatch) {
    const [, year, month, day, hour, minute, second = '0', millis = '0'] = naiveMatch
    return new Date(
      Number(year),
      Number(month) - 1,
      Number(day),
      Number(hour),
      Number(minute),
      Number(second),
      Number(millis.padEnd(3, '0'))
    )
  }

  const parsed = new Date(text)
  return Number.isNaN(parsed.getTime()) ? null : parsed
}

function formatApiTime(value) {
  const d = parseApiDateTime(value)
  return d ? formatTime(d) : ''
}

function formatConvTime(iso) {
  if (!iso) return ''
  const d = parseApiDateTime(iso)
  if (!d) return ''
  const now = new Date()
  const sameDay =
    d.getFullYear() === now.getFullYear() &&
    d.getMonth() === now.getMonth() &&
    d.getDate() === now.getDate()
  if (sameDay) return `今天 ${formatTime(d)}`
  const yesterday = new Date(now)
  yesterday.setDate(now.getDate() - 1)
  const isYesterday =
    d.getFullYear() === yesterday.getFullYear() &&
    d.getMonth() === yesterday.getMonth() &&
    d.getDate() === yesterday.getDate()
  if (isYesterday) return `昨天 ${formatTime(d)}`
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

function pendingHeaderText(p) {
  if (p.status === 'confirmed') return '已执行的操作'
  if (p.status === 'failed') return '执行失败的操作'
  if (p.status === 'cancelled') return '已取消的操作'
  return '需要你确认的操作'
}

function pendingParamEntries(pending) {
  if (!pending?.params) return []
  if (pending.tool_name === 'create_reservation') {
    return createReservationParamEntries(pending)
  }
  return Object.entries(pending.params)
    .map(([key, value]) => ({ label: key, value: formatPendingValue(value) }))
    .filter(item => item.value)
}

function pendingPrimaryInfo(pending) {
  if (!pending) return null

  if (pending.tool_name === 'create_reservation') {
    const params = pending.params || {}
    const seatDetail = getCachedSeatDetail(params.seatId)
    const roomName = seatDetail?.roomDisplayName || seatDetail?.roomName || ''
    const seatLabel = seatDetail?.displayLabel || seatDetail?.seatCode
    const title = seatLabel ? `座位 ${seatLabel}` : params.seatId != null ? `座位 #${params.seatId}` : '预约座位'
    const metaParts = []

    if (params.date) metaParts.push(params.date)
    if (params.startTime && params.endTime) metaParts.push(`${params.startTime}-${params.endTime}`)
    const durationText = formatReservationDuration(params.startTime, params.endTime)
    if (durationText) metaParts.push(durationText)

    return {
      eyebrow: roomName,
      title,
      meta: metaParts.join(' · '),
    }
  }

  if (pending.tool_name === 'cancel_reservation') {
    const data = cancelReservationData(pending)
    const seatDetail = getCachedSeatDetail(data.seatId)
    const seatLabel = seatDetail?.displayLabel || seatDetail?.seatCode || data.seatCode
    const title = seatLabel
      ? `座位 ${seatLabel}`
      : data.reservationId != null ? `预约 #${data.reservationId}` : '取消当前预约'
    const metaParts = []
    if (data.date) metaParts.push(data.date)
    if (data.startTime && data.endTime) metaParts.push(`${data.startTime}-${data.endTime}`)
    const durationText = formatReservationDuration(data.startTime, data.endTime)
    if (durationText) metaParts.push(durationText)
    return {
      eyebrow: '取消预约',
      title,
      meta: metaParts.join(' · '),
    }
  }

  const entries = pendingParamEntries(pending)
  if (!entries.length) {
    return { eyebrow: '', title: '待确认操作', meta: '' }
  }

  const [first, ...rest] = entries
  return {
    eyebrow: '',
    title: `${first.label}: ${first.value}`,
    meta: rest.slice(0, 2).map(item => `${item.label}: ${item.value}`).join(' · '),
  }
}

function pendingSecondaryEntries(pending) {
  if (!pending?.params) return []
  if (pending.tool_name === 'create_reservation') {
    return createReservationSecondaryEntries(pending)
  }
  if (pending.tool_name === 'cancel_reservation') {
    return cancelReservationSecondaryEntries(pending)
  }
  return pendingParamEntries(pending).slice(1)
}

function createReservationParamEntries(pending) {
  const params = pending.params || {}
  const seatDetail = getCachedSeatDetail(params.seatId)
  const entries = []
  const roomName = seatDetail?.roomDisplayName || seatDetail?.roomName
  const seatLabel = seatDetail?.displayLabel || seatDetail?.seatCode
  const features = []

  if (roomName) entries.push({ label: '自习室', value: roomName })
  if (seatLabel) entries.push({ label: '座位', value: seatLabel })
  else if (params.seatId != null) entries.push({ label: '座位', value: `#${params.seatId}` })
  if (params.date) entries.push({ label: '日期', value: params.date })
  if (params.startTime && params.endTime) {
    entries.push({ label: '时段', value: `${params.startTime}-${params.endTime}` })
  }
  if (seatDetail?.hasPower) features.push('有电源')
  if (seatDetail?.isWindowSide) features.push('靠窗')
  if (seatDetail?.isAccessible) features.push('无障碍')
  if (features.length) entries.push({ label: '特征', value: features.join(' / ') })
  if (seatDetail?.locationDetail) entries.push({ label: '位置', value: seatDetail.locationDetail })

  return entries
}

function createReservationSecondaryEntries(pending) {
  const params = pending.params || {}
  const seatDetail = getCachedSeatDetail(params.seatId)
  const entries = []
  const features = []

  if (seatDetail?.seatType) entries.push({ label: '座位类型', value: seatDetail.seatType })
  if (seatDetail?.hasPower) features.push('有电源')
  if (seatDetail?.isWindowSide) features.push('靠窗')
  if (seatDetail?.isAccessible) features.push('无障碍')
  if (features.length) entries.push({ label: '特征', value: features.join(' / ') })
  if (seatDetail?.locationDetail) entries.push({ label: '位置', value: seatDetail.locationDetail })
  if (!seatDetail && params.seatId != null) entries.push({ label: '座位 ID', value: `#${params.seatId}` })

  return entries
}

function cancelReservationSecondaryEntries(pending) {
  const data = cancelReservationData(pending)
  const seatDetail = getCachedSeatDetail(data.seatId)
  const entries = []
  const roomName = seatDetail?.roomDisplayName || seatDetail?.roomName || data.roomName
  if (roomName) entries.push({ label: '自习室', value: roomName })
  if (seatDetail?.seatType) entries.push({ label: '座位类型', value: seatDetail.seatType })
  const features = []
  if (seatDetail?.hasPower) features.push('有电源')
  if (seatDetail?.isWindowSide) features.push('靠窗')
  if (seatDetail?.isAccessible) features.push('无障碍')
  if (features.length) entries.push({ label: '特征', value: features.join(' / ') })
  if (seatDetail?.locationDetail) entries.push({ label: '位置', value: seatDetail.locationDetail })
  if (data.reservationId != null) entries.push({ label: '预约编号', value: `#${data.reservationId}` })
  return entries
}

function formatReservationDuration(startTime, endTime) {
  if (!startTime || !endTime) return ''
  const start = parseClockMinutes(startTime)
  const end = parseClockMinutes(endTime)
  if (start == null || end == null || end <= start) return ''

  const durationMinutes = end - start
  const hours = Math.floor(durationMinutes / 60)
  const minutes = durationMinutes % 60
  if (minutes === 0) return `${hours}小时`
  if (hours === 0) return `${minutes}分钟`
  return `${hours}小时${minutes}分钟`
}

function parseClockMinutes(value) {
  if (typeof value !== 'string') return null
  const match = value.match(/^(\d{2}):(\d{2})$/)
  if (!match) return null
  return Number(match[1]) * 60 + Number(match[2])
}

function formatPendingValue(value) {
  if (value == null || value === '') return ''
  if (typeof value === 'object') return JSON.stringify(value)
  return String(value)
}

function normalizeSeatDetailKey(seatId) {
  const text = String(seatId ?? '').trim()
  return /^\d+$/.test(text) ? text : ''
}

function getCachedSeatDetail(seatId) {
  const key = normalizeSeatDetailKey(seatId)
  return key ? pendingSeatDetails[key] || null : null
}

async function ensureSeatDetailById(seatId) {
  const key = normalizeSeatDetailKey(seatId)
  if (!key || Object.prototype.hasOwnProperty.call(pendingSeatDetails, key) || pendingSeatDetailLoading[key]) {
    return
  }
  pendingSeatDetailLoading[key] = true
  try {
    pendingSeatDetails[key] = await getSeatDetail(key)
  } catch {
    pendingSeatDetails[key] = null
  } finally {
    pendingSeatDetailLoading[key] = false
  }
}

async function ensurePendingSeatDetail(pending) {
  if (pending?.tool_name !== 'create_reservation') return
  await ensureSeatDetailById(pending.params?.seatId)
}

function getCachedReservation(reservationId) {
  const key = normalizeSeatDetailKey(reservationId)
  return key ? pendingReservationDetails[key] || null : null
}

function pickReservationField(record, ...keys) {
  for (const key of keys) {
    const value = record?.[key]
    if (value != null && value !== '') return value
  }
  return undefined
}

function normalizeReservationDetail(record) {
  if (!record) return null
  return {
    id: pickReservationField(record, 'id'),
    seatId: pickReservationField(record, 'seatId', 'seat_id'),
    seatCode: pickReservationField(record, 'seatCode', 'seat_code'),
    roomName: pickReservationField(record, 'roomName', 'room_name'),
    date: pickReservationField(record, 'date', 'reservation_date'),
    startTime: pickReservationField(record, 'startTime', 'start_time'),
    endTime: pickReservationField(record, 'endTime', 'end_time'),
  }
}

/** Merge what the card already knows (params) with the reservation looked up by id. */
function cancelReservationData(pending) {
  const params = pending?.params || {}
  const resolved = getCachedReservation(params.reservationId) || {}
  return {
    reservationId: params.reservationId,
    seatId: params.seatId ?? resolved.seatId,
    seatCode: params.seatCode ?? resolved.seatCode,
    roomName: params.roomName ?? resolved.roomName,
    date: params.date ?? resolved.date,
    startTime: params.startTime ?? resolved.startTime,
    endTime: params.endTime ?? resolved.endTime,
  }
}

async function ensurePendingReservationDetail(pending) {
  if (pending?.tool_name !== 'cancel_reservation') return
  const key = normalizeSeatDetailKey(pending.params?.reservationId)
  if (!key) return

  if (!Object.prototype.hasOwnProperty.call(pendingReservationDetails, key) && !pendingReservationLoading[key]) {
    pendingReservationLoading[key] = true
    try {
      const rec = await getReservationDetail(key)
      pendingReservationDetails[key] = normalizeReservationDetail(rec)
    } catch {
      pendingReservationDetails[key] = null
    } finally {
      pendingReservationLoading[key] = false
    }
  }

  // Once the seat is known, hydrate seat detail too (type / features / location).
  const seatId = pendingReservationDetails[key]?.seatId ?? pending.params?.seatId
  if (seatId != null) await ensureSeatDetailById(seatId)
}

function hydratePendingCards(messageList) {
  for (const msg of messageList) {
    if (msg.role !== 'pending') continue
    void ensurePendingSeatDetail(msg.pending)
    void ensurePendingReservationDetail(msg.pending)
  }
}

function normalizeConvId(value) {
  const raw = Array.isArray(value) ? value[0] : value
  const num = Number(raw)
  return Number.isInteger(num) && num > 0 ? num : null
}

async function syncConversationQuery(conversationId) {
  const currentConv = Array.isArray(route.query.conv) ? route.query.conv[0] : route.query.conv
  const hasPromptQuery = route.query.q != null
  const nextConv = conversationId != null ? String(conversationId) : null
  if (currentConv === nextConv && !hasPromptQuery) return

  const query = { ...route.query }
  if (conversationId == null) delete query.conv
  else query.conv = String(conversationId)
  delete query.q
  await router.replace({ query })
}

function rememberConversation(conversationId) {
  if (conversationId == null) return
  activeConvId.value = conversationId
  void syncConversationQuery(conversationId)
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

// ── Conversation list ───────────────────────────────────────────────

async function loadConversations() {
  convLoading.value = true
  try {
    const pageSize = 100
    const records = []
    let page = 1
    let total = 0

    while (page === 1 || records.length < total) {
      const data = await getConversations({ page, pageSize })
      const batch = data.records || []
      total = data.total || 0
      records.push(...batch)
      if (batch.length === 0) break
      page += 1
    }

    conversations.value = records.map(r => ({
      id: r.id,
      title: r.title,
      pinned: !!r.is_pinned,
      timeLabel: formatConvTime(r.last_message_at || r.created_at),
    }))
  } catch {
    // request.js interceptor already handles
  } finally {
    convLoading.value = false
  }
}

async function loadConversationHistory(conversationId) {
  const chunks = []
  let beforeId = null

  while (true) {
    const page = await getMessages(conversationId, { limit: 200, beforeId })
    const records = page.records || []
    chunks.push(records)

    if (!page.has_more || page.next_before_id == null || records.length === 0) {
      break
    }
    beforeId = page.next_before_id
  }

  return chunks.reverse().flat()
}

async function switchConversation(id) {
  if (id === activeConvId.value || loading.value) return
  abortStream()
  clearActivity()
  activeConvId.value = id
  void syncConversationQuery(id)
  messages.value = [welcomeMsg()]

  try {
    const history = await loadConversationHistory(id)
    const historyMsgs = history.map(m => {
      if (m.role === 'pending') {
        return createMessage({
          id: nextId(),
          role: 'pending',
          pending: {
            action_id: m.pending?.action_id,
            tool_name: m.pending?.tool_name,
            summary: m.pending?.summary || '',
            params: m.pending?.params || {},
            status: m.pending?.status || 'pending',
          },
        })
      }
      return createMessage({
        id: nextId(),
        role: m.role,
        content: m.content,
        time: formatApiTime(m.created_at),
      })
    })
    messages.value = [welcomeMsg(), ...historyMsgs]
    hydratePendingCards(historyMsgs)
    await scrollToBottom()
  } catch {
    activeConvId.value = null
    messages.value = [welcomeMsg()]
    void syncConversationQuery(null)
  }
}

function newConversation() {
  abortStream()
  clearActivity()
  activeConvId.value = null
  messages.value = [welcomeMsg()]
  inputText.value = ''
  void syncConversationQuery(null)
}

// ── Conversation actions (置顶 / 重命名 / 删除) ───────────────────────

function onConvCommand(cmd, conv) {
  if (cmd === 'pin') togglePin(conv)
  else if (cmd === 'rename') renameConversation(conv)
  else if (cmd === 'delete') removeConversation(conv)
}

async function togglePin(conv) {
  const nextPinned = !conv.pinned
  try {
    await updateConversation(conv.id, { pinned: nextPinned })
    ElMessage.success(nextPinned ? '已置顶' : '已取消置顶')
    await loadConversations()
  } catch {
    // api layer already surfaced the error
  }
}

async function renameConversation(conv) {
  let value
  try {
    const res = await ElMessageBox.prompt('请输入新的对话名称', '重命名对话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: conv.title || '',
      inputValidator: v => (v && v.trim() ? true : '名称不能为空'),
      inputErrorMessage: '名称不能为空',
    })
    value = res.value
  } catch {
    return // user cancelled
  }
  const title = value.trim().slice(0, 128)
  try {
    await updateConversation(conv.id, { title })
    ElMessage.success('已重命名')
    await loadConversations()
  } catch {
    // handled by api layer
  }
}

async function removeConversation(conv) {
  try {
    await ElMessageBox.confirm(
      `确定删除对话「${conv.title || '新对话'}」吗？删除后将不再显示。`,
      '删除对话',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' },
    )
  } catch {
    return // user cancelled
  }
  try {
    await deleteConversation(conv.id)
    ElMessage.success('已删除')
    if (conv.id === activeConvId.value) newConversation()
    await loadConversations()
  } catch {
    // handled by api layer
  }
}

// ── Send / stream ───────────────────────────────────────────────────

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  const userMsg = createMessage({
    id: nextId(),
    role: 'user',
    content: text,
    time: formatTime(new Date()),
  })
  messages.value.push(userMsg)
  inputText.value = ''

  await runStream({ message: text })
}

async function confirmAction(msg) {
  if (loading.value) return
  msg.pending.status = 'confirmed'  // optimistic; server is source of truth
  await runStream({ confirmedActionId: msg.pending.action_id })
}

async function cancelAction(msg) {
  if (loading.value || activeConvId.value == null) return

  pendingActionBusyId.value = msg.pending.action_id
  try {
    await cancelPendingAction(msg.pending.action_id, activeConvId.value)
    msg.pending.status = 'cancelled'
  } catch {
    // request helper already surfaced the error
  } finally {
    if (pendingActionBusyId.value === msg.pending.action_id) {
      pendingActionBusyId.value = null
    }
  }
}

async function runStream({ message = null, confirmedActionId = null }) {
  loading.value = true
  abortController = new AbortController()
  setActivity('thinking', confirmedActionId != null ? '正在继续执行已确认的操作…' : '正在思考你的问题…')

  // Placeholder assistant bubble — streams tokens directly into .content.
  const assistantMsg = createMessage({
    id: nextId(),
    role: 'assistant',
    content: '',
    streaming: true,
    time: '',
  })
  messages.value.push(assistantMsg)
  await scrollToBottom()

  try {
    await chatStream({
      conversationId: activeConvId.value,
      message,
      confirmedActionId,
      signal: abortController.signal,
      onEvent: ev => handleEvent(ev, assistantMsg),
    })
  } catch {
    flushTypewriter(assistantMsg)
    if (!assistantMsg.content) {
      const idx = messages.value.indexOf(assistantMsg)
      if (idx !== -1) messages.value.splice(idx, 1)
    }
  } finally {
    // Mark typewriter buffer as "no more incoming" so its tick drains and stops.
    // The cursor + streaming flag stay on until the buffer empties.
    finishTypewriter(assistantMsg)
    waitForTypewriterDrain(assistantMsg).then(() => {
      assistantMsg.streaming = false
      if (assistantMsg.content && !assistantMsg.time) {
        assistantMsg.time = formatTime(new Date())
      }
      if (activityState.value === 'thinking' || activityState.value === 'tool' || activityState.value === 'streaming') {
        clearActivity()
      }
      scrollToBottom()
    })
    loading.value = false
    abortController = null
  }
}

function waitForTypewriterDrain(msg) {
  return new Promise(resolve => {
    const check = () => {
      const state = typewriterState.get(msg.id)
      if (!state || (state.pending.length === 0 && state.done)) resolve()
      else setTimeout(check, TYPE_INTERVAL_MS)
    }
    check()
  })
}

function handleEvent(ev, assistantMsg) {
  switch (ev.type) {
    case 'token':
      setActivity('streaming', '正在生成回复…')
      pushTokens(assistantMsg, ev.text || '')
      break

    case 'tool_call':
      setActivity('tool', toolCallHint(ev.name))
      break

    case 'tool_result':
      if (ev.ok === false && ev.error) {
        setActivity('error', `工具调用失败：${ev.error}`)
      } else {
        setActivity('thinking', '工具调用完成，正在整理答案…')
      }
      break

    case 'confirm_required': {
      if (ev.conversation_id) {
        rememberConversation(ev.conversation_id)
        loadConversations()
      }
      // Drop the empty placeholder — pending card replaces it for this turn.
      flushTypewriter(assistantMsg)
      if (!assistantMsg.content) {
        const idx = messages.value.indexOf(assistantMsg)
        if (idx !== -1) messages.value.splice(idx, 1)
      } else {
        assistantMsg.streaming = false
        assistantMsg.time = formatTime(new Date())
      }
      setActivity('confirm', '已生成待确认操作，请确认后继续')
      const pendingMsg = createMessage({
        id: nextId(),
        role: 'pending',
        pending: {
          action_id: ev.action_id,
          tool_name: ev.tool_name,
          summary: ev.summary || '',
          params: ev.params || {},
          status: 'pending',
        },
      })
      messages.value.push(pendingMsg)
      hydratePendingCards([pendingMsg])
      scrollToBottom()
      break
    }

    case 'final':
      if (ev.conversation_id) {
        rememberConversation(ev.conversation_id)
        loadConversations()
      }
      break

    case 'error':
      setActivity('error', ev.message || 'AI 内部错误')
      ElMessage.error(ev.message || 'AI 内部错误')
      break
  }
}

function toolCallHint(name) {
  const map = {
    search_rooms: '正在查询自习室…',
    get_room_detail: '正在查询自习室详情…',
    get_seat_availability: '正在查询座位可用情况…',
    list_my_reservations: '正在查询你的预约…',
    list_notices: '正在查询公告…',
    list_my_violations: '正在查询违约记录…',
    resolve_date: '正在解析日期…',
    create_reservation: '准备创建预约…',
    cancel_reservation: '准备取消预约…',
  }
  return map[name] || `调用工具 ${name}…`
}

function abortStream() {
  if (abortController) {
    abortController.abort()
    abortController = null
    setActivity('error', '已停止生成')
  }
  // Drain any in-flight typewriter buffers so the partial text is shown.
  for (const msg of messages.value) {
    if (msg.role === 'assistant' && typewriterState.has(msg.id)) {
      flushTypewriter(msg)
      msg.streaming = false
    }
  }
}

// ── Lifecycle ───────────────────────────────────────────────────────

watch(
  () => route.query.conv,
  async convQuery => {
    const convId = normalizeConvId(convQuery)
    if (convId == null) {
      if (activeConvId.value != null && !loading.value) newConversation()
      return
    }
    if (convId !== activeConvId.value) {
      await switchConversation(convId)
    }
  }
)

onMounted(async () => {
  await loadConversations()
  const convId = normalizeConvId(route.query.conv)
  if (convId != null) {
    await switchConversation(convId)
    return
  }
  const q = route.query.q
  if (q && typeof q === 'string') {
    inputText.value = q
    await sendMessage()
  }
})

onBeforeUnmount(() => {
  abortStream()
})
</script>

<style scoped>
.ai-chat-page {
  --surface: #ffffff;
  --canvas:  #fafbfc;
  --line:    #ecedf1;
  --text-1:  #1f2329;
  --text-2:  #5b6170;
  --text-3:  #9298a5;
  --hover:   #f2f3f6;
  --user-bubble: #EDF3FE;   /* 高级灰：用户气泡 */
  --r-sm: 8px;
  --r-md: 12px;
  --r-lg: 16px;

  display: flex;
  flex-direction: row;
  height: calc(100vh - 72px - 48px);
  min-height: 500px;
  background: var(--surface);
  border-radius: var(--r-lg);
  border: 1px solid var(--line);
  overflow: hidden;
}

/* ── Sidebar ── */
.chat-sidebar {
  width: 264px;
  flex-shrink: 0;
  border-right: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  background: var(--canvas);
  overflow: hidden;
}

.sidebar-header {
  padding: 14px 12px;
  flex-shrink: 0;
}

.new-conv-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 9px 0;
  background: var(--surface);
  color: var(--text-1);
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.new-conv-btn :deep(.el-icon) {
  color: var(--nt-primary);
}

.new-conv-btn:hover {
  background: var(--hover);
  border-color: #e0e1e7;
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 4px 8px 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conv-empty {
  font-size: 12px;
  color: var(--text-3);
  text-align: center;
  padding: 24px 8px;
}

.conv-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 9px 8px 9px 11px;
  border-radius: var(--r-sm);
  cursor: pointer;
  transition: background 0.15s;
}

.conv-item:hover {
  background: var(--hover);
}

.conv-active {
  background: var(--hover);
}

.conv-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 16px;
  border-radius: 0 2px 2px 0;
  background: var(--nt-primary);
}

.conv-item-main {
  flex: 1;
  min-width: 0;
}

.conv-item-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-1);
}

.conv-item-title-text {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-pin-icon {
  flex-shrink: 0;
  color: var(--nt-primary);
}

.conv-active .conv-item-title {
  font-weight: 600;
}

.conv-item-time {
  font-size: 12px;
  color: var(--text-3);
  margin-top: 3px;
}

.conv-item-more {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  color: var(--text-3);
  opacity: 0;
  transition: opacity 0.15s, background 0.15s, color 0.15s;
}

.conv-item:hover .conv-item-more,
.conv-active .conv-item-more {
  opacity: 1;
}

.conv-item-more:hover {
  background: rgba(0, 0, 0, 0.05);
  color: var(--text-1);
}

/* ── Main Chat Column ── */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}

/* ── Header ── */
.chat-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 24px;
  border-bottom: 1px solid var(--line);
  background: var(--surface);
  flex-shrink: 0;
}

.chat-back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-2);
  background: none;
  border: 1px solid var(--line);
  border-radius: var(--r-sm);
  padding: 6px 12px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  flex-shrink: 0;
}

.chat-back-btn:hover {
  background: var(--hover);
  color: var(--text-1);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.chat-ai-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: var(--surface);
  border: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.chat-header-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-1);
  display: flex;
  align-items: center;
}

.agent-online {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 500;
  color: #1a9d5a;
  background: #f1faf4;
  border-radius: 10px;
  padding: 1px 8px;
  margin-left: 8px;
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
}

.chat-header-sub {
  font-size: 13px;
  color: var(--text-3);
  margin-top: 3px;
}

.chat-activity-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 36px;
  padding: 8px 24px;
  border-bottom: 1px solid var(--line);
  background: var(--canvas);
}

.activity-spinner {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid var(--line);
  border-top-color: var(--nt-primary);
  animation: activity-spin 0.8s linear infinite;
  flex-shrink: 0;
}

.activity-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  background: var(--text-3);
}

.activity-text {
  font-size: 12px;
  color: var(--text-2);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-activity-error .activity-dot {
  background: #ef4444;
}

.chat-activity-confirm .activity-dot {
  background: #f59e0b;
}

@keyframes activity-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ── Messages ── */
.chat-messages {
  --chat-col: 888px;   /* centered reading column; extra space becomes side whitespace */
  flex: 1;
  overflow-y: auto;
  padding: 24px max(24px, calc((100% - var(--chat-col)) / 2));
  display: flex;
  flex-direction: column;
  background: var(--canvas);
}

/* ── Custom scrollbar — slim, light, semi-transparent ── */
.chat-messages,
.conv-list {
  scrollbar-width: thin;                                  /* Firefox */
  scrollbar-color: rgba(100, 110, 130, 0.28) transparent;
}

.chat-messages::-webkit-scrollbar,
.conv-list::-webkit-scrollbar {
  width: 10px;
}

.chat-messages::-webkit-scrollbar-track,
.conv-list::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb,
.conv-list::-webkit-scrollbar-thumb {
  background: rgba(100, 110, 130, 0.25);
  border-radius: 999px;
  border: 3px solid transparent;   /* transparent border + clip = slim inset pill */
  background-clip: padding-box;
}

.chat-messages::-webkit-scrollbar-thumb:hover,
.conv-list::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 110, 130, 0.42);
  background-clip: padding-box;
}

/* Turn-based rhythm: the answer hugs its question, bigger gap between turns. */
.chat-msg {
  display: flex;
  margin-top: 22px;
}

.chat-msg:first-child {
  margin-top: 0;
}

.chat-msg-user + .chat-msg-ai {
  margin-top: 8px;
}

.chat-msg-ai + .chat-msg-ai {
  margin-top: 12px;
}

.chat-msg-user {
  justify-content: flex-end;
}

.chat-bubble {
  max-width: min(680px, 72%);
  border-radius: var(--r-md);
  padding: 10px 14px;
}

.bubble-user {
  background: var(--user-bubble);
  color: var(--text-1);
}

/* AI answer: plain text on the canvas — no bubble, no avatar. */
.ai-answer {
  max-width: min(720px, 100%);
}

.bubble-text {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-1);
  white-space: pre-wrap;
  word-break: break-word;
}

/* Markdown bubble: marked produces real block elements, so disable pre-wrap
   (which would otherwise duplicate spacing) and style each tag. */
.bubble-md {
  white-space: normal;
}

.md-body {
  display: inline;
}

.md-body :deep(p) {
  margin: 0 0 8px;
}
.md-body :deep(p:last-child) {
  margin-bottom: 0;
}
.md-body :deep(ul),
.md-body :deep(ol) {
  margin: 6px 0 8px;
  padding-left: 22px;
}
.md-body :deep(li) {
  margin: 2px 0;
}
.md-body :deep(h1),
.md-body :deep(h2),
.md-body :deep(h3),
.md-body :deep(h4) {
  margin: 10px 0 6px;
  font-weight: 600;
  color: var(--text-1);
}
.md-body :deep(h1) { font-size: 18px; }
.md-body :deep(h2) { font-size: 17px; }
.md-body :deep(h3) { font-size: 16px; }
.md-body :deep(h4) { font-size: 15px; }
.md-body :deep(code) {
  background: var(--hover);
  border: 1px solid var(--line);
  border-radius: 4px;
  padding: 1px 5px;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  color: #c2410c;
}
.md-body :deep(pre) {
  background: #1a202c;
  color: #e2e8f0;
  border-radius: 8px;
  padding: 10px 12px;
  margin: 8px 0;
  overflow-x: auto;
  font-size: 13px;
  line-height: 1.5;
}
.md-body :deep(pre code) {
  background: transparent;
  border: none;
  padding: 0;
  color: inherit;
  font-size: inherit;
}
.md-body :deep(a) {
  color: var(--nt-primary);
  text-decoration: underline;
}
.md-body :deep(blockquote) {
  border-left: 3px solid var(--line);
  padding: 2px 0 2px 10px;
  margin: 6px 0;
  color: var(--text-2);
  background: var(--hover);
  border-radius: 0 6px 6px 0;
}
.md-body :deep(table) {
  border-collapse: collapse;
  margin: 8px 0;
  font-size: 13px;
}
.md-body :deep(th),
.md-body :deep(td) {
  border: 1px solid var(--line);
  padding: 4px 10px;
}
.md-body :deep(th) {
  background: var(--hover);
  font-weight: 600;
}
.md-body :deep(strong) { font-weight: 600; }
.md-body :deep(em) { font-style: italic; }
.md-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--line);
  margin: 10px 0;
}

.cursor-blink {
  display: inline-block;
  margin-left: 1px;
  color: var(--nt-primary);
  animation: cursor-fade 1s steps(1) infinite;
}

@keyframes cursor-fade {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.bubble-time {
  font-size: 11px;
  margin-top: 5px;
  text-align: right;
  color: var(--text-3);
}

.ai-answer .bubble-time {
  text-align: left;
}

/* Typing dots */
.ai-typing {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 0;
}

.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--text-3);
  animation: typing-bounce 1.2s infinite ease-in-out;
}

.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing-bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.5; }
  40% { transform: translateY(-6px); opacity: 1; }
}

/* ── Pending action card ── */
.pending-card {
  max-width: min(680px, 72%);
  background: #fffdf5;
  border: 1px solid #f1e4bf;
  border-radius: var(--r-md);
  padding: 12px 15px;
}

.pending-confirmed {
  background: #f5fbf7;
  border-color: #d6ecdc;
}

.pending-cancelled {
  background: var(--canvas);
  border-color: var(--line);
}

.pending-failed {
  background: #fdf6f6;
  border-color: #f0d6d6;
}

.pending-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #b07a16;
  margin-bottom: 8px;
}

.pending-confirmed .pending-header { color: #2f8a52; }
.pending-failed .pending-header { color: #cc4b4b; }
.pending-cancelled .pending-header { color: var(--text-3); }

.pending-primary {
  margin-bottom: 12px;
}

.pending-primary-eyebrow {
  font-size: 12px;
  font-weight: 600;
  color: #b07a16;
  margin-bottom: 2px;
}

.pending-primary-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-1);
  line-height: 1.4;
}

.pending-primary-meta {
  font-size: 12px;
  color: var(--text-2);
  margin-top: 4px;
}

.pending-confirmed .pending-primary-eyebrow { color: #2f8a52; }
.pending-failed .pending-primary-eyebrow { color: #cc4b4b; }
.pending-cancelled .pending-primary-eyebrow { color: var(--text-3); }

.pending-detail-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.pending-detail-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  background: rgba(255,255,255,0.6);
  border-radius: var(--r-sm);
}

.pending-detail-label {
  font-size: 12px;
  color: var(--text-2);
  flex-shrink: 0;
}

.pending-detail-value {
  font-size: 12px;
  color: var(--text-1);
  font-weight: 500;
  text-align: right;
  word-break: break-word;
}

.pending-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pending-status-tag {
  font-size: 12px;
  font-weight: 500;
  padding: 4px 0 0;
  text-align: right;
}

.pending-status-ok { color: #2f8a52; }
.pending-status-failed { color: #cc4b4b; }
.pending-status-cancel { color: var(--text-3); }

/* ── Input Area ── */
.chat-input-area {
  padding: 16px 24px 20px;
  border-top: 1px solid var(--line);
  background: var(--surface);
  flex-shrink: 0;
}

.chat-input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}

.chat-input {
  flex: 1;
}

.chat-input :deep(.el-input__wrapper) {
  border-radius: var(--r-md);
  box-shadow: 0 0 0 1px var(--line);
  background: var(--canvas);
  transition: box-shadow 0.15s, background 0.15s;
}

.chat-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d8dae2;
}

.chat-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--nt-primary);
  background: var(--surface);
}

.chat-send-btn {
  border-radius: var(--r-md) !important;
  padding: 0 24px !important;
  font-weight: 600 !important;
  flex-shrink: 0;
}

.chat-input-tip {
  font-size: 11px;
  color: var(--text-3);
  margin-top: 8px;
  text-align: center;
}
</style>
