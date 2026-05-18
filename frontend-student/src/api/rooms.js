import { get } from './request'

export function getRooms(query = {}) {
  return get('/api/student/rooms', {
    page: query.page || 1,
    pageSize: query.pageSize || 20,
    search: query.search || '',
    campus: query.campus || '',
  })
}

export function getRoomDetail(id) {
  return get(`/api/student/rooms/${id}`)
}

export function getSeatMap(roomId, date) {
  return get(`/api/student/rooms/${roomId}/seatmap`, { date })
}

export function getSeatSlots(seatId, date) {
  return get(`/api/student/seats/${seatId}/slots`, { date })
}
