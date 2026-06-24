import { get, post, put, del } from './request'

export function getRoles() {
  return get('/api/admin/roles')
}

export function getRole(id) {
  return get(`/api/admin/roles/${id}`)
}

export function createRole(data) {
  return post('/api/admin/roles', data)
}

export function updateRole(id, data) {
  return put(`/api/admin/roles/${id}`, data)
}

export function deleteRole(id) {
  return del(`/api/admin/roles/${id}`)
}

export function savePermissions(id, permissions) {
  return put(`/api/admin/roles/${id}/permissions`, { permissions })
}

export function getAllPermissions() {
  return get('/api/admin/permissions')
}
