import { get, post } from './request'

export function getViolations(query = {}) {
  return get('/api/admin/violations', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    search: query.search || '',
  })
}

export function getViolationStats() {
  return get('/api/admin/violations/stats')
}

export function getStudentViolations(studentId) {
  return get(`/api/admin/violations/student/${studentId}`)
}

export function suspendStudent(studentId, data) {
  return post(`/api/admin/violations/student/${studentId}/suspend`, data)
}
