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
          v-for="conv in conversations"
          :key="conv.id"
          :class="['conv-item', { 'conv-active': conv.id === activeConvId }]"
          @click="switchConversation(conv.id)"
        >
          <div class="conv-item-title">{{ conv.title }}</div>
          <div class="conv-item-time">{{ conv.time }}</div>
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
        <button class="chat-clear-btn" @click="clearMessages">清空对话</button>
      </div>

      <!-- Messages -->
      <div class="chat-messages" ref="messagesEl">
        <div
          v-for="msg in messages"
          :key="msg.id"
          :class="['chat-msg', msg.role === 'user' ? 'chat-msg-user' : 'chat-msg-ai']"
        >
          <div v-if="msg.role === 'ai'" class="chat-bubble-avatar ai-avatar">
            <MagicStick style="width:14px;height:14px;color:#fff" />
          </div>

          <div :class="['chat-bubble', msg.role === 'user' ? 'bubble-user' : 'bubble-ai']">
            <div class="bubble-text">{{ msg.content }}</div>
            <div class="bubble-time">{{ msg.time }}</div>
          </div>

          <div v-if="msg.role === 'user'" class="chat-bubble-avatar user-avatar">
            <img :src="auth.user?.avatarUrl || defaultAvatar" class="bubble-avatar-img" />
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="loading" class="chat-msg chat-msg-ai">
          <div class="chat-bubble-avatar ai-avatar">
            <img src="../assets/deepseek-color.svg" style="width:18px;height:18px;" />
          </div>
          <div class="chat-bubble bubble-ai bubble-typing">
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
            type="primary"
            size="large"
            :disabled="!inputText.trim() || loading"
            :loading="loading"
            class="chat-send-btn"
            @click="sendMessage"
          >
            发送
          </el-button>
        </div>
        <div class="chat-input-tip">Enter 发送 · 本智能体仅供学习参考</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useStudentAuthStore } from '../stores/auth'
import { ArrowLeft, Plus } from '@element-plus/icons-vue'
import defaultAvatar from '../assets/stu-default-icon.png'

const router = useRouter()
const route = useRoute()
const auth = useStudentAuthStore()

const messagesEl = ref(null)
const inputText = ref('')
const loading = ref(false)
const activeConvId = ref(0)

const conversations = ref([
  { id: 0, title: '当前对话', time: '刚刚' },
  { id: 1, title: '如何预约图书馆座位', time: '今天 14:30' },
  { id: 2, title: '信用分扣除原因查询', time: '今天 10:15' },
  { id: 3, title: '预约取消规则说明', time: '昨天 16:20' },
  { id: 4, title: '违规申诉流程', time: '5月13日' },
])

function makeWelcomeMsg() {
  return {
    id: 0,
    role: 'ai',
    content: '你好！我是 Nookit 校园智能体，不只是答题助手——我能帮你完成预约操作、查询座位状态、分析信用情况、指导违规申诉，以及解答各类校园自习室问题。\n\n有什么想完成的任务，直接告诉我吧！',
    time: formatTime(new Date()),
  }
}

const messages = ref([makeWelcomeMsg()])

const mockReplies = [
  '每位同学每天最多可预约 2 个时段的自习室座位，单次预约时长不超过 4 小时。',
  '您可以在"我的预约"页面查看当前预约状态，包括待确认、已确认和已完成的记录。',
  '如需取消预约，请在预约开始时间 30 分钟前操作，逾期取消将扣除 5 分信用积分。',
  '自习室一般在工作日 8:00–22:00 开放，节假日开放时间请关注通知公告。',
  '信用积分低于 60 分时将暂时无法发起预约。如认为扣分有误，可在"我的违规"页面发起申诉，审核周期为 3 个工作日。',
  '预约成功后请准时入座。超过 15 分钟未入座，系统将自动取消本次预约并扣除信用积分 10 分。',
  '您好，这个问题我暂时还在学习中。您可以前往"问题反馈"页面提交详细问题，工作人员会尽快为您解答。',
]

let replyIdx = 0

function formatTime(d) {
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  const conv = conversations.value.find(c => c.id === activeConvId.value)
  if (conv && (conv.title === '当前对话' || conv.title === '新对话')) {
    conv.title = text.length > 14 ? text.slice(0, 14) + '…' : text
  }

  messages.value.push({
    id: Date.now(),
    role: 'user',
    content: text,
    time: formatTime(new Date()),
  })
  inputText.value = ''
  loading.value = true
  await scrollToBottom()

  await new Promise(r => setTimeout(r, 900 + Math.random() * 700))

  messages.value.push({
    id: Date.now() + 1,
    role: 'ai',
    content: mockReplies[replyIdx % mockReplies.length],
    time: formatTime(new Date()),
  })
  replyIdx++
  loading.value = false
  await scrollToBottom()
}

function clearMessages() {
  messages.value = [makeWelcomeMsg()]
  replyIdx = 0
}

function switchConversation(id) {
  if (id === activeConvId.value) return
  activeConvId.value = id
  const conv = conversations.value.find(c => c.id === id)
  if (id === 0) {
    messages.value = [makeWelcomeMsg()]
  } else {
    messages.value = [
      { ...makeWelcomeMsg(), id: -1 },
      { id: 1, role: 'user', content: conv?.title || '问题', time: conv?.time || '' },
      { id: 2, role: 'ai', content: mockReplies[id % mockReplies.length], time: conv?.time || '' },
    ]
  }
  scrollToBottom()
}

function newConversation() {
  const newId = Date.now()
  conversations.value.unshift({ id: newId, title: '新对话', time: '刚刚' })
  activeConvId.value = newId
  messages.value = [makeWelcomeMsg()]
}

onMounted(async () => {
  const q = route.query.q
  if (q) {
    inputText.value = q
    await sendMessage()
  }
})
</script>

<style scoped>
.ai-chat-page {
  display: flex;
  flex-direction: row;
  height: calc(100vh - 72px - 48px);
  min-height: 500px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #f0f2f8;
  box-shadow: 0 2px 12px rgba(0,0,0,0.04);
  overflow: hidden;
}

/* ── Sidebar ── */
.chat-sidebar {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid #f0f2f8;
  display: flex;
  flex-direction: column;
  background: #f9fafc;
  overflow: hidden;
}

.sidebar-header {
  padding: 14px 12px;
  border-bottom: 1px solid #f0f2f8;
  flex-shrink: 0;
}

.new-conv-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 0;
  background: var(--nt-primary-light);
  color: var(--nt-primary);
  border: 1.5px dashed var(--nt-primary-border);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.18s;
}

.new-conv-btn:hover {
  background: var(--nt-primary-light);
  border-color: var(--nt-primary);
  border-style: solid;
}

.conv-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conv-item {
  padding: 10px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.conv-item:hover {
  background: #f0f4fd;
}

.conv-active {
  background: var(--nt-primary-light) !important;
}

.conv-item-title {
  font-size: 13px;
  font-weight: 500;
  color: #2d3748;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.conv-active .conv-item-title {
  color: var(--nt-primary);
  font-weight: 600;
}

.conv-item-time {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 2px;
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
  border-bottom: 1px solid #f0f2f8;
  background: #fff;
  flex-shrink: 0;
}

.chat-back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #4a5568;
  background: none;
  border: 1px solid #e8eaf2;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  flex-shrink: 0;
}

.chat-back-btn:hover {
  background: var(--nt-primary-light);
  color: var(--nt-primary-dark);
}

.chat-header-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.chat-ai-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #fff;
  border: 1.5px solid #e8eaf2;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.chat-header-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  display: flex;
  align-items: center;
}

.agent-online {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 500;
  color: #16a34a;
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 10px;
  padding: 1px 8px;
  margin-left: 8px;
}

.online-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
  animation: pulse-green 2s infinite;
}

@keyframes pulse-green {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.chat-header-sub {
  font-size: 12px;
  color: #8492a6;
  margin-top: 2px;
}

.chat-clear-btn {
  font-size: 13px;
  color: #8492a6;
  background: none;
  border: 1px solid #e8eaf2;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  flex-shrink: 0;
}

.chat-clear-btn:hover {
  background: #fff5f5;
  color: #e53e3e;
  border-color: #fed7d7;
}

/* ── Messages ── */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: #f9fafc;
}

.chat-msg {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.chat-msg-user {
  flex-direction: row-reverse;
}

.chat-bubble-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 700;
}

.ai-avatar {
  background: #fff;
  border: 1.5px solid #e8eaf2;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}

.user-avatar {
  background: #e8eaf2;
  overflow: hidden;
}

.bubble-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.chat-bubble {
  max-width: 65%;
  border-radius: 12px;
  padding: 12px 16px;
  position: relative;
}

.bubble-ai {
  background: #fff;
  border: 1px solid #e8eaf2;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}

.bubble-user {
  background: linear-gradient(135deg, var(--nt-primary), var(--nt-primary-dark));
  color: #fff;
  border-bottom-right-radius: 4px;
}

.bubble-text {
  font-size: 14px;
  line-height: 1.65;
  color: inherit;
  white-space: pre-wrap;
  word-break: break-word;
}

.bubble-ai .bubble-text {
  color: #2d3748;
}

.bubble-time {
  font-size: 11px;
  margin-top: 6px;
  text-align: right;
}

.bubble-ai .bubble-time {
  color: #a0aec0;
}

.bubble-user .bubble-time {
  color: rgba(255,255,255,0.65);
}

/* Typing dots */
.bubble-typing {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 14px 18px;
  min-width: 60px;
}

.typing-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #a0aec0;
  animation: typing-bounce 1.2s infinite ease-in-out;
}

.typing-dot:nth-child(2) { animation-delay: 0.2s; }
.typing-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing-bounce {
  0%, 80%, 100% { transform: translateY(0); opacity: 0.5; }
  40% { transform: translateY(-6px); opacity: 1; }
}

/* ── Input Area ── */
.chat-input-area {
  padding: 16px 24px 20px;
  border-top: 1px solid #f0f2f8;
  background: #fff;
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
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e8eaf2;
}

.chat-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--nt-primary);
}

.chat-send-btn {
  border-radius: 10px !important;
  padding: 0 24px !important;
  font-weight: 600 !important;
  flex-shrink: 0;
}

.chat-input-tip {
  font-size: 11px;
  color: #a0aec0;
  margin-top: 8px;
  text-align: center;
}
</style>
