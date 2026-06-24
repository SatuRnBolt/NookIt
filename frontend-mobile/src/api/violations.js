import { get, post } from './request'

export function getMyViolations(query = {}) {
  return get('/api/student/violations', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
  })
}

export function submitAppeal(id, reason) {
  return post(`/api/student/violations/${id}/appeal`, { reason })
}
