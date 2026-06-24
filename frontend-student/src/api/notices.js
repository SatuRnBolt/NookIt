import { get } from './request'

export function getNotices(query = {}) {
  return get('/api/student/notices', {
    page: query.page || 1,
    pageSize: query.pageSize || 20,
    type: query.type || '',
  })
}

export function getNoticeDetail(id) {
  return get(`/api/student/notices/${id}`)
}
