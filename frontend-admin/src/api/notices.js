import { get, post, put, patch, del } from './request'

export function getNotices(query = {}) {
  return get('/api/admin/notices', {
    page: query.page || 1,
    pageSize: query.pageSize || 20,
    search: query.search || '',
    type: query.type || '',
  })
}

export function getNotice(id) {
  return get(`/api/admin/notices/${id}`)
}

export function createNotice(data) {
  return post('/api/admin/notices', data)
}

export function updateNotice(id, data) {
  return put(`/api/admin/notices/${id}`, data)
}

export function deleteNotice(id) {
  return del(`/api/admin/notices/${id}`)
}

export function publishNotice(id) {
  return patch(`/api/admin/notices/${id}/publish`)
}
