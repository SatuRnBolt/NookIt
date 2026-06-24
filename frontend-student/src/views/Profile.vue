<template>
  <div class="profile-page">
    <div class="page-header">
      <el-icon class="page-icon" :size="22"><User /></el-icon>
      <div>
        <h2>个人中心</h2>
        <p class="page-subtitle">查看个人资料、常用服务和系统偏好</p>
      </div>
    </div>

    <section class="hero-card">
      <div class="hero-accent"></div>
      <div class="hero-main">
        <div class="profile-avatar">
          <img :src="auth.user?.avatarUrl || defaultAvatar" class="profile-avatar-img" />
        </div>
        <div class="hero-info">
          <div class="hero-badges">
            <span class="hero-chip">学生账号</span>
            <span class="hero-chip hero-chip-light">状态正常</span>
          </div>
          <h3>{{ displayName }}</h3>
          <p>{{ profileIntro }}</p>
          <div class="hero-meta">
            <span>账号：{{ accountLabel }}</span>
            <span>邮箱：{{ auth.user?.email || '未绑定' }}</span>
          </div>
        </div>
      </div>

      <div class="hero-side">
        <div class="hero-stat">
          <span class="stat-label">身份类型</span>
          <strong>{{ roleLabel }}</strong>
        </div>
        <div class="hero-stat">
          <span class="stat-label">当前主题</span>
          <strong>{{ currentThemeName }}</strong>
        </div>
      </div>
    </section>

    <div class="profile-layout">
      <div class="profile-main">
        <section class="panel-card">
          <div class="panel-head">
            <div>
              <h3>个人信息</h3>
              <p>这些信息会用于预约记录、反馈沟通和身份识别</p>
            </div>
          </div>

          <div class="info-grid">
            <div
              v-for="item in profileFields"
              :key="item.label"
              class="info-item"
            >
              <span class="info-label">{{ item.label }}</span>
              <span class="info-value">{{ item.value }}</span>
            </div>
          </div>

          <div class="signature-editor">
            <div class="signature-head">
              <div>
                <h4>个性签名</h4>
                <p>签名会保存到你的账号资料里，首页右侧也会同步展示</p>
              </div>
              <span class="signature-tag">账号资料</span>
            </div>
            <el-input
              v-model="signatureDraft"
              type="textarea"
              :rows="3"
              maxlength="60"
              show-word-limit
              resize="none"
              placeholder="写一句展示在首页右侧卡片里的签名"
            />
            <div class="signature-actions">
              <el-button @click="resetSignatureDraft">恢复默认</el-button>
              <el-button type="primary" @click="saveSignature">保存签名</el-button>
            </div>
          </div>
        </section>

        <section class="panel-card">
          <div class="panel-head">
            <div>
              <h3>常用功能</h3>
              <p>把最常访问的服务放在个人中心里，会更符合用户习惯</p>
            </div>
          </div>

          <div class="action-grid">
            <button
              v-for="action in quickActions"
              :key="action.title"
              class="action-card"
              type="button"
              @click="router.push(action.path)"
            >
              <div class="action-icon" :class="action.iconClass">
                <el-icon :size="20">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <div class="action-body">
                <span class="action-title">{{ action.title }}</span>
                <span class="action-desc">{{ action.desc }}</span>
              </div>
              <el-icon :size="14" color="#b6bfcc"><ArrowRight /></el-icon>
            </button>
          </div>
        </section>
      </div>

      <aside class="profile-side">
        <section class="panel-card settings-card">
          <div class="panel-head">
            <div>
              <h3>系统设置</h3>
              <p>先放常见偏好项，后续如果接后端再扩展即可</p>
            </div>
          </div>

          <div class="setting-group">
            <span class="setting-label">界面主题</span>
            <div class="theme-options">
              <button
                v-for="theme in themeStore.themeList"
                :key="theme.id"
                type="button"
                class="theme-btn"
                :class="{ active: themeStore.theme === theme.id }"
                @click="themeStore.setTheme(theme.id)"
              >
                <span class="theme-dot" :style="{ background: theme.primary }"></span>
                <span>{{ theme.name }}</span>
              </button>
            </div>
          </div>

          <div class="setting-row">
            <div>
              <div class="setting-title">预约提醒</div>
              <div class="setting-desc">在个人设备上保留提醒偏好</div>
            </div>
            <el-switch v-model="settings.reminder" />
          </div>

          <div class="setting-row">
            <div>
              <div class="setting-title">消息通知</div>
              <div class="setting-desc">用于反馈回复和系统公告提示</div>
            </div>
            <el-switch v-model="settings.notification" />
          </div>

          <div class="setting-row">
            <div>
              <div class="setting-title">紧凑模式</div>
              <div class="setting-desc">减少卡片留白，适合信息密度更高的浏览</div>
            </div>
            <el-switch v-model="settings.compact" />
          </div>
        </section>

        <section class="panel-card">
          <div class="panel-head">
            <div>
              <h3>账号与安全</h3>
              <p>这里放用户通常会期待看到的账户相关操作</p>
            </div>
          </div>

          <div class="security-list">
            <div class="security-item">
              <span class="security-label">登录状态</span>
              <span class="security-value success">已登录</span>
            </div>
            <div class="security-item">
              <span class="security-label">邮箱绑定</span>
              <span class="security-value">{{ auth.user?.email ? '已绑定' : '未绑定' }}</span>
            </div>
            <div class="security-item">
              <span class="security-label">最近操作</span>
              <span class="security-value">可正常预约与申诉</span>
            </div>
          </div>

          <el-button class="full-btn" @click="router.push('/feedback')">联系管理员</el-button>
          <el-button type="danger" class="full-btn logout-btn" @click="handleLogout">
            <el-icon :size="14"><SwitchButton /></el-icon>
            退出登录
          </el-button>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowRight,
  Calendar,
  ChatDotRound,
  Document,
  Setting,
  SwitchButton,
  User,
  Warning,
} from '@element-plus/icons-vue'
import { useStudentAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'
import defaultAvatar from '../assets/stu-default-icon.png'

const router = useRouter()
const auth = useStudentAuthStore()
const themeStore = useThemeStore()
const DEFAULT_SIGNATURE = '保持专注，今天也认真学习。'

const SETTINGS_KEY = 'student_profile_settings'

function loadSettings() {
  try {
    const raw = localStorage.getItem(SETTINGS_KEY)
    if (!raw) {
      return { reminder: true, notification: true, compact: false }
    }
    return {
      reminder: true,
      notification: true,
      compact: false,
      ...JSON.parse(raw),
    }
  } catch {
    return { reminder: true, notification: true, compact: false }
  }
}

const settings = reactive(loadSettings())

watch(
  settings,
  (value) => {
    localStorage.setItem(SETTINGS_KEY, JSON.stringify(value))
    window.dispatchEvent(new CustomEvent('student-settings-changed'))
  },
  { deep: true }
)

const displayName = computed(() =>
  auth.user?.nickname || auth.user?.name || '未登录用户'
)

const roleLabel = computed(() =>
  auth.user?.userType === 'student' ? '学生' : auth.user?.userType || '校园用户'
)

const accountLabel = computed(() =>
  auth.user?.studentNo ||
  auth.user?.student_id ||
  auth.user?.identity ||
  auth.user?.id ||
  '未提供'
)

const profileIntro = computed(() => {
  if (!auth.user) return '登录后可以查看账号资料、预约服务和偏好设置。'
  return `${displayName.value}，欢迎回来。这里集中放你的资料信息、预约入口和常用设置。`
})

const currentThemeName = computed(() => {
  const current = themeStore.themeList.find(item => item.id === themeStore.theme)
  return current?.name || '默认主题'
})

const profileFields = computed(() => [
  { label: '姓名', value: auth.user?.name || auth.user?.nickname || '未填写' },
  { label: '账号', value: accountLabel.value },
  { label: '邮箱', value: auth.user?.email || '未绑定' },
  { label: '手机号', value: auth.user?.phone || auth.user?.mobile || '未填写' },
  { label: '身份', value: roleLabel.value },
  { label: '违约次数', value: auth.user?.violationCount ?? 0 },
])

const signatureDraft = ref(auth.user?.signature || DEFAULT_SIGNATURE)

watch(
  () => auth.user?.signature,
  (value) => {
    signatureDraft.value = value || DEFAULT_SIGNATURE
  }
)

const quickActions = [
  {
    title: '我的预约',
    desc: '查看预约记录、签到码和使用状态',
    path: '/reservations',
    icon: Calendar,
    iconClass: 'reserve-icon',
  },
  {
    title: '违约记录',
    desc: '查看违约情况并发起申诉',
    path: '/violations',
    icon: Warning,
    iconClass: 'violations-icon',
  },
  {
    title: '问题反馈',
    desc: '提交建议、报修问题或体验反馈',
    path: '/feedback',
    icon: ChatDotRound,
    iconClass: 'feedback-icon',
  },
  {
    title: '通知公告',
    desc: '查看开放时间调整和系统通知',
    path: '/notices',
    icon: Document,
    iconClass: 'notice-icon',
  },
  {
    title: '主题设置',
    desc: '切换界面配色，保留当前使用偏好',
    path: '/profile',
    icon: Setting,
    iconClass: 'setting-icon',
  },
]

async function saveSignature() {
  try {
    await auth.saveSignature(signatureDraft.value)
    ElMessage.success('个性签名已保存')
  } catch {
    // handled by interceptor
  }
}

async function resetSignatureDraft() {
  signatureDraft.value = DEFAULT_SIGNATURE
  try {
    await auth.saveSignature('')
    ElMessage.success('已恢复默认签名')
  } catch {
    // handled by interceptor
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '退出登录',
      cancelButtonText: '取消',
      type: 'warning',
    })
    auth.logout()
    router.push('/login')
  } catch {
    // cancelled
  }
}
</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-subtitle {
  margin-top: 4px;
  font-size: 13px;
  color: #8b95a7;
}

.hero-card {
  position: relative;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 32px;
  border-radius: 28px;
  color: #1b2432;
  background:
    radial-gradient(circle at top right, rgba(26, 92, 200, 0.08), transparent 26%),
    linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid rgba(207, 220, 239, 0.9);
  box-shadow: 0 18px 42px rgba(19, 45, 89, 0.08);
}

.hero-accent {
  position: absolute;
  left: 0;
  top: 22px;
  bottom: 22px;
  width: 5px;
  border-radius: 999px;
  background: linear-gradient(180deg, var(--nt-primary) 0%, #7aa5e8 100%);
}

.hero-main {
  display: flex;
  align-items: center;
  gap: 20px;
  min-width: 0;
  padding-left: 6px;
}

.profile-avatar {
  width: 78px;
  height: 78px;
  border-radius: 22px;
  background: linear-gradient(180deg, #fefefe 0%, #eef4fb 100%);
  border: 1px solid #dde6f2;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: inset 0 1px 0 rgba(255,255,255,0.7);
}

.profile-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-info {
  min-width: 0;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  background: #eef4ff;
  border: 1px solid #d9e6fb;
  color: #315c9d;
}

.hero-chip-light {
  background: #eff9f2;
  border-color: #d3ecd8;
  color: #2d7a47;
}

.hero-info h3 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.hero-info p {
  margin: 10px 0 14px;
  max-width: 620px;
  font-size: 14px;
  line-height: 1.7;
  color: #66758b;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  font-size: 13px;
  color: #7d8aa0;
}

.hero-side {
  min-width: 180px;
  display: grid;
  gap: 12px;
}

.hero-stat {
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(247, 250, 255, 0.92);
  border: 1px solid #e6edf7;
}

.stat-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #8a97ab;
}

.hero-stat strong {
  font-size: 17px;
  font-weight: 700;
  color: #233047;
}

.profile-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(300px, 0.95fr);
  gap: 24px;
  align-items: start;
}

.profile-main,
.profile-side {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.panel-card {
  background: #fff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 10px 32px rgba(15, 41, 84, 0.06);
  border: 1px solid #edf1f7;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel-head h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #1b2432;
}

.panel-head p {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.6;
  color: #8b95a7;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.signature-editor {
  margin-top: 18px;
  padding: 18px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fbfcff 0%, #f7f9fc 100%);
  border: 1px solid #edf1f7;
}

.signature-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.signature-head h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #1b2432;
}

.signature-head p {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: #8b95a7;
}

.signature-tag {
  flex-shrink: 0;
  align-self: flex-start;
  padding: 5px 10px;
  border-radius: 999px;
  background: #eef4ff;
  border: 1px solid #d9e6fb;
  color: #315c9d;
  font-size: 12px;
}

.signature-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}

.info-item {
  padding: 16px 18px;
  border-radius: 16px;
  background: #f7f9fc;
  border: 1px solid #edf1f7;
}

.info-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #8b95a7;
}

.info-value {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1b2432;
  word-break: break-word;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 14px;
  width: 100%;
  padding: 18px;
  border: 1px solid #edf1f7;
  border-radius: 18px;
  background: #fff;
  cursor: pointer;
  text-align: left;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  border-color: var(--nt-primary-border);
  box-shadow: 0 14px 28px rgba(15, 41, 84, 0.08);
}

.action-icon {
  width: 46px;
  height: 46px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-body {
  flex: 1;
  min-width: 0;
}

.action-title {
  display: block;
  font-size: 15px;
  font-weight: 700;
  color: #1b2432;
}

.action-desc {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.6;
  color: #8b95a7;
}

.violations-icon {
  background: #fdeaea;
  color: #dd4f4f;
}

.feedback-icon {
  background: #eaf1fe;
  color: #1f63d1;
}

.reserve-icon {
  background: #e8f7ef;
  color: #24925a;
}

.notice-icon {
  background: #fff5e5;
  color: #cf8a10;
}

.setting-icon {
  background: #eef1f7;
  color: #607086;
}

.settings-card {
  background:
    linear-gradient(180deg, #ffffff 0%, #fbfcff 100%);
}

.setting-group + .setting-row,
.setting-row + .setting-row {
  margin-top: 16px;
}

.setting-label {
  display: block;
  margin-bottom: 12px;
  font-size: 13px;
  font-weight: 600;
  color: #526074;
}

.theme-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.theme-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid #e4e9f2;
  background: #fff;
  color: #445064;
  cursor: pointer;
  transition: all 0.18s ease;
}

.theme-btn.active {
  border-color: var(--nt-primary);
  background: var(--nt-primary-light);
  color: var(--nt-primary-dark);
}

.theme-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 0;
  border-top: 1px solid #eef2f7;
}

.setting-title {
  font-size: 14px;
  font-weight: 600;
  color: #1b2432;
}

.setting-desc {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: #8b95a7;
}

.security-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 18px;
}

.security-item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  border-radius: 14px;
  background: #f7f9fc;
}

.security-label {
  font-size: 13px;
  color: #7f8a9d;
}

.security-value {
  font-size: 13px;
  font-weight: 600;
  color: #1b2432;
  text-align: right;
}

.security-value.success {
  color: #24925a;
}

.full-btn {
  width: 100%;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn {
  margin-top: 10px;
}

.logout-btn :deep(.el-icon) {
  margin-right: 6px;
}

@media (max-width: 1100px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }

  .hero-card {
    flex-direction: column;
  }

  .hero-side {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero-card,
  .panel-card {
    padding: 20px;
    border-radius: 18px;
  }

  .hero-accent {
    top: 18px;
    bottom: 18px;
  }

  .hero-main {
    align-items: flex-start;
  }

  .hero-info h3 {
    font-size: 24px;
  }

  .hero-side,
  .info-grid,
  .action-grid {
    grid-template-columns: 1fr;
  }

  .signature-head,
  .signature-actions {
    flex-direction: column;
    align-items: stretch;
  }

  .action-card,
  .setting-row,
  .security-item {
    align-items: flex-start;
  }

  .setting-row,
  .security-item {
    flex-direction: column;
  }
}
</style>
