<template>
  <div>
    <div class="page-title">
      <el-icon><Lock /></el-icon> 角色与权限管理
    </div>

    <el-row :gutter="20">
      <!-- Role list -->
      <el-col :span="8">
        <div class="content-card" style="height: calc(100vh - 160px); overflow: hidden; display: flex; flex-direction: column">
          <div class="card-header">
            <span>角色列表</span>
            <el-button type="primary" size="small" icon="Plus" @click="openRoleDialog()">新增</el-button>
          </div>
          <div style="overflow-y: auto; flex: 1">
            <div
              v-for="role in roles"
              :key="role.id"
              class="role-item"
              :class="{ active: selectedRoleId === role.id }"
              @click="selectedRoleId = role.id"
            >
              <div class="role-item-header">
                <span class="role-name">{{ role.name }}</span>
                <span class="role-count">{{ role.userCount }} 人</span>
              </div>
              <div class="role-desc">{{ role.description }}</div>
              <div class="role-actions" @click.stop>
                <el-button link size="small" @click="openRoleDialog(role)">编辑</el-button>
                <el-button link size="small" type="danger" @click="deleteRole(role)" :disabled="role.id === 1">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- Permission matrix -->
      <el-col :span="16">
        <div class="content-card" style="height: calc(100vh - 160px); overflow: hidden; display: flex; flex-direction: column">
          <div class="card-header">
            <span>
              权限配置
              <span v-if="selectedRole" style="color:#4f6ef7;margin-left:8px">— {{ selectedRole.name }}</span>
            </span>
            <el-button v-if="selectedRole" type="primary" size="small" @click="savePermissions">保存配置</el-button>
          </div>

          <div v-if="!selectedRole" class="empty-perm">
            <el-empty description="请选择左侧角色查看权限配置" />
          </div>

          <div v-else style="overflow-y: auto; flex: 1; padding: 20px">
            <div class="perm-tip">
              <el-icon><InfoFilled /></el-icon>
              已选 {{ editingPerms.length }} / {{ allPermissions.length }} 个权限
            </div>
            <div class="perm-grid">
              <div
                v-for="perm in allPermissions"
                :key="perm.key"
                class="perm-item"
                :class="{ enabled: editingPerms.includes(perm.key), disabled: selectedRole.id === 1 }"
                @click="selectedRole.id !== 1 && togglePerm(perm.key)"
              >
                <div class="perm-check">
                  <el-icon v-if="editingPerms.includes(perm.key)" color="#4f6ef7" :size="20">
                    <SuccessFilled />
                  </el-icon>
                  <div v-else class="perm-empty-check"></div>
                </div>
                <div class="perm-label">{{ perm.label }}</div>
              </div>
            </div>

            <div v-if="selectedRole.id === 1" class="super-notice">
              <el-icon><Warning /></el-icon>
              超级管理员拥有全部权限，不可修改
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- Role dialog -->
    <el-dialog v-model="roleDialog" :title="editingRole ? '编辑角色' : '新增角色'" width="400px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色名"><el-input v-model="roleForm.name" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="roleForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockRoles, PERMISSIONS } from '../mock/data'

const roles = ref(JSON.parse(JSON.stringify(mockRoles)))
const allPermissions = PERMISSIONS
const selectedRoleId = ref(null)
const roleDialog = ref(false)
const editingRole = ref(null)
const editingPerms = ref([])

const roleForm = reactive({ name: '', description: '' })

const selectedRole = computed(() => roles.value.find(r => r.id === selectedRoleId.value))

watch(selectedRole, (role) => {
  editingPerms.value = role ? [...role.permissions] : []
}, { immediate: true })

function togglePerm(key) {
  const i = editingPerms.value.indexOf(key)
  if (i >= 0) {
    editingPerms.value.splice(i, 1)
  } else {
    editingPerms.value.push(key)
  }
}

function savePermissions() {
  if (selectedRole.value) {
    selectedRole.value.permissions = [...editingPerms.value]
    ElMessage.success('权限配置已保存')
  }
}

function openRoleDialog(role = null) {
  editingRole.value = role
  if (role) {
    Object.assign(roleForm, role)
  } else {
    Object.assign(roleForm, { name: '', description: '' })
  }
  roleDialog.value = true
}

function saveRole() {
  if (!roleForm.name) {
    ElMessage.warning('请填写角色名称')
    return
  }
  if (editingRole.value) {
    Object.assign(editingRole.value, { name: roleForm.name, description: roleForm.description })
    ElMessage.success('角色已更新')
  } else {
    roles.value.push({
      id: Date.now(), name: roleForm.name, description: roleForm.description,
      permissions: [], userCount: 0
    })
    ElMessage.success('角色已创建')
  }
  roleDialog.value = false
}

async function deleteRole(role) {
  if (role.userCount > 0) {
    ElMessage.warning('该角色下还有用户，无法删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除角色"${role.name}"吗？`, '删除确认', { type: 'warning' })
    roles.value = roles.value.filter(r => r.id !== role.id)
    if (selectedRoleId.value === role.id) selectedRoleId.value = null
    ElMessage.success('角色已删除')
  } catch {}
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.role-item {
  padding: 14px 16px;
  border-bottom: 1px solid #f9fafb;
  cursor: pointer;
  transition: background 0.2s;
}

.role-item:hover, .role-item.active {
  background: #f0f4ff;
}

.role-item.active {
  border-left: 3px solid #4f6ef7;
}

.role-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.role-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2937;
}

.role-count {
  font-size: 12px;
  color: #6b7280;
}

.role-desc {
  font-size: 12px;
  color: #9ca3af;
  margin-bottom: 8px;
}

.role-actions {
  display: flex;
  gap: 4px;
}

.empty-perm {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.perm-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #6b7280;
  margin-bottom: 16px;
}

.perm-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.perm-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.perm-item:hover:not(.disabled) {
  border-color: #4f6ef7;
  background: #f0f4ff;
}

.perm-item.enabled {
  border-color: #4f6ef7;
  background: #e8ecff;
}

.perm-item.disabled {
  cursor: default;
  opacity: 0.85;
}

.perm-empty-check {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d1d5db;
}

.perm-label {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.super-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  font-size: 13px;
  color: #d97706;
  background: #fffbeb;
  padding: 10px 14px;
  border-radius: 8px;
}
</style>
