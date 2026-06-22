<template>
  <div>
    <van-nav-bar title="我的违约" left-arrow @click-left="router.back()" />
    <div class="page-wrap">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadMore"
        >
          <div style="padding: 12px 12px 0">
            <div
              v-for="item in records"
              :key="item.id"
              class="vio-card"
            >
              <div class="vio-header">
                <div class="vio-room">{{ item.roomName || item.room_name || '—' }}</div>
                <span class="vio-count">第 {{ item.count || item.violation_count || 1 }} 次违约</span>
              </div>
              <div class="vio-detail">
                <div class="vio-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="18" rx="2"/><line x1="2" y1="9" x2="22" y2="9"/></svg>
                  {{ item.date || item.violation_date || '—' }}
                </div>
                <div class="vio-item">
                  <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 8v4l3 3"/></svg>
                  {{ item.time || item.violation_time || '—' }}
                </div>
              </div>
              <div class="vio-reason">{{ item.reason || '未签到离场' }}</div>
              <div class="vio-footer" v-if="!item.appealed && !item.appeal_status">
                <van-button
                  size="small"
                  plain
                  type="warning"
                  @click="showAppeal(item)"
                >
                  申请申诉
                </van-button>
              </div>
              <div class="appeal-status" v-else>
                申诉状态：{{ item.appeal_status || '已提交' }}
              </div>
            </div>
          </div>
          <van-empty v-if="!loading && records.length === 0" description="暂无违约记录" />
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- Appeal Dialog -->
    <van-dialog
      teleport=".phone-screen"
      v-model:show="showAppealDialog"
      title="申请申诉"
      show-cancel-button
      confirm-button-color="#003893"
      :before-close="onAppealClose"
    >
      <div style="padding: 16px">
        <van-field
          v-model="appealReason"
          type="textarea"
          placeholder="请描述申诉原因（至少10字）"
          rows="4"
          maxlength="300"
          show-word-limit
        />
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getMyViolations, submitAppeal } from '../api/violations'

const router = useRouter()
const records = ref([])
const page = ref(1)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const showAppealDialog = ref(false)
const appealReason = ref('')
const currentItem = ref(null)

async function fetchData(reset = false) {
  if (reset) { page.value = 1; records.value = []; finished.value = false }
  try {
    const data = await getMyViolations({ page: page.value, pageSize: 10 })
    const list = data?.records || []
    if (reset) records.value = list
    else records.value.push(...list)
    if (list.length < 10) finished.value = true
    else page.value++
  } catch { finished.value = true }
  finally { loading.value = false; refreshing.value = false }
}

function loadMore() { fetchData() }

function onRefresh() {
  page.value = 1
  records.value = []
  finished.value = false
  loadMore()
}

function showAppeal(item) {
  currentItem.value = item
  appealReason.value = ''
  showAppealDialog.value = true
}

async function onAppealClose(action) {
  if (action !== 'confirm') return true
  if (appealReason.value.trim().length < 10) {
    showToast('申诉原因至少10字')
    return false
  }
  try {
    await submitAppeal(currentItem.value.id, appealReason.value)
    showToast('申诉已提交')
    onRefresh()
    return true
  } catch {
    return false
  }
}
</script>

<style scoped>
.vio-card {
  background: #fff;
  border-radius: 14px;
  padding: 16px;
  margin-bottom: 10px;
  box-shadow: 0 2px 10px rgba(0,56,147,0.06);
}

.vio-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.vio-room {
  font-size: 15px;
  font-weight: 700;
}

.vio-count {
  font-size: 12px;
  color: #ef4444;
  font-weight: 600;
  background: #fef2f2;
  padding: 2px 8px;
  border-radius: 8px;
}

.vio-detail {
  display: flex;
  gap: 14px;
  margin-bottom: 8px;
}

.vio-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #6b7a99;
}

.vio-reason {
  font-size: 13px;
  color: #ef4444;
  background: #fef2f2;
  border-radius: 8px;
  padding: 6px 10px;
  margin-bottom: 10px;
}

.vio-footer {
  display: flex;
  justify-content: flex-end;
}

.appeal-status {
  font-size: 12px;
  color: #6b7a99;
  text-align: right;
}
</style>
