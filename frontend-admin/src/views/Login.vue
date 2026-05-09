<template>
  <div class="login-page">
    <div class="bg-circle bg-circle-1"></div>
    <div class="bg-circle bg-circle-2"></div>
    <div class="bg-circle bg-circle-3"></div>

    <div class="login-wrapper">
      <!-- ===== 左侧品牌区 ===== -->
      <div class="login-left">
        <div class="brand-area">
          <div class="brand-logo">
            <svg width="56" height="56" viewBox="0 0 56 56" fill="none">
              <circle cx="28" cy="28" r="28" fill="rgba(255,255,255,0.12)"/>
              <circle cx="28" cy="28" r="20" stroke="rgba(255,255,255,0.5)" stroke-width="1.5" fill="none"/>
              <path d="M28 18v20M18 28h20" stroke="white" stroke-width="2" stroke-linecap="round"/>
              <circle cx="28" cy="28" r="4" fill="white"/>
            </svg>
          </div>
          <div class="brand-text">
            <div class="brand-name">复旦大学</div>
            <div class="brand-sub">自习座位预约管理平台</div>
          </div>
        </div>

        <div class="hero-title">
          <h1>智慧校园</h1>
          <h1 class="highlight">自习空间</h1>
          <h1>一体化管理</h1>
        </div>

        <p class="hero-desc">
          统一管理全校自习室资源，高效处理预约、违约与权限配置，
          让每一个学习空间都发挥最大价值。
        </p>

        <div class="stats-row">
          <div class="stat-item" v-for="s in stats" :key="s.label">
            <div class="stat-num">{{ s.num }}</div>
            <div class="stat-label">{{ s.label }}</div>
          </div>
        </div>
      </div>

      <!-- ===== 右侧登录卡片 ===== -->
      <div class="login-right">
        <div class="login-card">
          <!-- 右上角斜三角切换按钮 -->
          <div class="qr-corner" @click="toggleMode" :title="loginMode === 'password' ? '切换至二维码登录' : '切换至账号登录'">
            <div class="qr-corner-triangle"></div>
            <div class="qr-corner-icon">
              <svg v-if="loginMode === 'password'" width="20" height="20" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="3" width="7" height="7" rx="1" stroke="white" stroke-width="1.8"/>
                <rect x="14" y="3" width="7" height="7" rx="1" stroke="white" stroke-width="1.8"/>
                <rect x="3" y="14" width="7" height="7" rx="1" stroke="white" stroke-width="1.8"/>
                <rect x="5" y="5" width="3" height="3" fill="white" rx="0.5"/>
                <rect x="16" y="5" width="3" height="3" fill="white" rx="0.5"/>
                <rect x="5" y="16" width="3" height="3" fill="white" rx="0.5"/>
                <rect x="14" y="14" width="2" height="2" fill="white"/>
                <rect x="17" y="14" width="2" height="2" fill="white"/>
                <rect x="14" y="17" width="2" height="2" fill="white"/>
                <rect x="17" y="17" width="2" height="2" fill="white"/>
              </svg>
              <svg v-else width="20" height="20" viewBox="0 0 24 24" fill="none">
                <rect x="3" y="7" width="18" height="3" rx="1.5" fill="white"/>
                <rect x="3" y="14" width="14" height="3" rx="1.5" fill="white"/>
              </svg>
            </div>
          </div>

          <!-- 账号密码登录 -->
          <template v-if="loginMode === 'password'">
            <div class="card-top">
              <h2>管理员登录</h2>
              <p>欢迎使用自习座位预约管理系统</p>
            </div>

            <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
              <el-form-item prop="email">
                <label class="field-label">账号</label>
                <el-input v-model="form.email" placeholder="请输入管理员账号" size="large" clearable />
              </el-form-item>
              <el-form-item prop="password">
                <label class="field-label">密码</label>
                <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password clearable />
              </el-form-item>

              <div class="form-footer-row">
                <el-checkbox>记住登录状态</el-checkbox>
                <a class="forgot-link" href="javascript:void(0)">忘记密码？</a>
              </div>

              <el-button type="primary" size="large" :loading="loading" native-type="submit" @click="handleLogin" class="login-btn">
                {{ loading ? '登录中...' : '登 录' }}
              </el-button>
            </el-form>

            <div class="divider"><span>测试账号</span></div>
            <div class="demo-hint">
              <div class="hint-row">
                <span class="hint-key">账号</span>
                <code>admin@study.edu.cn</code>
              </div>
              <div class="hint-row">
                <span class="hint-key">密码</span>
                <code>admin123</code>
              </div>
            </div>
          </template>

          <!-- 二维码登录 -->
          <template v-else>
            <div class="card-top">
              <h2>扫码登录</h2>
              <p>使用复旦校园通 App 扫描二维码</p>
            </div>
            <div class="qr-body">
              <div class="qr-wrap">
                <!-- 模拟二维码 -->
                <svg width="160" height="160" viewBox="0 0 160 160">
                  <rect width="160" height="160" fill="white"/>
                  <!-- 三个定位角 -->
                  <rect x="10" y="10" width="44" height="44" rx="4" fill="none" stroke="#1a56db" stroke-width="4"/>
                  <rect x="17" y="17" width="30" height="30" rx="2" fill="#1a56db"/>
                  <rect x="106" y="10" width="44" height="44" rx="4" fill="none" stroke="#1a56db" stroke-width="4"/>
                  <rect x="113" y="17" width="30" height="30" rx="2" fill="#1a56db"/>
                  <rect x="10" y="106" width="44" height="44" rx="4" fill="none" stroke="#1a56db" stroke-width="4"/>
                  <rect x="17" y="113" width="30" height="30" rx="2" fill="#1a56db"/>
                  <!-- 随机数据点模拟 -->
                  <rect x="64" y="10" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="78" y="10" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="92" y="10" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="24" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="92" y="24" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="38" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="78" y="38" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="10" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="24" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="38" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="10" y="78" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="38" y="78" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="10" y="92" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="24" y="92" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="78" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="92" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="106" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="120" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="134" y="64" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="78" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="92" y="78" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="120" y="78" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="92" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="78" y="92" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="106" y="92" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="134" y="92" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="106" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="92" y="106" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="106" y="106" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="120" y="106" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="120" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="78" y="120" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="134" y="120" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="64" y="134" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="92" y="134" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="106" y="134" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="120" y="134" width="8" height="8" rx="1" fill="#1a56db"/>
                  <rect x="134" y="134" width="8" height="8" rx="1" fill="#1a56db"/>
                </svg>
                <div class="qr-scan-line"></div>
              </div>
              <p class="qr-tip">二维码有效期 3 分钟，过期后请刷新</p>
              <button class="qr-refresh">↻ &nbsp;刷新二维码</button>
            </div>
          </template>
        </div>

        <div class="card-footer">
          © 2026 复旦大学 &nbsp;·&nbsp; 信息化建设与管理办公室
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAdminAuthStore()

const formRef = ref()
const loading = ref(false)
const loginMode = ref('password') // 'password' | 'qrcode'

const form = reactive({
  email: 'admin@study.edu.cn',
  password: 'admin123',
})

const rules = {
  email: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const stats = [
  { num: '12', label: '自习室' },
  { num: '680', label: '座位数' },
  { num: '4,200+', label: '日均预约' },
]

function toggleMode() {
  loginMode.value = loginMode.value === 'password' ? 'qrcode' : 'password'
}

async function handleLogin() {
  if (!form.email || !form.password) {
    ElMessage.warning('请填写账号和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(form.email, form.password)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    ElMessage.error('账号或密码错误，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ===== 页面基底 ===== */
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #0f2057 0%, #1a3a8f 45%, #1565c0 100%);
  display: flex;
  position: relative;
  overflow: hidden;
}

/* ===== 背景装饰圆 ===== */
.bg-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255,255,255,0.04);
  pointer-events: none;
}
.bg-circle-1 { width: 800px; height: 800px; top: -300px; left: -200px; }
.bg-circle-2 { width: 500px; height: 500px; bottom: -150px; left: 100px; background: rgba(255,255,255,0.03); }
.bg-circle-3 { width: 420px; height: 420px; top: 60px; right: 380px; }

/* ===== 外层容器 ===== */
.login-wrapper {
  display: flex;
  width: 100%;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* ===== 左侧 ===== */
.login-left {
  flex: 1;
  color: #fff;
  padding: 48px 64px 64px;
  display: flex;
  flex-direction: column;
}

/* 品牌锚定在左上角 */
.brand-area {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-name {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 4px;
  color: #fff;
}

.brand-sub {
  font-size: 12px;
  color: rgba(255,255,255,0.5);
  letter-spacing: 1.5px;
  margin-top: 3px;
}

/* 大标题居中撑满剩余高度 */
.hero-title {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-bottom: 24px;
}

/* 阶梯错落：每行不同缩进 + 不同字号 + 不同透明度 */
.hero-title h1 {
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: 4px;
  margin: 0;
}

.hero-title h1:nth-child(1) {
  font-size: 52px;
  color: rgba(255,255,255,0.6);
  margin-left: 0;
}

.hero-title h1:nth-child(2) {
  font-size: 76px;
  color: #fff;
  margin-left: 56px;
  position: relative;
  display: inline-block;
}

.hero-title h1:nth-child(2)::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 8px;
  width: 100%;
  height: 12px;
  background: rgba(255,210,0,0.3);
  z-index: -1;
  border-radius: 4px;
}

.hero-title h1:nth-child(3) {
  font-size: 44px;
  color: rgba(255,255,255,0.5);
  margin-left: 112px;
}

.hero-desc {
  font-size: 14px;
  color: rgba(255,255,255,0.45);
  line-height: 2;
  max-width: 420px;
  margin-left: 4px;
}

.stats-row {
  display: flex;
  gap: 48px;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid rgba(255,255,255,0.1);
}

.stat-num {
  font-size: 34px;
  font-weight: 700;
  color: #fff;
  line-height: 1;
  letter-spacing: 1px;
}

.stat-label {
  font-size: 12px;
  color: rgba(255,255,255,0.4);
  margin-top: 8px;
}

/* ===== 右侧 ===== */
.login-right {
  width: 46%;
  min-width: 500px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 64px;
  gap: 16px;
  backdrop-filter: blur(2px);
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 40px 32px;
  box-shadow: 0 24px 60px rgba(0,0,0,0.28), 0 4px 16px rgba(0,0,0,0.1);
  position: relative;
  overflow: hidden;
  width: 100%;
  max-width: 420px;
}

/* ===== 右上角斜三角切换 ===== */
.qr-corner {
  position: absolute;
  top: 0;
  right: 0;
  width: 72px;
  height: 72px;
  cursor: pointer;
  z-index: 10;
}

.qr-corner-triangle {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 72px 72px 0;
  border-color: transparent #2563eb transparent transparent;
  transition: border-color 0.2s;
}

.qr-corner:hover .qr-corner-triangle {
  border-color: transparent #1a4fd8 transparent transparent;
}

.qr-corner-icon {
  position: absolute;
  top: 9px;
  right: 9px;
  opacity: 0.95;
  line-height: 0;
}

/* ===== 表单区 ===== */
.card-top {
  margin-bottom: 24px;
  padding-right: 48px;
}

.card-top h2 {
  font-size: 22px;
  font-weight: 700;
  color: #111827;
}

.card-top p {
  font-size: 13px;
  color: #9ca3af;
  margin-top: 4px;
}

.field-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  margin-bottom: 6px;
}

:deep(.el-form-item) {
  margin-bottom: 18px;
}

:deep(.el-form-item__content) {
  flex-direction: column;
  align-items: stretch;
}

:deep(.el-form-item__error) {
  position: static;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e5e7eb;
  transition: box-shadow 0.2s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px #2563eb;
}

.form-footer-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
  margin-top: -4px;
}

:deep(.el-checkbox__label) {
  font-size: 13px;
  color: #6b7280;
}

.forgot-link {
  font-size: 13px;
  color: #2563eb;
  text-decoration: none;
}
.forgot-link:hover { text-decoration: underline; }

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  letter-spacing: 4px;
  border-radius: 8px;
  background: linear-gradient(90deg, #1a56db, #2563eb);
  border: none;
  box-shadow: 0 4px 14px rgba(37,99,235,0.45);
  transition: box-shadow 0.2s, transform 0.15s;
}
.login-btn:hover {
  box-shadow: 0 6px 20px rgba(37,99,235,0.55);
  transform: translateY(-1px);
}
.login-btn:active { transform: translateY(0); }

/* 分割线 */
.divider {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 18px 0 12px;
}
.divider::before, .divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #e5e7eb;
}
.divider span { font-size: 12px; color: #9ca3af; white-space: nowrap; }

/* 测试账号提示 */
.demo-hint {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.hint-row {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
}
.hint-key { color: #9ca3af; width: 28px; }
.hint-row code {
  color: #2563eb;
  background: #eff6ff;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'SF Mono', Consolas, monospace;
}

/* ===== 二维码面板 ===== */
.qr-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0 4px;
  gap: 16px;
}

.qr-wrap {
  position: relative;
  width: 176px;
  height: 176px;
  padding: 8px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.qr-scan-line {
  position: absolute;
  left: 8px;
  right: 8px;
  height: 2px;
  background: linear-gradient(90deg, transparent, #2563eb, transparent);
  top: 30%;
  animation: scan 2s ease-in-out infinite;
  border-radius: 1px;
}

@keyframes scan {
  0%   { top: 15%; opacity: 0; }
  10%  { opacity: 1; }
  90%  { opacity: 1; }
  100% { top: 83%; opacity: 0; }
}

.qr-tip {
  font-size: 12px;
  color: #9ca3af;
  text-align: center;
}

.qr-refresh {
  font-size: 13px;
  color: #2563eb;
  background: none;
  border: 1px solid #2563eb;
  border-radius: 20px;
  padding: 5px 20px;
  cursor: pointer;
  transition: background 0.15s;
}
.qr-refresh:hover { background: #eff6ff; }

/* ===== 底部版权 ===== */
.card-footer {
  text-align: center;
  font-size: 12px;
  color: rgba(255,255,255,0.3);
}

/* ===== 响应式 ===== */
@media (max-width: 860px) {
  .login-left { display: none; }
  .login-right { width: 100%; min-width: unset; padding: 64px 32px; background: none; }
  .login-wrapper { justify-content: center; }
}
</style>
