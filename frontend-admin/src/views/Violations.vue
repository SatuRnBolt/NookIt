<template>
  <div>
    <div class="page-title">
      <el-icon><Warning /></el-icon> 违约记录
    </div>

    <div class="toolbar">
      <el-input v-model="search" placeholder="搜索学生姓名/学号" prefix-icon="Search" clearable style="width:200px" />
      <el-button icon="Download">导出记录</el-button>
    </div>

    <!-- Stats -->
    <el-row :gutter="16" style="margin-bottom: 16px">
      <el-col :span="6">
        <div class="mini-stat">
          <span class="ms-label">总违约数</span>
          <span class="ms-value danger">{{ violations.length }}</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="mini-stat">
          <span class="ms-label">涉及学生</span>
          <span class="ms-value">{{ uniqueStudents }}</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="mini-stat">
          <span class="ms-label">已封号学生</span>
          <span class="ms-value warning">0</span>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="mini-stat">
          <span class="ms-label">本月新增</span>
          <span class="ms-value">{{ violations.length }}</span>
        </div>
      </el-col>
    </el-row>

    <div class="content-card">
      <el-table :data="filteredViolations" stripe style="width:100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="学生" width="130">
          <template #default="{ row }">
            <div>{{ row.studentName }}</div>
            <div style="font-size:11px;color:#9ca3af">{{ row.studentId }}</div>
          </template>
        </el-table-column>
        <el-table-column prop="roomName" label="自习室" min-width="140" />
        <el-table-column prop="seatNo" label="座位" width="70" />
        <el-table-column prop="date" label="违约日期" width="110" />
        <el-table-column prop="time" label="时段" width="130" />
        <el-table-column prop="reason" label="原因" width="110" />
        <el-table-column label="累计违约次数" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.count >= 3 ? 'danger' : row.count >= 2 ? 'warning' : 'info'" size="small">
              {{ row.count }} 次
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="记录时间" width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewStudent(row)">查看学生</el-button>
            <el-button v-if="row.count >= 3" link type="danger" @click="suspend(row)">封号</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getViolations, suspendStudent } from '../api/violations'

const violations = ref([])
const total = ref(0)
const uniqueStudents = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const search = ref('')
const loading = ref(false)

const filteredViolations = computed(() => violations.value)

async function loadViolations() {
  loading.value = true
  try {
    const data = await getViolations({ page: currentPage.value, pageSize: pageSize.value, search: search.value })
    violations.value = data.records || []
    total.value = data.total || 0
    uniqueStudents.value = new Set(violations.value.map(v => v.studentId)).size
  } catch {
    violations.value = []
  } finally {
    loading.value = false
  }
}

watch([search], () => { currentPage.value = 1; loadViolations() })
onMounted(loadViolations)

function viewStudent(row) {
  ElMessage.info(`查看学生 ${row.studentName}（${row.studentId}）的详情`)
}

async function suspend(row) {
  try {
    await suspendStudent(row.studentId, { days: 7 })
    ElMessage.warning(`已对 ${row.studentName} 发出封号`)
    loadViolations()
  } catch {}
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}

.mini-stat {
  background: #fff;
  border-radius: 12px;
  padding: 14px 20px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ms-label {
  font-size: 13px;
  color: #6b7280;
}

.ms-value {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
}

.ms-value.danger { color: #dc2626; }
.ms-value.warning { color: #d97706; }
</style>
