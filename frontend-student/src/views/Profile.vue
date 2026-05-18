<template>
  <div class="profile-page">
    <div class="page-header">
      <el-icon class="page-icon" :size="22"><User /></el-icon>
      <h2>个人中心</h2>
    </div>

    <!-- User Info Card -->
    <div class="profile-card">
      <div class="profile-avatar">
        <img :src="auth.user?.avatarUrl || defaultAvatar" class="profile-avatar-img" />
      </div>
      <div class="profile-info">
        <h3>{{ auth.user?.name || '未登录' }}</h3>
        <p>{{ auth.user?.email || '' }}</p>
        <el-tag size="small">{{ auth.user?.userType === 'student' ? '学生' : auth.user?.userType || '' }}</el-tag>
      </div>
    </div>

    <!-- Quick Links -->
    <div class="link-cards">
      <div class="link-card" @click="$router.push('/violations')">
        <div class="link-icon violations-icon">
          <el-icon :size="22"><Warning /></el-icon>
        </div>
        <div class="link-text">
          <span class="link-title">我的违约</span>
          <span class="link-desc">查看违约记录与申诉</span>
        </div>
        <el-icon :size="14" color="#c0c4cc"><ArrowRight /></el-icon>
      </div>

      <div class="link-card" @click="$router.push('/feedback')">
        <div class="link-icon feedback-icon">
          <el-icon :size="22"><ChatDotRound /></el-icon>
        </div>
        <div class="link-text">
          <span class="link-title">问题反馈</span>
          <span class="link-desc">提交问题或建议</span>
        </div>
        <el-icon :size="14" color="#c0c4cc"><ArrowRight /></el-icon>
      </div>

      <div class="link-card" @click="$router.push('/reservations')">
        <div class="link-icon reserve-icon">
          <el-icon :size="22"><Calendar /></el-icon>
        </div>
        <div class="link-text">
          <span class="link-title">我的预约</span>
          <span class="link-desc">查看预约记录</span>
        </div>
        <el-icon :size="14" color="#c0c4cc"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- Logout -->
    <el-button type="danger" class="logout-btn" @click="handleLogout">
      <el-icon :size="14"><SwitchButton /></el-icon>
      退出登录
    </el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useStudentAuthStore } from '../stores/auth'
import defaultAvatar from '../assets/stu-default-icon.png'

const router = useRouter()
const auth = useStudentAuthStore()

async function handleLogout() {
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
</script>

<style scoped>
.profile-card {
  background: linear-gradient(135deg, #163a74 0%, #28539b 56%, #3464b1 100%);
  border-radius: 16px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 20px;
  color: #fff;
  margin-bottom: 24px;
}

.profile-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #e8eaf2;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 3px solid rgba(255,255,255,0.4);
  overflow: hidden;
}

.profile-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.profile-info h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 4px;
}

.profile-info p {
  font-size: 13px;
  opacity: 0.7;
  margin-bottom: 8px;
}

.link-cards {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 24px;
}

.link-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0,56,147,0.04);
  transition: transform 0.15s, box-shadow 0.15s;
}

.link-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0,56,147,0.1);
}

.link-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.violations-icon {
  background: #fde8e8;
  color: #e53e3e;
}

.feedback-icon {
  background: #e8edf9;
  color: #003893;
}

.reserve-icon {
  background: #e1f0e8;
  color: #2f855a;
}

.link-text {
  flex: 1;
}

.link-title {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: #1a202c;
}

.link-desc {
  display: block;
  font-size: 12px;
  color: #a0aec0;
  margin-top: 2px;
}

.logout-btn {
  width: 100%;
  height: 44px;
  font-size: 14px;
  border-radius: 12px;
}
</style>
