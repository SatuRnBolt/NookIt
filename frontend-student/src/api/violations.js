import { get, post } from './request'

export function getMyViolations() {
  return get('/api/student/violations')
}

export function appealViolation(id, reason) {
  return post(`/api/student/violations/${id}/appeal`, { reason })
}
