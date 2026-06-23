<template>
  <div class="login-page">
    <div class="login-hero">
      <div class="login-logo">
        <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
          <rect width="48" height="48" rx="14" fill="#fff" fill-opacity="0.15"/>
          <path d="M24 10L38 18V30L24 38L10 30V18L24 10Z" stroke="#fff" stroke-width="2.5" fill="none"/>
          <circle cx="24" cy="24" r="5" fill="#fff" fill-opacity="0.9"/>
        </svg>
      </div>
      <h1 class="login-title">校园自习空间</h1>
      <p class="login-subtitle">Nookit · 随时随地预约学习座位</p>
    </div>

    <div class="login-card">
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="identity"
            name="identity"
            label="学号/邮箱"
            placeholder="请输入学号或邮箱"
            :rules="[{ required: true, message: '请填写学号或邮箱' }]"
            autocomplete="username"
          />
          <van-field
            v-model="password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
            autocomplete="current-password"
          />
        </van-cell-group>

        <div class="login-btn-wrap">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
            loading-text="登录中..."
            color="linear-gradient(135deg, #003893 0%, #1a5cc8 100%)"
          >
            登录
          </van-button>
        </div>
      </van-form>
    </div>

    <p class="login-footer">复旦大学自习室预约管理系统</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const identity = ref('')
const password = ref('')
const loading = ref(false)

async function handleLogin() {
  loading.value = true
  try {
    await auth.login(identity.value, password.value)
    router.replace('/home')
  } catch {
    showToast('登录失败，请检查账号密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #001f6b 0%, #003893 40%, #1a5cc8 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 0 40px;
}

.login-hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 24px 48px;
  color: #fff;
  gap: 12px;
}

.login-logo {
  margin-bottom: 4px;
}

.login-title {
  margin: 0;
  font-size: 26px;
  font-weight: 900;
  letter-spacing: 3px;
}

.login-subtitle {
  margin: 0;
  font-size: 13px;
  color: rgba(255,255,255,0.7);
  letter-spacing: 1px;
}

.login-card {
  width: calc(100% - 32px);
  background: #fff;
  border-radius: 20px;
  padding: 24px 0 8px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.2);
}

.login-btn-wrap {
  padding: 24px 16px 16px;
}

.login-footer {
  margin-top: 32px;
  font-size: 12px;
  color: rgba(255,255,255,0.4);
}
</style>
