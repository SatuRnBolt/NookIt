import { get, post } from './request'

export function getMyFeedbacks(query = {}) {
  return get('/api/student/feedbacks', {
    page: query.page || 1,
    pageSize: query.pageSize || 20,
  })
}

export function submitFeedback(data) {
  return post('/api/student/feedbacks', data)
}
