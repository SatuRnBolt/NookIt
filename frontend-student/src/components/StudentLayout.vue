<template>
  <div class="student-layout">
    <header class="top-nav">
      <div class="nav-inner">
        <router-link to="/home" class="nav-brand">
          <img src="../assets/logo.svg" alt="logo" class="brand-logo" />
          <span class="brand-title">校园自习空间</span>
        </router-link>

        <nav class="nav-links">
          <router-link to="/home" class="nav-link" :class="{ active: isActive('/home') }">
            <el-icon :size="16"><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link to="/reservations" class="nav-link" :class="{ active: isActive('/reservations') }">
            <el-icon :size="16"><Calendar /></el-icon>
            <span>我的预约</span>
          </router-link>
          <router-link to="/notices" class="nav-link" :class="{ active: isActive('/notices') }">
            <el-icon :size="16"><Bell /></el-icon>
            <span>通知公告</span>
          </router-link>
          <router-link to="/feedback" class="nav-link" :class="{ active: isActive('/feedback') }">
            <el-icon :size="16"><ChatDotRound /></el-icon>
            <span>问题反馈</span>
          </router-link>
        </nav>

        <div class="nav-ai-wrap">
          <button class="nav-ai-btn" @click="router.push('/ai-chat')">
            <img src="../assets/deepseek-color.svg" class="nav-ai-icon" />
            <span class="ai-online-dot"></span>
          </button>
          <div class="ai-hover-card">
            <img src="../assets/deepseek-color.svg" class="ai-card-logo" />
            <div class="ai-card-body">
              <div class="ai-card-name">Nookit AI</div>
              <div class="ai-card-status">
                <span class="ai-card-dot"></span>在线 · 智能预约助手
              </div>
            </div>
          </div>
        </div>

        <div class="nav-theme-wrap">
          <button class="nav-theme-btn" @click.stop="showPicker = !showPicker">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
          </button>
          <div v-if="showPicker" class="theme-picker">
            <div class="theme-picker-title">选择主题</div>
            <div class="theme-options">
              <button
                v-for="t in themeStore.themeList"
                :key="t.id"
                class="theme-option"
                :class="{ 'theme-option-active': themeStore.theme === t.id }"
                @click="selectTheme(t.id)"
              >
                <span class="theme-swatch" :style="{ background: t.primary }">
                  <svg v-if="themeStore.theme === t.id" width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>
                </span>
                <span class="theme-label">{{ t.name }}</span>
              </button>
            </div>
          </div>
          <div v-if="showPicker" class="theme-backdrop" @click="showPicker = false"></div>
        </div>

        <div class="nav-notice-wrap">
          <button
            class="nav-notice-btn"
            :class="{ active: isActive('/notices') }"
            @click="router.push('/notices')"
            :aria-label="noticeEnabled ? '消息通知已开启' : '消息通知已关闭'"
            :title="noticeEnabled ? '消息通知已开启' : '消息通知已关闭'"
          >
            <el-icon :size="18"><Bell /></el-icon>
            <span v-if="noticeEnabled" class="notice-dot"></span>
          </button>
        </div>

        <div class="nav-user">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-trigger">
              <div class="user-avatar">
                <img :src="auth.user?.avatarUrl || defaultAvatar" class="avatar-img" />
              </div>
              <span class="user-name">{{ auth.user?.nickname || auth.user?.name }}</span>
              <el-icon :size="14"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon :size="14"><User /></el-icon>
                  <span>个人中心</span>
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon :size="14"><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </header>

    <main class="main-content">
      <div class="content-inner">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useStudentAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
import { ElMessageBox } from 'element-plus'
import defaultAvatar from '../assets/stu-default-icon.png'

const route = useRoute()
const router = useRouter()
const auth = useStudentAuthStore()
const themeStore = useThemeStore()
const showPicker = ref(false)
const SETTINGS_KEY = 'student_profile_settings'
const noticeEnabled = ref(true)

function syncNoticeSetting() {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY)
    if (!raw) {
      noticeEnabled.value = true
      return
    }
    const parsed = JSON.parse(raw)
    noticeEnabled.value = parsed.notification !== false
  } catch {
    noticeEnabled.value = true
  }
}

function handleNoticeSettingChanged() {
  syncNoticeSetting()
}

onMounted(() => {
  syncNoticeSetting()
  window.addEventListener('student-settings-changed', handleNoticeSettingChanged)
})

onBeforeUnmount(() => {
  window.removeEventListener('student-settings-changed', handleNoticeSettingChanged)
})

function selectTheme(id) {
  themeStore.setTheme(id)
  showPicker.value = false
}

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

async function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '退出', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning',
      })
      auth.logout()
      router.push('/login')
    } catch {
      // cancelled
    }
  }
}
</script>

<style scoped>
.student-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f4f6fb;
}

/* ─── Top Nav ─── */
.top-nav {
  height: 72px;
  background: #fff;
  border-bottom: 1px solid #e8eaf2;
  flex-shrink: 0;
  box-shadow: 0 2px 12px rgba(0, 56, 147, 0.07);
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-inner {
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 40px;
}

.nav-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-right: 40px;
  flex-shrink: 0;
}

.brand-logo {
  width: 38px;
  height: 38px;
}

.brand-title {
  font-size: 19px;
  font-weight: 900;
  letter-spacing: 2px;
  white-space: nowrap;
  background: linear-gradient(135deg, #001f6b 0%, #1a5cc8 50%, #5a9de8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  filter: drop-shadow(0 1px 2px rgba(0, 56, 147, 0.15));
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 8px;
  font-size: 15px;
  color: #4a5568;
  transition: background 0.15s, color 0.15s;
}

.nav-link:hover {
  background: var(--nt-primary-light);
  color: var(--nt-primary-dark);
}

.nav-link.active {
  background: var(--nt-primary-light);
  color: var(--nt-primary-dark);
  font-weight: 600;
}

/* ─── AI Entry Button ─── */
.nav-ai-wrap {
  position: relative;
  margin-right: 14px;
}

.nav-ai-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef3ff;
  border: 1.5px solid #d0def8;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.nav-ai-btn:hover {
  background: #e0ebff;
  border-color: #1a5cc8;
  box-shadow: 0 4px 14px rgba(26,92,200,0.18);
  transform: translateY(-1px);
}

/* ─── Theme Picker ─── */
.nav-theme-wrap {
  position: relative;
  margin-right: 10px;
  flex-shrink: 0;
}

.nav-theme-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1.5px solid #e8eaf2;
  border-radius: 10px;
  color: #8492a6;
  cursor: pointer;
  transition: all 0.18s;
}

.nav-theme-btn:hover {
  background: var(--nt-primary-light);
  color: var(--nt-primary);
  border-color: var(--nt-primary-border);
}

.theme-picker {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #eef0f8;
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  padding: 14px;
  z-index: 300;
  min-width: 160px;
}

.theme-picker-title {
  font-size: 11px;
  font-weight: 600;
  color: #a0aec0;
  letter-spacing: 0.5px;
  text-transform: uppercase;
  margin-bottom: 10px;
}

.theme-options {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.theme-option {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 8px;
  border-radius: 8px;
  border: none;
  background: none;
  cursor: pointer;
  width: 100%;
  transition: background 0.15s;
}

.theme-option:hover {
  background: #f4f6fb;
}

.theme-option-active {
  background: #f4f6fb;
}

.theme-swatch {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 1px 4px rgba(0,0,0,0.15);
}

.theme-label {
  font-size: 13px;
  color: #2d3748;
  font-weight: 500;
}

.theme-option-active .theme-label {
  color: var(--nt-primary);
  font-weight: 600;
}

.theme-backdrop {
  position: fixed;
  inset: 0;
  z-index: 299;
}

.nav-notice-wrap {
  margin-right: 10px;
  flex-shrink: 0;
}

.nav-notice-btn {
  position: relative;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: 1.5px solid #e8eaf2;
  border-radius: 10px;
  color: #8492a6;
  cursor: pointer;
  transition: all 0.18s;
}

.nav-notice-btn:hover {
  background: var(--nt-primary-light);
  color: var(--nt-primary);
  border-color: var(--nt-primary-border);
}

.nav-notice-btn.active {
  background: var(--nt-primary-light);
  color: var(--nt-primary-dark);
  border-color: var(--nt-primary-border);
}

.notice-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ef4444;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(239, 68, 68, 0.22);
}

.nav-ai-icon {
  width: 22px;
  height: 22px;
}

.ai-online-dot {
  position: absolute;
  top: -3px;
  right: -3px;
  width: 9px;
  height: 9px;
  border-radius: 50%;
  background: #22c55e;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(34,197,94,0.3);
}

/* ─── AI Hover Card ─── */
.ai-hover-card {
  position: absolute;
  top: calc(100% + 10px);
  right: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #eef0f8;
  box-shadow: 0 8px 24px rgba(0,0,0,0.1);
  padding: 10px 14px;
  white-space: nowrap;
  opacity: 0;
  transform: translateY(-6px);
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
  z-index: 200;
}

.nav-ai-wrap:hover .ai-hover-card {
  opacity: 1;
  transform: translateY(0);
  pointer-events: all;
}

.ai-card-logo {
  width: 28px;
  height: 28px;
  flex-shrink: 0;
}

.ai-card-name {
  font-size: 13px;
  font-weight: 700;
  color: #1a202c;
  margin-bottom: 2px;
}

.ai-card-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #6b7a99;
}

.ai-card-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: #22c55e;
  flex-shrink: 0;
}

/* ─── User Dropdown ─── */
.nav-user {
  margin-left: auto;
  flex-shrink: 0;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.15s;
}

.user-trigger:hover {
  background: #f4f6fb;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e8eaf2;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 700;
  overflow: hidden;
  flex-shrink: 0;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.user-name {
  font-size: 14px;
  color: #1a202c;
  font-weight: 500;
}

/* ─── Main Content ─── */
.main-content {
  flex: 1;
  overflow-y: auto;
}

.content-inner {
  padding: 24px 40px;
}
</style>
