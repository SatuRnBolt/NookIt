<template>
  <div class="appeal-page">
    <div class="page-header">
      <el-button text @click="router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>违约申诉</h2>
    </div>

    <div class="content-card appeal-card">
      <h3>申诉表单</h3>
      <p class="appeal-hint">请详细说明违约原因，管理员将在48小时内审核。</p>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="申诉理由" prop="reason">
          <el-input
            v-model="form.reason"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请详细说明您的情况..."
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          class="submit-btn"
          @click="handleSubmit"
        >
          {{ submitting ? '提交中...' : '提交申诉' }}
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { appealViolation } from '../api/violations'

const route = useRoute()
const router = useRouter()
const violationId = route.params.id

const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  reason: '',
})

const rules = {
  reason: [
    { required: true, message: '请输入申诉理由', trigger: 'blur' },
    { min: 10, message: '申诉理由至少10个字', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await appealViolation(violationId, form.reason)
    ElMessage.success('申诉已提交，请等待管理员审核')
    router.push('/violations')
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.appeal-card {
  max-width: 600px;
}

.appeal-card h3 {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 6px;
}

.appeal-hint {
  font-size: 13px;
  color: #8492a6;
  margin-bottom: 24px;
}

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
}
</style>
