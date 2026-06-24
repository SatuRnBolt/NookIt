<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-brand">
        <img src="../assets/logo.svg" alt="logo" class="login-logo" />
        <h1>校园自习空间</h1>
        <p>学生登录</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="handleLogin"
      >
        <el-form-item label="学号 / 邮箱" prop="identity">
          <el-input
            v-model="form.identity"
            placeholder="请输入学号或邮箱"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="login-btn"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </el-button>
      </el-form>

      <p class="login-tip">首次登录请使用学号 + 初始密码</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useStudentAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useStudentAuthStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  identity: '',
  password: '',
})

const rules = {
  identity: [{ required: true, message: '请输入学号或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const user = await auth.login(form.identity, form.password)
    ElMessage.success(`欢迎回来，${user.name}`)
    router.push('/home')
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f2557 0%, #1a3f7a 40%, #28539b 100%);
}

.login-card {
  width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 48px 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
}

.login-brand {
  text-align: center;
  margin-bottom: 36px;
}

.login-logo {
  width: 56px;
  height: 56px;
  margin-bottom: 16px;
}

.login-brand h1 {
  font-size: 22px;
  font-weight: 700;
  color: #1a202c;
  letter-spacing: 2px;
}

.login-brand p {
  font-size: 13px;
  color: #8492a6;
  margin-top: 4px;
}

.login-btn {
  width: 100%;
  margin-top: 8px;
  height: 44px;
  font-size: 15px;
  letter-spacing: 2px;
}

.login-tip {
  text-align: center;
  font-size: 12px;
  color: #a0aec0;
  margin-top: 20px;
}
</style>
