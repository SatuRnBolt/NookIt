<template>
  <div class="feedback-page">
    <div class="page-header">
      <el-icon class="page-icon" :size="22"><ChatDotRound /></el-icon>
      <h2>问题反馈</h2>
      <el-button type="primary" @click="dialogVisible = true" style="margin-left: auto">
        <el-icon :size="14"><Plus /></el-icon>
        提交反馈
      </el-button>
    </div>

    <div class="content-card feedback-card" v-loading="loading">
      <el-table
        :data="feedbacks"
        stripe
        style="width: 100%"
        class="feedback-table"
        empty-text="暂无反馈记录"
        @row-click="openThread"
      >
        <el-table-column label="标题" min-width="240">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title }}</span>
              <span v-if="row.reply_content || row.replyContent" class="reply-dot"></span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="feedbackTypeTag(row)" size="small">
              {{ feedbackTypeText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="feedbackStatusTag(row)" size="small">
              {{ feedbackStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.created_at || row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button text type="primary" @click.stop="openThread(row)">查看对话</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loading && feedbacks.length > 0" class="feedback-tip">
        点击列表项可查看完整对话和管理员回复
      </div>
    </div>

    <el-dialog
      v-model="threadVisible"
      width="760px"
      destroy-on-close
      class="thread-dialog"
    >
      <template #header>
        <div v-if="selectedFeedback" class="thread-header">
          <div class="thread-title-row">
            <h3 class="thread-title">{{ selectedFeedback.title }}</h3>
            <el-tag :type="feedbackStatusTag(selectedFeedback)" size="small">
              {{ feedbackStatusText(selectedFeedback) }}
            </el-tag>
          </div>
          <div class="thread-meta">
            <el-tag :type="feedbackTypeTag(selectedFeedback)" size="small" effect="plain">
              {{ feedbackTypeText(selectedFeedback) }}
            </el-tag>
            <span>提交于 {{ formatDateTime(selectedFeedback.created_at || selectedFeedback.createdAt) }}</span>
          </div>
        </div>
      </template>

      <div v-if="selectedFeedback" class="thread-body">
        <div class="msg msg-user">
          <div class="msg-main">
            <div class="msg-top user-top">
              <span class="msg-name">我的反馈</span>
              <span class="msg-time">{{ formatDateTime(selectedFeedback.created_at || selectedFeedback.createdAt) }}</span>
            </div>
            <div class="msg-bubble user-bubble">{{ selectedFeedback.content || '暂无内容' }}</div>
          </div>
          <div class="msg-avatar user-avatar">
            <span>我</span>
          </div>
        </div>

        <div v-if="selectedFeedback.reply_content || selectedFeedback.replyContent" class="msg msg-admin">
          <div class="msg-avatar admin-avatar">
            <span>管</span>
          </div>
          <div class="msg-main">
            <div class="msg-top admin-top">
              <span class="msg-name">管理员回复</span>
              <span class="msg-time">{{ formatDateTime(selectedFeedback.replied_at || selectedFeedback.repliedAt) }}</span>
            </div>
            <div class="msg-bubble admin-bubble">
              {{ selectedFeedback.reply_content || selectedFeedback.replyContent }}
            </div>
          </div>
        </div>

        <div v-else class="reply-placeholder">
          <el-icon :size="18"><ChatLineRound /></el-icon>
          <span>管理员暂未回复，请耐心等待</span>
        </div>
      </div>

      <div class="thread-footer-note">
        当前弹窗只能查看历史反馈与回复，还不能继续发送消息。
        目前学生端接口只支持“提交新反馈”和“查看反馈列表”，还没有“在原会话里追问/补充”的接口。
      </div>

      <template #footer>
        <el-button @click="threadVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      title="提交反馈"
      width="520px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="反馈类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择" style="width: 100%">
            <el-option label="问题反馈" value="bug" />
            <el-option label="功能建议" value="suggestion" />
            <el-option label="投诉" value="complaint" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请概括您的问题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="详细描述" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="5"
            maxlength="500"
            show-word-limit
            placeholder="请详细描述您的问题或建议..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, ChatLineRound, Plus } from '@element-plus/icons-vue'
import { getMyFeedbacks, submitFeedback } from '../api/feedback'

const feedbacks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const threadVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const selectedFeedback = ref(null)

const form = reactive({
  type: 'bug',
  title: '',
  content: '',
})

const rules = {
  type: [{ required: true, message: '请选择反馈类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入详细描述', trigger: 'blur' }],
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value)
    .replace('T', ' ')
    .replace(/\.\d+$/, '')
}

function feedbackTypeText(row) {
  const map = { bug: '问题反馈', suggestion: '功能建议', complaint: '投诉' }
  return map[row.feedback_type || row.type] || row.feedback_type || row.type || '-'
}

function feedbackTypeTag(row) {
  const type = row.feedback_type || row.type || ''
  const map = { bug: 'danger', suggestion: 'primary', complaint: 'warning' }
  return map[type] || 'info'
}

function feedbackStatusText(row) {
  const map = { pending: '待处理', processing: '处理中', resolved: '已解决' }
  return map[row.feedback_status || row.status] || row.feedback_status || row.status || '-'
}

function feedbackStatusTag(row) {
  const status = row.feedback_status || row.status || ''
  const map = { pending: 'warning', processing: 'primary', resolved: 'success' }
  return map[status] || 'info'
}

function openThread(row) {
  selectedFeedback.value = row
  threadVisible.value = true
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getMyFeedbacks({ page: 1, pageSize: 50 })
    feedbacks.value = data.records || data || []
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await submitFeedback({ ...form })
    ElMessage.success('反馈已提交，感谢您的反馈')
    dialogVisible.value = false
    form.title = ''
    form.content = ''
    form.type = 'bug'
    fetchData()
  } catch {
    // handled
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.feedback-card {
  padding-bottom: 12px;
}

.feedback-table {
  cursor: pointer;
}

.feedback-table :deep(.el-table__row) {
  cursor: pointer;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.reply-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
}

.feedback-tip {
  padding: 12px 4px 0;
  font-size: 12px;
  color: #8b95a7;
}

.thread-header {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.thread-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.thread-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1b2432;
}

.thread-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: #7f8a9d;
}

.thread-body {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 18px;
  min-height: 260px;
  max-height: 60vh;
  overflow-y: auto;
  background:
    radial-gradient(circle at top left, rgba(26, 92, 200, 0.06), transparent 24%),
    linear-gradient(180deg, #fcfdff 0%, #f6f8fc 100%);
  border: 1px solid #edf1f7;
  border-radius: 22px;
}

.msg {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  max-width: 84%;
}

.msg-user {
  align-self: flex-end;
  flex-direction: row;
}

.msg-admin {
  align-self: flex-start;
}

.msg-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.msg-top {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #7f8a9d;
  padding: 0 4px;
}

.user-top {
  justify-content: flex-end;
}

.admin-top {
  justify-content: flex-start;
}

.msg-name {
  font-weight: 600;
  color: #4b5563;
  letter-spacing: 0.01em;
}

.msg-time {
  color: #9aa4b2;
}

.msg-avatar {
  width: 40px;
  height: 40px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(15, 41, 84, 0.08);
}

.msg-avatar span {
  position: relative;
  z-index: 1;
}

.user-avatar {
  color: #fff;
  background: linear-gradient(135deg, #2a6ae0 0%, #0f4fbf 100%);
}

.admin-avatar {
  color: #355c94;
  background: linear-gradient(135deg, #ffffff 0%, #edf4ff 100%);
  border: 1px solid #d8e5fb;
}

.msg-bubble {
  padding: 15px 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.75;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 10px 24px rgba(15, 41, 84, 0.06);
}

.user-bubble {
  color: #fff;
  background:
    linear-gradient(135deg, #2a6ae0 0%, #0b4dbf 100%);
  border-bottom-right-radius: 8px;
}

.admin-bubble {
  color: #1f2937;
  background: rgba(255,255,255,0.96);
  border: 1px solid #e6ebf3;
  border-bottom-left-radius: 8px;
}

.reply-placeholder {
  align-self: center;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  border-radius: 999px;
  background: rgba(255,255,255,0.88);
  border: 1px solid #e7edf6;
  color: #8b95a7;
  font-size: 12px;
}

.thread-footer-note {
  margin-top: 14px;
  padding: 12px 14px;
  border-radius: 14px;
  background: linear-gradient(180deg, #fafcff 0%, #f5f8fc 100%);
  border: 1px solid #e8edf5;
  color: #7f8a9d;
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 768px) {
  .thread-title-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .msg {
    max-width: 100%;
  }
}
</style>
