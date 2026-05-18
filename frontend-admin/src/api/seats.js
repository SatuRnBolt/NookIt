import { get, post, put, patch, del } from './request'
import { seatAdminToFrontend, roomSimpleToFrontend } from './transformers'

export function getSeats(query = {}) {
  return get('/api/admin/seats', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    search: query.search || '',
    roomId: query.roomId || '',
    status: query.status || '',
    hasPower: query.hasPower || undefined,
  }).then((data) => ({
    ...data,
    records: (data.records || []).map(seatAdminToFrontend),
  }))
}

export function createSeat(data) {
  return post('/api/admin/seats', data).then(seatAdminToFrontend)
}

export function updateSeat(id, data) {
  return put(`/api/admin/seats/${id}`, data)
}

export function updateSeatStatus(id, status) {
  return patch(`/api/admin/seats/${id}/status`, { status })
}

export function deleteSeat(id) {
  return del(`/api/admin/seats/${id}`)
}

export function getRoomsSimple() {
  return get('/api/admin/rooms/simple').then((list) => (list || []).map(roomSimpleToFrontend))
}
