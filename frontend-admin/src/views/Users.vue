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
import { ref, computed, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStudents, updateStudentStatus, getAdmins, createAdmin, updateAdmin as updateAdminApi, deleteAdmin as deleteAdminApi } from '../api/users'
import { getRoles } from '../api/roles'

const activeTab = ref('students')
const students = ref([])
const stuTotal = ref(0)
const stuPage = ref(1)
const admins = ref([])
const adminTotal = ref(0)
const adminPage = ref(1)
const roles = ref([])

const stuSearch = ref('')
const stuStatusFilter = ref('')
const adminSearch = ref('')
const adminDialog = ref(false)
const editingAdmin = ref(null)

const adminForm = reactive({ name: '', email: '', password: '', roleId: null })

const filteredStudents = computed(() => students.value)
const filteredAdmins = computed(() => admins.value)

async function loadStudents() {
  try {
    const data = await getStudents({ page: stuPage.value, pageSize: 10, search: stuSearch.value, status: stuStatusFilter.value })
    students.value = data.records || []
    stuTotal.value = data.total || 0
  } catch { students.value = [] }
}

async function loadAdmins() {
  try {
    const data = await getAdmins({ page: adminPage.value, pageSize: 10, search: adminSearch.value })
    admins.value = data.records || []
    adminTotal.value = data.total || 0
  } catch { admins.value = [] }
}

async function loadRoles() {
  try { roles.value = (await getRoles()) || [] } catch {}
}

watch([stuSearch, stuStatusFilter], () => { stuPage.value = 1; loadStudents() })
watch([adminSearch], () => { adminPage.value = 1; loadAdmins() })

onMounted(() => { loadStudents(); loadAdmins(); loadRoles() })

async function toggleStudentStatus(s) {
  const next = s.status === 'active' ? 'suspended' : 'active'
  try {
    await updateStudentStatus(s.id, next)
    s.status = next
    ElMessage.success(next === 'active' ? '已解封账号' : '已封号')
  } catch {}
}

function viewViolations(s) {
  ElMessage.info(`查看 ${s.name} 的违约记录（跳转违约页面）`)
}

function openAdminDialog(admin = null) {
  editingAdmin.value = admin
  Object.assign(adminForm, admin
    ? { name: admin.name, email: admin.email, password: '', roleId: admin.roleId }
    : { name: '', email: '', password: '', roleId: null })
  adminDialog.value = true
}

async function saveAdmin() {
  if (!adminForm.name || !adminForm.email) { ElMessage.warning('请填写完整信息'); return }
  try {
    if (editingAdmin.value) {
      await updateAdminApi(editingAdmin.value.id, { name: adminForm.name, email: adminForm.email, roleId: adminForm.roleId })
      ElMessage.success('管理员信息已更新')
    } else {
      await createAdmin({ name: adminForm.name, email: adminForm.email, password: adminForm.password, roleId: adminForm.roleId })
      ElMessage.success('管理员已创建')
    }
    adminDialog.value = false
    loadAdmins()
  } catch {}
}

async function changeRole(admin) {
  ElMessage.info('请在编辑对话框中修改角色')
}

async function deleteAdmin(admin) {
  try {
    await ElMessageBox.confirm(`确定删除管理员 ${admin.name} 吗？`, '删除确认', { type: 'warning' })
    await deleteAdminApi(admin.id)
    ElMessage.success('已删除')
    loadAdmins()
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
