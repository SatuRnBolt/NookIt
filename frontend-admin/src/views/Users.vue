<template>
  <div>
    <div class="page-title">
      <el-icon><User /></el-icon> 用户管理
    </div>

    <el-tabs v-model="activeTab">
      <!-- Student users -->
      <el-tab-pane label="学生用户" name="students">
        <div class="toolbar">
          <el-input v-model="stuSearch" placeholder="搜索姓名/学号" prefix-icon="Search" clearable style="width:200px" />
          <el-select v-model="stuStatusFilter" placeholder="账号状态" clearable style="width:120px">
            <el-option label="正常" value="active" />
            <el-option label="已封号" value="suspended" />
          </el-select>
        </div>

        <div class="content-card">
          <el-table :data="filteredStudents" stripe style="width:100%">
            <el-table-column prop="studentId" label="学号" width="120" />
            <el-table-column prop="name" label="姓名" width="90" />
            <el-table-column prop="department" label="学院" min-width="120" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column label="违约次数" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.violationCount >= 3 ? 'danger' : row.violationCount >= 1 ? 'warning' : 'success'" size="small">
                  {{ row.violationCount }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="账号状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'danger'" size="small">
                  {{ row.status === 'active' ? '正常' : '已封号' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="注册日期" width="110" />
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewViolations(row)">违约记录</el-button>
                <el-button link :type="row.status === 'active' ? 'danger' : 'success'" @click="toggleStudentStatus(row)">
                  {{ row.status === 'active' ? '封号' : '解封' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- Admin users -->
      <el-tab-pane label="管理员账号" name="admins">
        <div class="toolbar">
          <el-input v-model="adminSearch" placeholder="搜索姓名/邮箱" prefix-icon="Search" clearable style="width:200px" />
          <el-button type="primary" icon="Plus" @click="openAdminDialog()">新增管理员</el-button>
        </div>

        <div class="content-card">
          <el-table :data="filteredAdmins" stripe style="width:100%">
            <el-table-column prop="name" label="姓名" width="100" />
            <el-table-column prop="email" label="邮箱" min-width="180" />
            <el-table-column label="角色" width="130">
              <template #default="{ row }">
                <el-tag size="small">{{ row.roleName }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
                  {{ row.status === 'active' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastLogin" label="最后登录" width="160" />
            <el-table-column label="操作" width="170" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="openAdminDialog(row)">编辑</el-button>
                <el-button link type="warning" @click="changeRole(row)">分配角色</el-button>
                <el-button link type="danger" @click="deleteAdmin(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- Admin user dialog -->
    <el-dialog v-model="adminDialog" :title="editingAdmin ? '编辑管理员' : '新增管理员'" width="440px">
      <el-form :model="adminForm" label-width="80px">
        <el-form-item label="姓名"><el-input v-model="adminForm.name" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="adminForm.email" /></el-form-item>
        <el-form-item label="密码" v-if="!editingAdmin">
          <el-input v-model="adminForm.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="adminForm.role">
            <el-option v-for="r in roles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adminDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAdmin">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockStudentUsers, mockAdminUsers, mockRoles } from '../mock/data'

const activeTab = ref('students')
const students = ref([...mockStudentUsers])
const admins = ref([...mockAdminUsers])
const roles = mockRoles

const stuSearch = ref('')
const stuStatusFilter = ref('')
const adminSearch = ref('')
const adminDialog = ref(false)
const editingAdmin = ref(null)

const adminForm = reactive({ name: '', email: '', password: '', role: 1 })

const filteredStudents = computed(() => students.value.filter(s => {
  if (stuSearch.value && !s.name.includes(stuSearch.value) && !s.studentId.includes(stuSearch.value)) return false
  if (stuStatusFilter.value && s.status !== stuStatusFilter.value) return false
  return true
}))

const filteredAdmins = computed(() => admins.value.filter(a =>
  !adminSearch.value || a.name.includes(adminSearch.value) || a.email.includes(adminSearch.value)
))

function toggleStudentStatus(s) {
  s.status = s.status === 'active' ? 'suspended' : 'active'
  ElMessage.success(s.status === 'active' ? '已解封账号' : '已封号')
}

function viewViolations(s) {
  ElMessage.info(`查看 ${s.name} 的违约记录（跳转违约页面）`)
}

function openAdminDialog(admin = null) {
  editingAdmin.value = admin
  if (admin) {
    Object.assign(adminForm, admin)
  } else {
    Object.assign(adminForm, { name: '', email: '', password: '', role: 1 })
  }
  adminDialog.value = true
}

function saveAdmin() {
  if (!adminForm.name || !adminForm.email) {
    ElMessage.warning('请填写完整信息')
    return
  }
  const role = roles.find(r => r.id === adminForm.role)
  if (editingAdmin.value) {
    Object.assign(editingAdmin.value, { ...adminForm, roleName: role?.name })
    ElMessage.success('管理员信息已更新')
  } else {
    admins.value.push({
      id: Date.now(), ...adminForm,
      roleName: role?.name || '', status: 'active', lastLogin: '-', createdAt: new Date().toISOString().slice(0,10)
    })
    ElMessage.success('管理员已创建')
  }
  adminDialog.value = false
}

async function changeRole(admin) {
  // Simple inline role change via prompt-like dialog
  ElMessage.info('功能：在角色管理页配置角色后，可在此处分配')
}

async function deleteAdmin(admin) {
  try {
    await ElMessageBox.confirm(`确定删除管理员 ${admin.name} 吗？`, '删除确认', { type: 'warning' })
    admins.value = admins.value.filter(a => a.id !== admin.id)
    ElMessage.success('已删除')
  } catch {}
}
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
  flex-wrap: wrap;
}

.content-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 8px rgba(0,0,0,0.06);
}
</style>
