<template>
  <div>
    <van-nav-bar title="问题反馈" left-arrow @click-left="router.back()" />
    <div style="padding: 12px">
      <div class="card">
        <van-form @submit="handleSubmit">
          <van-cell-group inset>
            <van-field
              v-model="form.type"
              is-link
              readonly
              name="type"
              label="反馈类型"
              :placeholder="form.type || '请选择'"
              @click="showTypePicker = true"
            />
            <van-field
              v-model="form.title"
              name="title"
              label="标题"
              placeholder="请输入反馈标题"
              :rules="[{ required: true, message: '请填写标题' }]"
              maxlength="100"
            />
            <van-field
              v-model="form.content"
              name="content"
              type="textarea"
              label="内容"
              placeholder="请详细描述您的问题或建议"
              rows="5"
              :rules="[{ required: true, message: '请填写内容' }]"
              maxlength="500"
              show-word-limit
            />
          </van-cell-group>
          <div style="padding: 16px">
            <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="loading"
              color="linear-gradient(135deg, #003893 0%, #1a5cc8 100%)"
              loading-text="提交中..."
            >
              提交反馈
            </van-button>
          </div>
        </van-form>
      </div>

      <!-- History -->
      <div class="card" style="margin-top: 0">
        <div class="section-label">历史反馈</div>
        <div v-if="history.length === 0 && !histLoading" class="empty-hint">暂无历史反馈</div>
        <van-loading v-if="histLoading" size="24" color="#003893" style="display:flex;justify-content:center;padding:16px" />
        <div v-for="item in history" :key="item.id" class="hist-item">
          <div class="hist-header">
            <span class="hist-title">{{ item.title }}</span>
            <span class="hist-status" :class="item.status">{{ statusLabel(item.status) }}</span>
          </div>
          <div class="hist-content">{{ item.content }}</div>
          <div class="hist-reply" v-if="item.reply">
            <strong>官方回复：</strong>{{ item.reply }}
          </div>
          <div class="hist-date">{{ item.createdAt || item.created_at || '' }}</div>
        </div>
      </div>
    </div>

    <van-popup v-model:show="showTypePicker" position="bottom" teleport=".phone-screen">
      <van-picker
        :columns="typeOptions"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showSuccessToast } from 'vant'
import { getMyFeedbacks, submitFeedback } from '../api/feedback'

const router = useRouter()

const form = reactive({ type: 'bug', title: '', content: '' })
const loading = ref(false)
const showTypePicker = ref(false)
const history = ref([])
const histLoading = ref(false)

const typeOptions = [
  { text: '问题报告', value: 'bug' },
  { text: '功能建议', value: 'suggestion' },
  { text: '投诉', value: 'complaint' },
]

const typeLabels = { bug: '问题报告', suggestion: '功能建议', complaint: '投诉' }
const statusLabels = { pending: '待处理', processing: '处理中', resolved: '已解决' }

function statusLabel(s) { return statusLabels[s] || s || '—' }

function onTypeConfirm({ selectedOptions }) {
  const opt = selectedOptions[0]
  form.type = opt?.value || 'bug'
  showTypePicker.value = false
}

async function handleSubmit() {
  loading.value = true
  try {
    await submitFeedback({ ...form })
    showSuccessToast('反馈已提交')
    form.title = ''
    form.content = ''
    loadHistory()
  } catch { /* handled */ }
  finally { loading.value = false }
}

async function loadHistory() {
  histLoading.value = true
  try {
    const data = await getMyFeedbacks({ page: 1, pageSize: 10 })
    history.value = data?.records || []
  } catch { /* handled */ }
  finally { histLoading.value = false }
}

onMounted(loadHistory)
</script>

<style scoped>
.section-label {
  font-size: 13px;
  font-weight: 700;
  color: #4a5568;
  margin-bottom: 12px;
}

.empty-hint {
  font-size: 13px;
  color: #a0aec0;
  text-align: center;
  padding: 16px 0;
}

.hist-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f2f8;
}

.hist-item:last-child { border-bottom: none; }

.hist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.hist-title {
  font-size: 14px;
  font-weight: 600;
  color: #1a202c;
}

.hist-status {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 8px;
  font-weight: 600;
}

.hist-status.pending { background: #fff7e6; color: #d97706; }
.hist-status.processing { background: #eff6ff; color: #2563eb; }
.hist-status.resolved { background: #f0fdf4; color: #16a34a; }

.hist-content {
  font-size: 12px;
  color: #6b7a99;
  margin-bottom: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.hist-reply {
  font-size: 12px;
  color: #003893;
  background: #eef4ff;
  border-radius: 6px;
  padding: 6px 10px;
  margin: 6px 0;
}

.hist-date {
  font-size: 11px;
  color: #c0c4cc;
}
</style>
