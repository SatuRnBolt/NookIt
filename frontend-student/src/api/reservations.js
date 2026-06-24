import { get, post } from './request'

export function getMyReservations(query = {}) {
  return get('/api/student/reservations', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    status: query.status || '',
  })
}

export function getReservationDetail(id) {
  return get('/api/student/reservations/detail', { reservationId: String(id) })
}

export function createReservation(data) {
  return post('/api/student/reservations', data)
}

export function cancelReservation(id) {
  return post(`/api/student/reservations/${id}/cancel`)
}
