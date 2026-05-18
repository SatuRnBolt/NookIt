import { get, post, patch } from './request'

export function getFeedbacks(query = {}) {
  return get('/api/admin/feedbacks', {
    page: query.page || 1,
    pageSize: query.pageSize || 20,
    search: query.search || '',
    type: query.type || '',
    status: query.status || '',
  })
}

export function getFeedback(id) {
  return get(`/api/admin/feedbacks/${id}`)
}

export function updateFeedbackStatus(id, status) {
  return patch(`/api/admin/feedbacks/${id}/status`, { status })
}

export function replyFeedback(id, content) {
  return post(`/api/admin/feedbacks/${id}/reply`, { content })
}
