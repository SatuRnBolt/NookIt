<template>
  <div class="feedback-page">
    <div class="page-header">
      <div class="page-title">
        <el-icon><ChatDotRound /></el-icon> 问题反馈
      </div>
      <div class="header-stats">
        <div class="stat-pill pending">
          <span class="pill-dot"></span>
          待处理 <strong>{{ pendingCount }}</strong>
        </div>
        <div class="stat-pill processing">
          <span class="pill-dot"></span>
          处理中 <strong>{{ processingCount }}</strong>
        </div>
        <div class="stat-pill resolved">
          <span class="pill-dot"></span>
          已解决 <strong>{{ resolvedCount }}</strong>
        </div>
      </div>
    </div>

    <div class="split-layout">
      <!-- 左侧列表 -->
      <div class="list-panel">
        <div class="list-toolbar">
          <el-input v-model="search" placeholder="搜索学生姓名/学号/标题" :prefix-icon="Search" clearable />
          <div class="filter-row">
            <el-select v-model="filterType" placeholder="全部类型" clearable>
              <el-option label="故障报告" value="bug" />
              <el-option label="功能建议" value="suggestion" />
              <el-option label="投诉" value="complaint" />
            </el-select>
            <el-select v-model="filterStatus" placeholder="全部状态" clearable>
              <el-option label="待处理" value="pending" />
              <el-option label="处理中" value="processing" />
              <el-option label="已解决" value="resolved" />
            </el-select>
          </div>
        </div>

        <div class="list-scroll">
          <div v-if="!filteredFeedbacks.length" class="empty-list">
            <el-empty description="暂无符合条件的反馈" :image-size="70" />
          </div>
          <div
            v-for="f in filteredFeedbacks"
            :key="f.id"
            class="fb-item"
            :class="{ active: selectedId === f.id, unread: f.status === 'pending' }"
            @click="selectFeedback(f)"
          >
            <div class="fb-avatar" :class="`avatar-${f.type}`">{{ f.studentName.slice(0, 1) }}</div>
            <div class="fb-body">
              <div class="fb-row">
                <span class="fb-name">{{ f.studentName }}</span>
                <span class="fb-time">{{ shortTime(f.createdAt) }}</span>
              </div>
              <div class="fb-title">{{ f.title }}</div>
              <div class="fb-meta">
                <el-tag size="small" :type="typeTagType(f.type)" effect="plain">{{ typeLabel(f.type) }}</el-tag>
                <span class="status-pill" :class="`status-${f.status}`">
                  <span class="pill-dot"></span>{{ statusLabel(f.status) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧对话流 -->
      <div class="detail-panel">
        <div v-if="!selected" class="detail-empty">
          <el-icon size="56" color="#d1d5db"><ChatDotRound /></el-icon>
          <div class="empty-text">从左侧选择反馈查看详情</div>
          <div class="empty-hint">点击列表项可查看完整内容并进行回复</div>
        </div>

        <template v-else>
          <div class="thread-header">
            <div class="thread-title-row">
              <h2 class="thread-title">{{ selected.title }}</h2>
              <span class="status-pill" :class="`status-${selected.status}`">
                <span class="pill-dot"></span>{{ statusLabel(selected.status) }}
              </span>
            </div>
            <div class="thread-meta">
              <el-tag :type="typeTagType(selected.type)" size="small">{{ typeLabel(selected.type) }}</el-tag>
              <span class="meta-sep">·</span>
              <span>{{ selected.studentName }}（{{ selected.studentId }}）</span>
              <span class="meta-sep">·</span>
              <span>提交于 {{ selected.createdAt }}</span>
              <div style="flex:1" />
              <el-button
                v-if="selected.status === 'pending'"
                size="small"
                @click="setStatus(selected, 'processing')"
              >
                <el-icon><Loading /></el-icon><span>标记为处理中</span>
              </el-button>
            </div>
          </div>

          <div class="thread-body">
            <!-- 用户消息（左侧气泡） -->
            <div class="msg msg-left">
              <div class="msg-avatar" :class="`avatar-${selected.type}`">
                {{ selected.studentName.slice(0, 1) }}
              </div>
              <div class="msg-content">
                <div class="msg-author">
                  {{ selected.studentName }}
                  <span class="role-badge">学生</span>
                  <span class="msg-time">{{ selected.createdAt }}</span>
                </div>
                <div class="msg-bubble user-bubble">{{ selected.content }}</div>
              </div>
            </div>

            <!-- 官方回复（右侧气泡） -->
            <div v-if="selected.reply" class="msg msg-right">
              <div class="msg-content">
                <div class="msg-author right">
                  <span class="msg-time">{{ selected.repliedAt }}</span>
                  <span class="role-badge admin">管理员</span>
                  {{ selected.repliedBy }}
                </div>
                <div class="msg-bubble admin-bubble">{{ selected.reply }}</div>
              </div>
              <div class="msg-avatar admin-avatar">管</div>
            </div>

            <div v-else-if="selected.status !== 'resolved'" class="msg-placeholder">
              <el-icon><ChatLineRound /></el-icon>
              <span>暂无回复，请在下方输入回复内容</span>
            </div>
          </div>

          <!-- 回复输入区 / 已解决横幅 -->
          <div v-if="selected.status !== 'resolved'" class="reply-area">
            <el-input
              v-model="replyText"
              type="textarea"
              :rows="3"
              placeholder="输入回复内容…按 Ctrl+Enter 快速提交"
              maxlength="500"
              show-word-limit
              resize="none"
              @keydown.ctrl.enter="submitReply"
            />
            <div class="reply-bar">
              <span class="reply-hint">
                <el-icon><InfoFilled /></el-icon>
                提交回复后，反馈将自动标记为「已解决」
              </span>
              <el-button type="primary" :disabled="!replyText.trim()" @click="submitReply">
                <el-icon><Promotion /></el-icon><span>提交回复</span>
              </el-button>
            </div>
          </div>
          <div v-else class="resolved-banner">
            <el-icon><CircleCheckFilled /></el-icon>
            <span>该反馈已解决</span>
            <span class="banner-sub">由 {{ selected.repliedBy }} 于 {{ selected.repliedAt }} 处理</span>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ChatDotRound, ChatLineRound, Search, Promotion,
  Loading, InfoFilled, CircleCheckFilled,
} from '@element-plus/icons-vue'
import { mockFeedback } from '../mock/data'

const feedbacks = ref([...mockFeedback])
const search = ref('')
const filterType = ref('')
const filterStatus = ref('')
const selectedId = ref(null)
const replyText = ref('')

const filteredFeedbacks = computed(() => {
  const list = feedbacks.value.filter(f => {
    if (search.value) {
      const q = search.value
      if (!f.studentName.includes(q) && !f.studentId.includes(q) && !f.title.includes(q)) return false
    }
    if (filterType.value && f.type !== filterType.value) return false
    if (filterStatus.value && f.status !== filterStatus.value) return false
    return true
  })
  return list.sort((a, b) => {
    const order = { pending: 0, processing: 1, resolved: 2 }
    if (order[a.status] !== order[b.status]) return order[a.status] - order[b.status]
    return b.createdAt.localeCompare(a.createdAt)
  })
})

const selected = computed(() => feedbacks.value.find(f => f.id === selectedId.value))
const pendingCount = computed(() => feedbacks.value.filter(f => f.status === 'pending').length)
const processingCount = computed(() => feedbacks.value.filter(f => f.status === 'processing').length)
const resolvedCount = computed(() => feedbacks.value.filter(f => f.status === 'resolved').length)

function typeLabel(t) {
  return { bug: '故障报告', suggestion: '功能建议', complaint: '投诉' }[t] || t
}

function typeTagType(t) {
  return { bug: 'danger', suggestion: 'primary', complaint: 'warning' }[t] || ''
}

function statusLabel(s) {
  return { pending: '待处理', processing: '处理中', resolved: '已解决' }[s] || s
}

function shortTime(s) {
  if (!s) return ''
  const parts = s.split(' ')
  return parts[0]?.slice(5) || s
}

function selectFeedback(f) {
  selectedId.value = f.id
  replyText.value = ''
}

function setStatus(f, status) {
  const idx = feedbacks.value.findIndex(x => x.id === f.id)
  if (idx !== -1) {
    feedbacks.value[idx].status = status
    ElMessage.success('状态已更新')
  }
}

function submitReply() {
  if (!replyText.value.trim()) {
    ElMessage.warning('请填写回复内容')
    return
  }
  const idx = feedbacks.value.findIndex(f => f.id === selectedId.value)
  if (idx !== -1) {
    const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    feedbacks.value[idx].reply = replyText.value
    feedbacks.value[idx].repliedAt = now
    feedbacks.value[idx].repliedBy = '张管理'
    feedbacks.value[idx].status = 'resolved'
  }
  ElMessage.success('回复已提交，反馈已标记为已解决')
  replyText.value = ''
}

if (filteredFeedbacks.value.length) selectedId.value = filteredFeedbacks.value[0].id
</script>

<style scoped>
.feedback-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 110px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.header-stats {
  display: flex;
  gap: 8px;
}

.stat-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: #fff;
  border-radius: 16px;
  font-size: 13px;
  color: #6b7280;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.stat-pill strong {
  color: #1f2937;
  font-weight: 700;
}

.pill-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #9ca3af;
}

.stat-pill.pending .pill-dot { background: #ef4444; }
.stat-pill.processing .pill-dot { background: #f59e0b; }
.stat-pill.resolved .pill-dot { background: #10b981; }

.split-layout {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

/* 左侧列表 */
.list-panel {
  width: 360px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-toolbar {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-bottom: 1px solid #f1f5f9;
}

.filter-row {
  display: flex;
  gap: 8px;
}

.filter-row :deep(.el-select) {
  flex: 1;
}

.list-scroll {
  flex: 1;
  overflow-y: auto;
}

.fb-item {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}

.fb-item:hover {
  background: #f9fafb;
}

.fb-item.active {
  background: #eff6ff;
}

.fb-item.active::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: #003893;
}

.fb-item.unread .fb-name::after {
  content: '';
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ef4444;
  margin-left: 6px;
  vertical-align: middle;
}

.fb-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
  background: #6b7280;
}

.avatar-bug { background: #ef4444; }
.avatar-suggestion { background: #003893; }
.avatar-complaint { background: #f59e0b; }

.fb-body {
  flex: 1;
  min-width: 0;
}

.fb-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.fb-name {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.fb-time {
  font-size: 11px;
  color: #9ca3af;
}

.fb-title {
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}

.fb-meta {
  display: flex;
  align-items: center;
  gap: 6px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
}

.status-pill .pill-dot {
  width: 6px;
  height: 6px;
}

.status-pending {
  background: #fef2f2;
  color: #dc2626;
}
.status-pending .pill-dot { background: #ef4444; }

.status-processing {
  background: #fffbeb;
  color: #d97706;
}
.status-processing .pill-dot { background: #f59e0b; }

.status-resolved {
  background: #ecfdf5;
  color: #059669;
}
.status-resolved .pill-dot { background: #10b981; }

.empty-list {
  padding: 40px 0;
}

/* 右侧详情 */
.detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.empty-text {
  font-size: 15px;
  color: #6b7280;
  margin-top: 12px;
}

.empty-hint {
  font-size: 12px;
  color: #9ca3af;
}

.thread-header {
  padding: 18px 24px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.thread-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.thread-title {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
  flex: 1;
  line-height: 1.4;
}

.thread-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #6b7280;
}

.meta-sep {
  color: #d1d5db;
}

.thread-meta :deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 对话流 */
.thread-body {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f9fafb;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.msg {
  display: flex;
  gap: 10px;
  max-width: 80%;
}

.msg-left {
  align-self: flex-start;
}

.msg-right {
  align-self: flex-end;
  flex-direction: row;
}

.msg-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
}

.msg-avatar.admin-avatar {
  background: #003893;
}

.msg-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.msg-author {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #4b5563;
}

.msg-author.right {
  justify-content: flex-end;
}

.role-badge {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: #f3f4f6;
  color: #6b7280;
  font-weight: 500;
}

.role-badge.admin {
  background: #dbeafe;
  color: #003893;
}

.msg-time {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 400;
}

.msg-bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-bubble {
  background: #fff;
  color: #1f2937;
  border: 1px solid #e5e7eb;
  border-top-left-radius: 4px;
}

.admin-bubble {
  background: #003893;
  color: #fff;
  border-top-right-radius: 4px;
}

.msg-placeholder {
  align-self: center;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #9ca3af;
  padding: 12px 16px;
  background: #f3f4f6;
  border-radius: 16px;
}

/* 回复区 */
.reply-area {
  border-top: 1px solid #f1f5f9;
  padding: 14px 24px 18px;
  background: #fff;
  flex-shrink: 0;
}

.reply-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.reply-hint {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.reply-bar :deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.resolved-banner {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 24px;
  background: #ecfdf5;
  color: #059669;
  font-size: 14px;
  font-weight: 500;
  border-top: 1px solid #d1fae5;
  flex-shrink: 0;
}

.resolved-banner .banner-sub {
  font-size: 12px;
  color: #6b7280;
  font-weight: 400;
  margin-left: auto;
}
</style>
