<template>
  <div class="violations-page">
    <div class="page-header">
      <el-icon class="page-icon" :size="22"><Warning /></el-icon>
      <h2>我的违约记录</h2>
    </div>

    <!-- Stats Card -->
    <div class="stats-card" v-if="stats">
      <div class="stat-big">
        <span class="big-num">{{ stats.total || violations.length }}</span>
        <span class="big-label">累计违约次数</span>
      </div>
      <div class="stat-divider"></div>
      <div class="stat-big">
        <span class="big-num" :class="{ 'text-danger': stats.accountStatus === 'suspended' }">
          {{ stats.accountStatus === 'active' ? '正常' : '已封禁' }}
        </span>
        <span class="big-label">账号状态</span>
      </div>
    </div>

    <div class="content-card" v-loading="loading">
      <el-table :data="violations" stripe style="width: 100%">
        <el-table-column label="日期" width="120">
          <template #default="{ row }">
            {{ row.occurred_at || row.occurredAt || row.date || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="自习室" min-width="120">
          <template #default="{ row }">
            {{ row.roomName || row.room_name || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="座位号" width="90">
          <template #default="{ row }">
            {{ row.seatCode || row.seat_code || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="违约类型" width="120">
          <template #default="{ row }">
            <el-tag :type="violationTypeTag(row)" size="small">
              {{ violationTypeText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="原因" min-width="180">
          <template #default="{ row }">
            {{ row.description_text || row.description || row.reason || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="violationStatusTag(row)" size="small">
              {{ violationStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="(row.violation_status || row.status) === 'active'"
              type="primary"
              text
              size="small"
              @click="goAppeal(row.id)"
            >
              申诉
            </el-button>
            <span v-else style="color:#a0aec0">-</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && violations.length === 0" description="暂无违约记录，继续保持！" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getMyViolations } from '../api/violations'

const router = useRouter()
const violations = ref([])
const stats = ref(null)
const loading = ref(false)

function violationTypeText(row) {
  const type = row.violation_type || row.violationType || row.type || ''
  const map = { no_checkin: '未签到', late_cancel: '临时取消', manual: '手动记录' }
  return map[type] || type
}

function violationTypeTag(row) {
  const type = row.violation_type || row.violationType || row.type || ''
  const map = { no_checkin: 'danger', late_cancel: 'warning', manual: 'info' }
  return map[type] || 'info'
}

function violationStatusText(row) {
  const status = row.violation_status || row.status || ''
  const map = { active: '生效中', revoked: '已撤销' }
  return map[status] || status
}

function violationStatusTag(row) {
  const status = row.violation_status || row.status || ''
  const map = { active: 'danger', revoked: 'info' }
  return map[status] || 'info'
}

function goAppeal(id) {
  router.push(`/violation/${id}/appeal`)
}

async function fetchData() {
  loading.value = true
  try {
    const data = await getMyViolations()
    violations.value = data.records || data.violations || data || []
    stats.value = data.stats || null
    if (!stats.value) {
      stats.value = {
        total: violations.value.length,
        accountStatus: 'active',
      }
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.stats-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 2px 12px rgba(0,56,147,0.06);
  display: flex;
  align-items: center;
  gap: 32px;
  margin-bottom: 20px;
}

.stat-big {
  text-align: center;
}

.big-num {
  font-size: 28px;
  font-weight: 700;
  color: #1a202c;
  display: block;
  line-height: 1;
}

.big-num.text-danger {
  color: #e53e3e;
}

.big-label {
  font-size: 12px;
  color: #8492a6;
  margin-top: 4px;
  display: block;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: #e8eaf2;
}
</style>
