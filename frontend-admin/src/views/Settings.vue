<template>
  <div>
    <div class="page-title">
      <el-icon><Setting /></el-icon> 系统参数设置
    </div>

    <el-row :gutter="20">
      <el-col :span="14">
        <div class="content-card">
          <div class="card-header">预约规则参数</div>
          <div class="card-body">
            <el-form :model="settings" label-width="180px" label-position="left">
              <el-form-item label="单次最长预约时间（小时）">
                <el-input-number v-model="settings.maxBookingHours" :min="1" :max="8" :step="1" />
                <span class="unit">小时</span>
                <div class="param-hint">学生每次预约最多可选择的小时数</div>
              </el-form-item>

              <el-divider />

              <el-form-item label="提前提醒时间">
                <el-input-number v-model="settings.reminderMinutesBefore" :min="5" :max="60" :step="5" />
                <span class="unit">分钟</span>
                <div class="param-hint">预约开始前多少分钟发送推送提醒</div>
              </el-form-item>

              <el-form-item label="签到宽限时间">
                <el-input-number v-model="settings.checkInWindowAfter" :min="5" :max="30" :step="5" />
                <span class="unit">分钟</span>
                <div class="param-hint">预约开始后多少分钟内未签到则自动取消</div>
              </el-form-item>

              <el-divider />

              <el-form-item label="封号触发违约次数">
                <el-input-number v-model="settings.violationLimit" :min="1" :max="10" :step="1" />
                <span class="unit">次</span>
                <div class="param-hint">违约达到该次数后系统自动限制预约</div>
              </el-form-item>

              <el-form-item label="封号时长">
                <el-input-number v-model="settings.suspendDays" :min="1" :max="365" :step="1" />
                <span class="unit">天</span>
                <div class="param-hint">违规封号持续天数</div>
              </el-form-item>

              <el-divider />

              <el-form-item label="允许周末预约">
                <el-switch v-model="settings.allowWeekend" />
                <span class="switch-label">{{ settings.allowWeekend ? '是' : '否' }}</span>
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="saveSettings" :loading="saving">保存设置</el-button>
                <el-button @click="resetSettings">重置为默认</el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="content-card" style="margin-bottom: 20px">
          <div class="card-header">当前参数快览</div>
          <div class="card-body">
            <div class="param-summary-row" v-for="item in paramSummary" :key="item.label">
              <span class="ps-label">{{ item.label }}</span>
              <span class="ps-value">{{ item.value }}</span>
            </div>
          </div>
        </div>

        <div class="content-card">
          <div class="card-header">修改历史（示例）</div>
          <div class="card-body">
            <div v-for="log in changeLogs" :key="log.id" class="log-item">
              <div class="log-header">
                <span class="log-user">{{ log.user }}</span>
                <span class="log-time">{{ log.time }}</span>
              </div>
              <div class="log-detail">{{ log.detail }}</div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { mockSettings } from '../mock/data'

const settings = reactive({ ...mockSettings })
const saving = ref(false)

const defaultSettings = { ...mockSettings }

const paramSummary = computed(() => [
  { label: '最长预约时间', value: `${settings.maxBookingHours} 小时` },
  { label: '提前提醒', value: `${settings.reminderMinutesBefore} 分钟前` },
  { label: '签到宽限', value: `${settings.checkInWindowAfter} 分钟` },
  { label: '封号阈值', value: `${settings.violationLimit} 次违约` },
  { label: '封号时长', value: `${settings.suspendDays} 天` },
  { label: '允许周末', value: settings.allowWeekend ? '是' : '否' },
])

const changeLogs = [
  { id: 1, user: '张管理', time: '2026-04-01 10:00', detail: '将最长预约时间从3小时改为4小时' },
  { id: 2, user: '张管理', time: '2026-03-15 14:30', detail: '将封号阈值从5次改为3次' },
  { id: 3, user: '李明', time: '2026-02-20 09:00', detail: '开启周末预约功能' },
]

async function saveSettings() {
  saving.value = true
  await new Promise(r => setTimeout(r, 800))
  saving.value = false
  ElMessage.success('系统参数已保存')
}

function resetSettings() {
  Object.assign(settings, defaultSettings)
  ElMessage.info('已重置为默认参数')
}
</script>

<style scoped>
.content-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}

.card-header {
  padding: 14px 20px;
  border-bottom: 1px solid #f3f4f6;
  font-weight: 600;
  font-size: 14px;
}

.card-body {
  padding: 24px;
}

.unit {
  margin-left: 10px;
  color: #6b7280;
  font-size: 14px;
}

.switch-label {
  margin-left: 10px;
  color: #6b7280;
  font-size: 14px;
}

.param-hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 4px;
}

.param-summary-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f9fafb;
  font-size: 14px;
}

.param-summary-row:last-child {
  border-bottom: none;
}

.ps-label {
  color: #6b7280;
}

.ps-value {
  font-weight: 600;
  color: #1f2937;
}

.log-item {
  padding: 10px 0;
  border-bottom: 1px solid #f9fafb;
}

.log-item:last-child {
  border-bottom: none;
}

.log-header {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 4px;
}

.log-user {
  font-weight: 600;
  color: #374151;
}

.log-time {
  color: #9ca3af;
}

.log-detail {
  font-size: 12px;
  color: #6b7280;
}
</style>
