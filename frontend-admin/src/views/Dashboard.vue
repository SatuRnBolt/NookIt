<template>
  <div class="dashboard">
    <!-- 1. 极简标题 + 运营摘要 -->
    <div class="page-title">
      <el-icon><Odometer /></el-icon> 数据看板
    </div>
    <div class="page-subtitle">
      <el-icon><InfoFilled /></el-icon>
      <span>
        {{ todayStr }} · {{ weekday }}　|
        本周预约较上周 <strong class="up">↑ 12%</strong>，
        今日实时入座率 <strong>{{ dashboard.occupancyRate }}%</strong>，
        高于近 7 日平均水平。
      </span>
    </div>

    <!-- 2. 中间：周预约趋势 + 自习室占用率（保留） -->
    <el-row :gutter="16" style="margin-bottom: 20px">
      <el-col :span="14">
        <div class="content-card">
          <div class="card-header">
            <span><el-icon><TrendCharts /></el-icon> 本周预约趋势</span>
            <span class="card-extra">较上周 <strong class="up">↑ 12%</strong></span>
          </div>
          <div class="card-body">
            <div class="mini-chart">
              <div v-for="(val, i) in dashboard.weeklyTrend" :key="i" class="bar-col">
                <div class="bar-value">{{ val }}</div>
                <div
                  class="bar"
                  :style="{
                    height: (val / maxTrend * 120) + 'px',
                    background: i === 6 ? '#003893' : '#c7d2fe',
                  }"
                ></div>
                <div class="bar-label">{{ weekLabels[i] }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="content-card">
          <div class="card-header"><span><el-icon><OfficeBuilding /></el-icon> 自习室占用率</span></div>
          <div class="card-body">
            <div v-for="room in dashboard.topRooms" :key="room.name" class="room-occ-row">
              <span class="occ-name">{{ room.name }}</span>
              <div class="occ-bar-wrap">
                <div class="occ-bar" :style="{ width: room.rate + '%' }"></div>
              </div>
              <span class="occ-rate">{{ room.rate }}%</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 3. 底部：一周时段热力图 + 待办中心 -->
    <el-row :gutter="16">
      <el-col :span="16">
        <div class="content-card">
          <div class="card-header">
            <span><el-icon><Histogram /></el-icon> 一周时段热度（近 7 天每小时预约量）</span>
            <div class="legend">
              <span class="legend-text">低</span>
              <span class="legend-cell" v-for="c in legendColors" :key="c" :style="{ background: c }"></span>
              <span class="legend-text">高</span>
            </div>
          </div>
          <div class="heatmap-body">
            <div class="heatmap-hours">
              <span class="hm-corner"></span>
              <span v-for="h in 24" :key="h" class="hm-hour">
                {{ (h - 1) % 3 === 0 ? (h - 1) : '' }}
              </span>
            </div>
            <div v-for="(row, i) in heatmap" :key="i" class="heatmap-row">
              <span class="hm-day">{{ weekLabels[i] }}</span>
              <div
                v-for="(val, j) in row"
                :key="j"
                class="hm-cell"
                :style="{ background: heatColor(val) }"
                :title="`${weekLabels[i]} ${j}:00 — 预约 ${val} 次`"
              ></div>
            </div>
          </div>
          <div class="heatmap-insight">
            <el-icon><InfoFilled /></el-icon>
            <span>预约高峰集中在 <strong>19:00-21:00</strong>，建议在该时段保持全部自习室开放</span>
          </div>
        </div>
      </el-col>

      <el-col :span="8">
        <div class="content-card">
          <div class="card-header">
            <span><el-icon><Bell /></el-icon> 待办中心</span>
            <el-tag size="small" type="warning" effect="plain">{{ totalTodos }} 项待处理</el-tag>
          </div>
          <div class="todo-list">
            <div
              v-for="t in todoList"
              :key="t.label"
              class="todo-item"
              :class="[`level-${t.level}`, { dim: t.count === 0 }]"
              @click="$router.push(t.to)"
            >
              <div class="todo-icon">
                <el-icon><component :is="t.icon" /></el-icon>
              </div>
              <div class="todo-info">
                <div class="todo-label">{{ t.label }}</div>
                <div class="todo-desc">{{ t.desc }}</div>
              </div>
              <div class="todo-count">{{ t.count }}</div>
              <el-icon class="todo-arrow"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  Odometer, TrendCharts, OfficeBuilding, Histogram,
  InfoFilled, Bell, ArrowRight, ChatDotRound, EditPen,
  Warning, CircleClose,
} from '@element-plus/icons-vue'
import {
  mockDashboard, mockFeedback, mockNotices, mockViolations, mockBookings,
} from '../mock/data'

const weekLabels = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const dashboard = mockDashboard
const maxTrend = Math.max(...dashboard.weeklyTrend)

const today = new Date()
const todayStr = `${today.getFullYear()} 年 ${today.getMonth() + 1} 月 ${today.getDate()} 日`
const weekday = weekLabels[(today.getDay() + 6) % 7]

// 一周 × 24 小时热力数据（模拟典型校园自习高峰）
const heatmap = [
  [0,0,0,0,0,0,0, 5,15,30,45,50,35,30,50,55,50,40,60,75,80,70,35,10],
  [0,0,0,0,0,0,0, 8,20,35,50,55,40,35,55,60,55,45,65,78,85,72,38,12],
  [0,0,0,0,0,0,0, 5,18,32,48,52,38,32,52,58,52,42,62,76,82,70,36,11],
  [0,0,0,0,0,0,0,10,22,38,52,58,42,38,58,62,58,48,68,80,88,75,40,14],
  [0,0,0,0,0,0,0, 7,18,30,42,48,35,30,48,52,48,38,55,70,75,60,30,10],
  [0,0,0,0,0,0,0, 5,15,25,35,40,30,28,40,45,40,35,45,55,60,45,25, 8],
  [0,0,0,0,0,0,0, 3,12,22,32,38,28,25,35,42,38,32,40,50,55,40,22, 6],
]

const legendColors = ['#f1f5f9', '#dbeafe', '#93c5fd', '#3b82f6', '#1e3a8a']

function heatColor(val) {
  if (val === 0) return '#f8fafc'
  if (val < 15) return '#dbeafe'
  if (val < 30) return '#93c5fd'
  if (val < 50) return '#60a5fa'
  if (val < 70) return '#3b82f6'
  return '#1e3a8a'
}

const todoList = computed(() => {
  const pendingFeedback = mockFeedback.filter(f => f.status === 'pending').length
  const processingFeedback = mockFeedback.filter(f => f.status === 'processing').length
  const draftNotices = mockNotices.filter(n => n.status === 'draft').length
  const heavyViolations = mockViolations.filter(v => v.count >= 2).length
  const missedBookings = mockBookings.filter(b => b.status === 'missed').length

  return [
    {
      icon: ChatDotRound,
      label: '待回复反馈',
      desc: '学生新提交的问题',
      count: pendingFeedback + processingFeedback,
      level: pendingFeedback > 0 ? 'danger' : 'info',
      to: '/feedback',
    },
    {
      icon: EditPen,
      label: '草稿公告',
      desc: '已创建尚未发布',
      count: draftNotices,
      level: 'warning',
      to: '/notices',
    },
    {
      icon: Warning,
      label: '严重违约学生',
      desc: '违约 ≥ 2 次需关注',
      count: heavyViolations,
      level: heavyViolations > 0 ? 'danger' : 'info',
      to: '/violations',
    },
    {
      icon: CircleClose,
      label: '今日爽约预约',
      desc: '未按时签到的预约',
      count: missedBookings,
      level: 'warning',
      to: '/bookings',
    },
  ]
})

const totalTodos = computed(() => todoList.value.reduce((s, t) => s + t.count, 0))
</script>

<style scoped>
.dashboard {
  padding-bottom: 20px;
}

.page-subtitle {
  margin: 4px 2px 18px;
  font-size: 13px;
  color: #6b7280;
  display: flex;
  align-items: center;
  gap: 6px;
}

.page-subtitle strong {
  color: #1f2937;
  font-weight: 600;
}

.page-subtitle .up {
  color: #059669;
}

/* === 中间卡片 === */
.content-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  overflow: hidden;
  height: 100%;
}

.card-header {
  padding: 14px 20px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 14px;
}

.card-header > span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.card-extra {
  font-weight: 400;
  font-size: 12px;
  color: #6b7280;
}

.card-extra .up { color: #059669; font-weight: 600; }

.card-body { padding: 20px; }

.mini-chart {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  height: 160px;
  padding-bottom: 30px;
  position: relative;
}

.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
  height: 100%;
}

.bar {
  width: 100%;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s;
}

.bar-value {
  font-size: 11px;
  font-weight: 600;
  color: #003893;
}

.bar-label {
  font-size: 11px;
  color: #9ca3af;
  position: absolute;
  bottom: 0;
}

.room-occ-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 13px;
}

.occ-name {
  width: 110px;
  flex-shrink: 0;
  color: #374151;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.occ-bar-wrap {
  flex: 1;
  height: 8px;
  background: #f3f4f6;
  border-radius: 4px;
  overflow: hidden;
}

.occ-bar {
  height: 100%;
  background: linear-gradient(90deg, #003893, #3b82f6);
  border-radius: 4px;
  transition: width 0.5s;
}

.occ-rate {
  width: 36px;
  text-align: right;
  font-size: 12px;
  font-weight: 600;
  color: #003893;
}

/* === 热力图 === */
.legend {
  display: flex;
  align-items: center;
  gap: 4px;
}

.legend-text {
  font-size: 11px;
  color: #9ca3af;
  font-weight: 400;
}

.legend-cell {
  width: 14px;
  height: 14px;
  border-radius: 3px;
}

.heatmap-body {
  padding: 16px 20px 8px;
}

.heatmap-hours {
  display: grid;
  grid-template-columns: 36px repeat(24, 1fr);
  gap: 3px;
  margin-bottom: 4px;
}

.hm-corner { width: 36px; }

.hm-hour {
  font-size: 10px;
  color: #9ca3af;
  text-align: center;
  height: 14px;
  line-height: 14px;
}

.heatmap-row {
  display: grid;
  grid-template-columns: 36px repeat(24, 1fr);
  gap: 3px;
  margin-bottom: 3px;
}

.hm-day {
  font-size: 11px;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 6px;
}

.hm-cell {
  height: 22px;
  border-radius: 3px;
  cursor: default;
  transition: transform 0.12s;
}

.hm-cell:hover {
  transform: scale(1.15);
  box-shadow: 0 0 0 1.5px #003893;
  z-index: 1;
}

.heatmap-insight {
  margin: 12px 20px 16px;
  padding: 10px 14px;
  background: #f0f9ff;
  border-left: 3px solid #003893;
  border-radius: 0 6px 6px 0;
  font-size: 12px;
  color: #374151;
  display: flex;
  align-items: center;
  gap: 6px;
}

.heatmap-insight strong {
  color: #003893;
  font-weight: 600;
}

/* === 待办中心 === */
.todo-list {
  padding: 8px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
  margin-bottom: 4px;
  border: 1px solid transparent;
}

.todo-item:hover {
  background: #f9fafb;
  border-color: #e5e7eb;
}

.todo-item.dim {
  opacity: 0.55;
}

.todo-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.level-info .todo-icon { background: #f1f5f9; color: #64748b; }
.level-warning .todo-icon { background: #fef3c7; color: #d97706; }
.level-danger .todo-icon { background: #fee2e2; color: #dc2626; }

.todo-info {
  flex: 1;
  min-width: 0;
}

.todo-label {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 2px;
}

.todo-desc {
  font-size: 12px;
  color: #9ca3af;
}

.todo-count {
  font-size: 22px;
  font-weight: 700;
  color: #1f2937;
  font-feature-settings: 'tnum';
}

.level-warning .todo-count { color: #d97706; }
.level-danger .todo-count { color: #dc2626; }

.todo-arrow {
  color: #d1d5db;
  font-size: 14px;
}

.todo-item:hover .todo-arrow {
  color: #003893;
  transform: translateX(2px);
}
</style>
