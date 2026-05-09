<template>
  <div class="admin-layout">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-logo" :class="{ 'logo-collapsed': sidebarCollapsed }">
        <div class="logo-emblem">
          <img src="../assets/logo.svg" class="logo-img" alt="logo" />
        </div>
        <div class="logo-texts" v-if="!sidebarCollapsed">
          <span class="logo-title">校园自习空间</span>
          <span class="logo-sub">管理平台</span>
        </div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in filteredNavItems"
          :key="item.path"
          :to="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          :title="sidebarCollapsed ? item.label : ''"
        >
          <el-icon :size="19"><component :is="item.icon" /></el-icon>
          <span v-if="!sidebarCollapsed">{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="sidebar-user" v-if="!sidebarCollapsed">
          <div class="user-avatar">{{ auth.user?.name?.charAt(0) }}</div>
          <div class="user-info">
            <div class="user-name">{{ auth.user?.name }}</div>
            <div class="user-role">{{ auth.user?.roleName }}</div>
          </div>
          <el-tooltip content="退出登录" placement="top">
            <div class="logout-icon-btn" @click="handleLogout">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                <polyline points="16 17 21 12 16 7"/>
                <line x1="21" y1="12" x2="9" y2="12"/>
              </svg>
            </div>
          </el-tooltip>
        </div>
        <el-tooltip content="退出登录" placement="right" v-else>
          <div class="user-avatar-sm" @click="handleLogout">{{ auth.user?.name?.charAt(0) }}</div>
        </el-tooltip>
      </div>
    </aside>

    <!-- Main area -->
    <div class="main-area">
      <!-- Header -->
      <header class="admin-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="sidebarCollapsed = !sidebarCollapsed" :size="18">
            <Fold v-if="!sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <span class="header-sep"></span>
          <div class="header-page-info">
            <span class="header-page-title">{{ currentPageTitle }}</span>
            <span class="header-breadcrumb">首页 · {{ currentPageTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <div class="header-search">
            <el-icon class="search-icon" :size="15"><Search /></el-icon>
            <input class="search-input" placeholder="搜索功能、页面…" />
          </div>
          <span class="header-sep"></span>
          <el-tooltip content="通知" placement="bottom">
            <div class="notify-btn" @click="notifyVisible = !notifyVisible">
              <el-icon :size="18"><Bell /></el-icon>
              <span class="notify-dot" v-if="notifyCount > 0"></span>
            </div>
          </el-tooltip>
          <span class="header-sep"></span>
          <div class="header-clock">
            <span class="clock-time">{{ currentTime }}</span>
            <span class="clock-date">{{ currentDate }}</span>
          </div>
        </div>
      </header>

      <!-- Page content -->
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminAuthStore } from '../stores/auth'
import { ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const auth = useAdminAuthStore()
const sidebarCollapsed = ref(false)
const notifyVisible = ref(false)
const notifyCount = ref(0)

const currentTime = ref('')
const currentDate = ref('')

function updateClock() {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false })
  currentDate.value = now.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' }).replace(/\//g, '·')
}

let clockTimer
onMounted(() => { updateClock(); clockTimer = setInterval(updateClock, 1000) })
onUnmounted(() => clearInterval(clockTimer))

const navItems = [
  { path: '/dashboard', label: '数据看板', icon: 'Odometer', perm: null },
  { path: '/rooms', label: '自习室管理', icon: 'OfficeBuilding', perm: 'room.view' },
  { path: '/bookings', label: '预约记录', icon: 'Calendar', perm: 'booking.view' },
  { path: '/violations', label: '违约记录', icon: 'Warning', perm: 'violation.view' },
  { path: '/notices', label: '通知公告', icon: 'Bell', perm: null },
  { path: '/feedback', label: '问题反馈', icon: 'ChatDotRound', perm: null },
  { path: '/users', label: '用户管理', icon: 'User', perm: 'user.view' },
  { path: '/roles', label: '角色权限', icon: 'Lock', perm: 'role.manage' },
  { path: '/settings', label: '系统设置', icon: 'Setting', perm: 'settings.manage' },
]

const filteredNavItems = computed(() =>
  navItems.filter(item => !item.perm || auth.hasPermission(item.perm))
)

function isActive(path) {
  return route.path === path || route.path.startsWith(path + '/')
}

const currentPageTitle = computed(() => {
  const item = navItems.find(n => route.path.startsWith(n.path))
  return item?.label || '首页'
})

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出', {
      confirmButtonText: '退出', cancelButtonText: '取消', type: 'warning'
    })
    auth.logout()
    router.push('/login')
  } catch {}
}
</script>

<style scoped>
/* ─── Layout shell ─── */
.admin-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f4f6fb;
}

/* ─── Sidebar ─── */
.sidebar {
  width: 216px;
  background: #fff;
  border-right: 1px solid #e8eaf2;
  display: flex;
  flex-direction: column;
  transition: width 0.22s cubic-bezier(.4,0,.2,1);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 2px 0 8px rgba(0,56,147,0.04);
}

.sidebar.collapsed {
  width: 72px;
}

/* Logo / brand bar */
.sidebar-logo {
  height: 80px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #163a74 0%, #28539b 56%, #3464b1 100%);
  border-bottom: 2px solid rgba(255,255,255,0.1);
  transition: padding 0.22s cubic-bezier(.4,0,.2,1), justify-content 0.22s;
}

.sidebar-logo.logo-collapsed {
  padding: 0;
  justify-content: center;
}

.logo-emblem {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 4px 14px rgba(7,28,67,0.28);
  transition: width 0.22s, height 0.22s, border-radius 0.22s;
}

.logo-collapsed .logo-emblem {
  width: 42px;
  height: 42px;
  border-radius: 12px;
}

.logo-img {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.logo-collapsed .logo-img {
  width: 24px;
  height: 24px;
}

.logo-texts {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  gap: 5px;
}

.logo-title {
  font-size: 15px;
  font-weight: 700;
  line-height: 1;
  color: #fff;
  white-space: nowrap;
  letter-spacing: 1.5px;
  font-family: "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

.logo-sub {
  font-size: 11px;
  color: rgba(255,255,255,0.55);
  white-space: nowrap;
  letter-spacing: 1px;
  font-family: "Microsoft YaHei UI", "PingFang SC", sans-serif;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  padding: 14px 10px;
  overflow-y: auto;
  scrollbar-width: none;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.sidebar-nav::-webkit-scrollbar { display: none; }

.nav-item {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 12px 14px;
  border-radius: 8px;
  color: #4a5568;
  text-decoration: none;
  font-size: 14.5px;
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  position: relative;
}

.nav-item:hover {
  background: #eef2fa;
  color: #003893;
}

.nav-item.active {
  background: #e8edf9;
  color: #003893;
  font-weight: 600;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 2px;
  background: #003893;
}

/* Footer / user info */
.sidebar-footer {
  padding: 12px 10px;
  border-top: 1px solid #eaecf4;
  flex-shrink: 0;
}

.sidebar-user {
  display: flex;
  align-items: center;
  gap: 9px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #003893;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  flex-shrink: 0;
}

.user-avatar-sm {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #003893;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 700;
  margin: 0 auto;
  cursor: pointer;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: #1a202c;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.user-role {
  font-size: 11px;
  color: #8492a6;
  margin-top: 1px;
}

.logout-icon-btn {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #a0aec0;
  flex-shrink: 0;
  margin-left: auto;
  transition: background 0.15s, color 0.15s;
}

.logout-icon-btn:hover {
  background: #fee2e2;
  color: #e53e3e;
}

/* ─── Main area ─── */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e8eaf2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 28px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,56,147,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-sep {
  display: inline-block;
  width: 1px;
  height: 22px;
  background: #e2e6f0;
  flex-shrink: 0;
}

.header-page-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.header-page-title {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  line-height: 1;
}

.header-breadcrumb {
  font-size: 11px;
  color: #a0aec0;
  line-height: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-clock {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
}

.clock-time {
  font-size: 15px;
  font-weight: 700;
  color: #1a202c;
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.5px;
  line-height: 1;
}

.clock-date {
  font-size: 11px;
  color: #a0aec0;
  line-height: 1;
}

.notify-btn {
  position: relative;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #8492a6;
  transition: background 0.15s, color 0.15s;
}

.notify-btn:hover {
  background: #eef2fa;
  color: #003893;
}

.notify-dot {
  position: absolute;
  top: 6px;
  right: 7px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #e53e3e;
  border: 1.5px solid #fff;
}

.header-search {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #f4f6fb;
  border: 1px solid #e8eaf2;
  border-radius: 8px;
  padding: 0 12px;
  height: 34px;
  width: 200px;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.header-search:focus-within {
  border-color: #003893;
  box-shadow: 0 0 0 3px rgba(0,56,147,0.08);
}

.search-icon {
  color: #a0aec0;
  flex-shrink: 0;
}

.search-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 13px;
  color: #1a202c;
  width: 100%;
}

.search-input::placeholder {
  color: #c0c8d8;
}

.collapse-btn {
  cursor: pointer;
  color: #8492a6;
  transition: color 0.15s;
}

.collapse-btn:hover {
  color: #003893;
}

.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #f4f6fb;
}
</style>
