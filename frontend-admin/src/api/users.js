import { get, post, put, patch, del } from './request'

// 学生用户
export function getStudents(query = {}) {
  return get('/api/admin/users/students', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    search: query.search || '',
    status: query.status || '',
  })
}

export function getStudent(id) {
  return get(`/api/admin/users/students/${id}`)
}

export function updateStudentStatus(id, status) {
  return patch(`/api/admin/users/students/${id}/status`, { status })
}

// 管理员账号
export function getAdmins(query = {}) {
  return get('/api/admin/users/admins', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    search: query.search || '',
  })
}

export function createAdmin(data) {
  return post('/api/admin/users/admins', data)
}

export function updateAdmin(id, data) {
  return put(`/api/admin/users/admins/${id}`, data)
}

export function assignRole(id, roleId) {
  return patch(`/api/admin/users/admins/${id}/role`, { roleId })
}

export function deleteAdmin(id) {
  return del(`/api/admin/users/admins/${id}`)
}
