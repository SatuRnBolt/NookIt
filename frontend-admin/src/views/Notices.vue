<template>
  <div class="notices-page">
    <div class="page-header">
      <div class="page-title">
        <el-icon><Bell /></el-icon> 通知公告
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">发布公告</el-button>
    </div>

    <div class="split-layout">
      <!-- 左侧：搜索 + 类型标签 + 列表 -->
      <div class="list-panel">
        <div class="list-toolbar">
          <el-input v-model="search" placeholder="搜索公告标题或内容" :prefix-icon="Search" clearable />
          <div class="type-tabs">
            <div class="tab-item" :class="{ active: !filterType }" @click="filterType = ''">
              <span>全部</span>
              <span class="tab-count">{{ notices.length }}</span>
            </div>
            <div
              v-for="t in typeOptions"
              :key="t.value"
              class="tab-item"
              :class="{ active: filterType === t.value }"
              @click="filterType = t.value"
            >
              <span class="type-dot" :class="`dot-${t.value}`"></span>
              <span>{{ t.label }}</span>
              <span class="tab-count">{{ countByType(t.value) }}</span>
            </div>
          </div>
        </div>

        <div class="list-scroll">
          <div v-if="!filteredNotices.length" class="empty-list">
            <el-empty description="暂无公告" :image-size="70" />
          </div>
          <div
            v-for="n in filteredNotices"
            :key="n.id"
            class="list-item"
            :class="{ active: selectedId === n.id, draft: n.status === 'draft' }"
            @click="selectNotice(n)"
          >
            <div class="item-marker" :class="`dot-${n.type}`"></div>
            <div class="item-body">
              <div class="item-row">
                <span class="item-title">{{ n.title }}</span>
                <el-icon v-if="n.pinned" class="item-pin" color="#f59e0b"><StarFilled /></el-icon>
              </div>
              <div class="item-summary">{{ n.content }}</div>
              <div class="item-meta">
                <el-tag size="small" :type="typeTagType(n.type)" effect="plain">{{ typeLabel(n.type) }}</el-tag>
                <el-tag v-if="n.status === 'draft'" size="small" type="info" effect="plain">草稿</el-tag>
                <span class="item-time">{{ n.publishedAt || n.createdAt }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：文章式详情 -->
      <div class="detail-panel">
        <div v-if="!selected" class="detail-empty">
          <el-icon size="56" color="#d1d5db"><Document /></el-icon>
          <div class="empty-text">从左侧选择公告查看详情</div>
          <div class="empty-hint">或点击右上角"发布公告"创建新公告</div>
        </div>

        <template v-else>
          <div class="detail-header">
            <div class="detail-tags">
              <el-tag :type="typeTagType(selected.type)" size="small">{{ typeLabel(selected.type) }}</el-tag>
              <el-tag :type="selected.status === 'published' ? 'success' : 'info'" size="small">
                {{ selected.status === 'published' ? '已发布' : '草稿' }}
              </el-tag>
              <el-tag v-if="selected.pinned" type="warning" size="small">
                <el-icon style="vertical-align:-2px"><StarFilled /></el-icon> 置顶
              </el-tag>
            </div>
            <div class="detail-actions">
              <el-button link type="primary" @click="openEdit(selected)">
                <el-icon><Edit /></el-icon><span>编辑</span>
              </el-button>
              <el-button v-if="selected.status === 'draft'" link type="success" @click="publish(selected)">
                <el-icon><Promotion /></el-icon><span>发布</span>
              </el-button>
              <el-button link type="danger" @click="deleteNotice(selected)">
                <el-icon><Delete /></el-icon><span>删除</span>
              </el-button>
            </div>
          </div>

          <div class="detail-body">
            <h1 class="detail-title">{{ selected.title }}</h1>
            <div class="detail-meta">
              <span><el-icon><User /></el-icon>{{ selected.author }}</span>
              <span class="meta-sep">·</span>
              <span>
                <el-icon><Clock /></el-icon>
                {{ selected.publishedAt || `创建于 ${selected.createdAt}` }}
              </span>
            </div>
            <div class="detail-divider"></div>
            <div class="detail-content">{{ selected.content }}</div>
          </div>
        </template>
      </div>
    </div>

    <!-- create/edit dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form :model="form" label-width="80px" label-position="left">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="请输入公告标题" maxlength="60" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="form.type" style="width:140px">
            <el-option v-for="t in typeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="form.pinned" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="请输入公告内容"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button @click="saveNotice('draft')">存为草稿</el-button>
        <el-button type="primary" @click="saveNotice('published')">立即发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell, Plus, Search, StarFilled, Document,
  Edit, Delete, Promotion, User, Clock,
} from '@element-plus/icons-vue'
import { mockNotices } from '../mock/data'

const notices = ref([...mockNotices])
const search = ref('')
const filterType = ref('')
const selectedId = ref(null)

const dialogVisible = ref(false)
const dialogMode = ref('create')
const form = reactive({ title: '', type: 'system', pinned: false, content: '' })

const typeOptions = [
  { value: 'system', label: '系统通知' },
  { value: 'rule', label: '规则说明' },
  { value: 'event', label: '活动公告' },
  { value: 'maintenance', label: '维护通知' },
]

const filteredNotices = computed(() => {
  const list = notices.value.filter(n => {
    if (search.value) {
      const q = search.value.toLowerCase()
      if (!n.title.toLowerCase().includes(q) && !n.content.toLowerCase().includes(q)) return false
    }
    if (filterType.value && n.type !== filterType.value) return false
    return true
  })
  return list.sort((a, b) => {
    if (a.pinned !== b.pinned) return a.pinned ? -1 : 1
    return (b.publishedAt || b.createdAt).localeCompare(a.publishedAt || a.createdAt)
  })
})

const selected = computed(() => notices.value.find(n => n.id === selectedId.value))
const dialogTitle = computed(() => dialogMode.value === 'edit' ? '编辑公告' : '发布公告')

function countByType(type) {
  return notices.value.filter(n => n.type === type).length
}

function typeLabel(t) {
  return typeOptions.find(x => x.value === t)?.label || t
}

function typeTagType(t) {
  return { system: 'primary', rule: 'warning', event: 'success', maintenance: 'danger' }[t] || ''
}

function selectNotice(n) {
  selectedId.value = n.id
}

function openCreate() {
  Object.assign(form, { title: '', type: 'system', pinned: false, content: '' })
  dialogMode.value = 'create'
  dialogVisible.value = true
}

function openEdit(n) {
  Object.assign(form, { title: n.title, type: n.type, pinned: n.pinned, content: n.content })
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

function saveNotice(status) {
  if (!form.title.trim()) { ElMessage.warning('请填写公告标题'); return }
  if (!form.content.trim()) { ElMessage.warning('请填写公告内容'); return }
  const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')

  if (dialogMode.value === 'create') {
    const newNotice = {
      id: Date.now(),
      title: form.title,
      content: form.content,
      type: form.type,
      pinned: form.pinned,
      status,
      author: '张管理',
      publishedAt: status === 'published' ? now : null,
      createdAt: now,
    }
    notices.value.unshift(newNotice)
    selectedId.value = newNotice.id
  } else {
    const idx = notices.value.findIndex(n => n.id === selectedId.value)
    if (idx !== -1) {
      Object.assign(notices.value[idx], {
        title: form.title,
        content: form.content,
        type: form.type,
        pinned: form.pinned,
        status,
        publishedAt: status === 'published' ? (notices.value[idx].publishedAt || now) : null,
      })
    }
  }
  ElMessage.success(status === 'published' ? '公告已发布' : '已保存为草稿')
  dialogVisible.value = false
}

function publish(n) {
  const idx = notices.value.findIndex(x => x.id === n.id)
  if (idx !== -1) {
    const now = new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-')
    notices.value[idx].status = 'published'
    notices.value[idx].publishedAt = now
    ElMessage.success('公告已发布')
  }
}

function deleteNotice(n) {
  ElMessageBox.confirm(`确定删除公告「${n.title}」吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    confirmButtonClass: 'el-button--danger',
  }).then(() => {
    notices.value = notices.value.filter(x => x.id !== n.id)
    if (selectedId.value === n.id) selectedId.value = null
    ElMessage.success('已删除')
  }).catch(() => {})
}

if (notices.value.length) selectedId.value = filteredNotices.value[0]?.id ?? null
</script>

<style scoped>
.notices-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 110px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.split-layout {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
}

/* 左侧列表面板 */
.list-panel {
  width: 360px;
  flex-shrink: 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-toolbar {
  padding: 14px 14px 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.type-tabs {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 14px;
  font-size: 12px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.18s;
  background: #f9fafb;
}

.tab-item:hover {
  background: #eff6ff;
  color: #003893;
}

.tab-item.active {
  background: #003893;
  color: #fff;
}

.tab-item.active .tab-count {
  background: rgba(255,255,255,0.25);
  color: #fff;
}

.tab-count {
  background: #e5e7eb;
  color: #6b7280;
  padding: 0 6px;
  border-radius: 8px;
  font-size: 11px;
  min-width: 18px;
  text-align: center;
}

.type-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.dot-system { background: #3b82f6; }
.dot-rule { background: #f59e0b; }
.dot-event { background: #10b981; }
.dot-maintenance { background: #ef4444; }

.list-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0;
}

.list-item {
  display: flex;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: background 0.15s;
  position: relative;
}

.list-item:hover {
  background: #f9fafb;
}

.list-item.active {
  background: #eff6ff;
}

.list-item.active::before {
  content: '';
  position: absolute;
  left: 0; top: 0; bottom: 0;
  width: 3px;
  background: #003893;
}

.item-marker {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-top: 7px;
  flex-shrink: 0;
}

.item-body {
  flex: 1;
  min-width: 0;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 4px;
}

.item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-pin {
  flex-shrink: 0;
}

.item-summary {
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 6px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.item-time {
  font-size: 11px;
  color: #9ca3af;
  margin-left: auto;
}

.empty-list {
  padding: 40px 0;
}

/* 右侧详情面板 */
.detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.empty-text {
  font-size: 15px;
  color: #6b7280;
  margin-top: 8px;
}

.empty-hint {
  font-size: 12px;
  color: #9ca3af;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
}

.detail-tags {
  display: flex;
  gap: 8px;
  align-items: center;
}

.detail-actions {
  display: flex;
  gap: 4px;
}

.detail-actions :deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 28px 36px 36px;
}

.detail-title {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0 0 12px;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 13px;
  color: #6b7280;
}

.detail-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.meta-sep {
  color: #d1d5db;
}

.detail-divider {
  height: 1px;
  background: #f1f5f9;
  margin: 18px 0 24px;
}

.detail-content {
  font-size: 15px;
  color: #374151;
  line-height: 1.85;
  white-space: pre-wrap;
}
</style>
