<template>
  <div class="mobile-app">
    <div class="mobile-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" :fixed="false" safe-area-inset-bottom>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="records-o" to="/reservations">我的预约</van-tabbar-item>
      <van-tabbar-item to="/checkin">
        <template #icon="{ active }">
          <div class="checkin-tab-icon" :class="{ 'checkin-tab-icon-active': active }">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 11l3 3L22 4"/>
              <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
            </svg>
          </div>
        </template>
        签到
      </van-tabbar-item>
      <van-tabbar-item icon="bell" to="/notices">消息</van-tabbar-item>
      <van-tabbar-item icon="contact-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>


<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const tabMap = {
  '/home': 0,
  '/reservations': 1,
  '/checkin': 2,
  '/notices': 3,
  '/profile': 4,
}

const active = ref(0)

watch(() => route.path, (path) => {
  if (tabMap[path] !== undefined) {
    active.value = tabMap[path]
  }
}, { immediate: true })
</script>

<style scoped>
.mobile-app {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--nt-bg);
}

.mobile-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.checkin-tab-icon {
  color: #969799;
  display: flex;
  align-items: center;
  justify-content: center;
}

.checkin-tab-icon-active {
  color: var(--nt-primary);
}
</style>
