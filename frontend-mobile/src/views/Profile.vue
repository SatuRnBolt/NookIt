<template>
  <div class="page-wrap">
    <!-- Header -->
    <div class="profile-hero">
      <div class="avatar-wrap">
        <img :src="auth.user?.avatarUrl || defaultAvatar" class="avatar" />
      </div>
      <div class="user-info">
        <div class="user-name">{{ auth.user?.nickname || auth.user?.name || '—' }}</div>
        <div class="user-id">{{ auth.user?.studentId || auth.user?.student_id || auth.user?.email || '' }}</div>
      </div>
    </div>

    <!-- Stats Row -->
    <div class="stats-row card" style="margin: -20px 12px 12px">
      <div class="stat-box">
        <div class="stat-num">{{ stats.total }}</div>
        <div class="stat-key">总预约</div>
      </div>
      <div class="stat-sep"></div>
      <div class="stat-box">
        <div class="stat-num">{{ stats.completed }}</div>
        <div class="stat-key">已完成</div>
      </div>
      <div class="stat-sep"></div>
      <div class="stat-box">
        <div class="stat-num" style="color:#ef4444">{{ stats.violations }}</div>
        <div class="stat-key">违约次数</div>
      </div>
    </div>

    <!-- Menu Items -->
    <van-cell-group inset>
      <van-cell title="我的预约" is-link icon="records-o" @click="router.push('/reservations')" />
      <van-cell title="违约记录" is-link icon="warning-o" @click="router.push('/violations')" />
      <van-cell title="问题反馈" is-link icon="service-o" @click="router.push('/feedback')" />
    </van-cell-group>

    <van-cell-group inset style="margin-top: 12px">
      <van-cell
        title="个性签名"
        is-link
        :label="auth.user?.signature || '未设置'"
        icon="edit"
        @click="showSigDialog = true"
      />
      <van-cell title="修改密码" is-link icon="lock" @click="showPwdDialog = true" />
    </van-cell-group>

    <div style="padding: 24px 12px 0">
      <van-button
        round
        block
        type="danger"
        plain
        @click="handleLogout"
      >
        退出登录
      </van-button>
    </div>

    <!-- Signature Dialog -->
    <van-dialog
      teleport=".phone-screen"
      v-model:show="showSigDialog"
      title="修改个性签名"
      show-cancel-button
      confirm-button-color="#003893"
      :before-close="saveSig"
    >
      <div style="padding: 16px">
        <van-field
          v-model="newSig"
          placeholder="输入新的个性签名"
          maxlength="60"
          show-word-limit
        />
      </div>
    </van-dialog>

    <!-- Password Dialog -->
    <van-dialog
      teleport=".phone-screen"
      v-model:show="showPwdDialog"
      title="修改密码"
      show-cancel-button
      confirm-button-color="#003893"
      :before-close="savePwd"
    >
      <div style="padding: 16px; display: flex; flex-direction: column; gap: 8px">
        <van-field v-model="pwd.old" type="password" placeholder="当前密码" label="当前密码" />
        <van-field v-model="pwd.new1" type="password" placeholder="新密码" label="新密码" />
        <van-field v-model="pwd.new2" type="password" placeholder="确认新密码" label="确认密码" />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { useAuthStore } from '../stores/auth'
import { updateMySignature, changePassword } from '../api/auth'
import { getMyReservations } from '../api/reservations'
import { getMyViolations } from '../api/violations'

const router = useRouter()
const auth = useAuthStore()

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><circle cx="50" cy="50" r="50" fill="%23e8eaf2"/><circle cx="50" cy="38" r="18" fill="%23a0aec0"/><ellipse cx="50" cy="85" rx="28" ry="20" fill="%23a0aec0"/></svg>'

const stats = reactive({ total: 0, completed: 0, violations: 0 })
const showSigDialog = ref(false)
const showPwdDialog = ref(false)
const newSig = ref(auth.user?.signature || '')
const pwd = reactive({ old: '', new1: '', new2: '' })

onMounted(async () => {
  try {
    const [resData, vioData] = await Promise.all([
      getMyReservations({ page: 1, pageSize: 1 }).catch(() => null),
      getMyViolations({ page: 1, pageSize: 1 }).catch(() => null),
    ])
    stats.total = resData?.total || 0
    stats.completed = resData?.completedCount || 0
    stats.violations = vioData?.total || 0
  } catch { /* handled */ }
})

async function saveSig(action) {
  if (action !== 'confirm') return true
  try {
    await updateMySignature(newSig.value)
    if (auth.user) auth.user.signature = newSig.value
    showToast('签名已更新')
    return true
  } catch { return false }
}

async function savePwd(action) {
  if (action !== 'confirm') return true
  if (!pwd.old || !pwd.new1 || !pwd.new2) {
    showToast('请填写完整')
    return false
  }
  if (pwd.new1 !== pwd.new2) {
    showToast('两次密码不一致')
    return false
  }
  try {
    await changePassword({ oldPassword: pwd.old, newPassword: pwd.new1 })
    showToast('密码已修改')
    pwd.old = ''; pwd.new1 = ''; pwd.new2 = ''
    return true
  } catch { return false }
}

async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出登录吗？',
      confirmButtonColor: '#ef4444',
      confirmButtonText: '退出',
    })
    await auth.logout()
    router.replace('/login')
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.profile-hero {
  background: linear-gradient(135deg, #001f6b 0%, #003893 60%, #1a5cc8 100%);
  padding: 32px 24px 48px;
  display: flex;
  align-items: center;
  gap: 16px;
  color: #fff;
}

.avatar-wrap {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(255,255,255,0.3);
  flex-shrink: 0;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-name {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 4px;
}

.user-id {
  font-size: 13px;
  color: rgba(255,255,255,0.65);
}

.stats-row {
  display: flex;
  align-items: center;
  padding: 16px 0;
}

.stat-box {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.stat-num {
  font-size: 22px;
  font-weight: 800;
  color: #1a202c;
}

.stat-key {
  font-size: 11px;
  color: #a0aec0;
}

.stat-sep {
  width: 1px;
  height: 28px;
  background: #f0f2f8;
}
</style>
