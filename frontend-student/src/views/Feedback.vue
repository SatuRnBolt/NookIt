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

    <div class="content-card" v-loading="loading">
      <el-table :data="feedbacks" stripe style="width: 100%">
        <el-table-column label="标题" min-width="180">
          <template #default="{ row }">
            {{ row.title }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
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
        <el-table-column label="提交时间" width="170">
          <template #default="{ row }">
            {{ row.created_at || row.createdAt || '-' }}
          </template>
        </el-table-column>
      </el-table>

      <el-table-column type="expand" v-if="false">
        <template #default="{ row }">
          <div class="expand-content" v-if="row.reply_content || row.replyContent">
            <div class="reply-label">管理员回复：</div>
            <div class="reply-text">{{ row.reply_content || row.replyContent }}</div>
            <div class="reply-meta" v-if="row.replied_at || row.repliedAt">
              {{ row.replied_at || row.repliedAt }}
            </div>
          </div>
        </template>
      </el-table-column>

      <el-empty v-if="!loading && feedbacks.length === 0" description="暂无反馈记录" />
    </div>

    <!-- Submit Dialog -->
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
            <el-option label="bug — 问题反馈" value="bug" />
            <el-option label="suggestion — 功能建议" value="suggestion" />
            <el-option label="complaint — 投诉" value="complaint" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyFeedbacks, submitFeedback } from '../api/feedback'

const feedbacks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

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
  const map = { pending: 'warning', processing: '', resolved: 'success' }
  return map[status] || 'info'
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
    ElMessage.success('反馈已提交，感谢您的反馈！')
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
/* uses global content-card and page-header styles */
</style>
