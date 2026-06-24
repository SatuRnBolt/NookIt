import { get, post, put, patch, del } from './request'
import { roomToFrontend, roomFormToBackend, seatMapToFrontend, seatMapSeatToFrontend, seatMapSeatToBackend } from './transformers'

// 自习室 CRUD
export function getRooms(query = {}) {
  return get('/api/admin/rooms', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    search: query.search || '',
    campus: query.campus || '',
    visibilityScope: query.visibilityScope || '',
  }).then((data) => ({
    ...data,
    records: (data.records || []).map(roomToFrontend),
  }))
}

export function createRoom(form) {
  return post('/api/admin/rooms', roomFormToBackend(form)).then(roomToFrontend)
}

export function getRoom(id) {
  return get(`/api/admin/rooms/${id}`).then(roomToFrontend)
}

export function updateRoom(id, form) {
  return put(`/api/admin/rooms/${id}`, roomFormToBackend(form)).then(roomToFrontend)
}

export function deleteRoom(id) {
  return del(`/api/admin/rooms/${id}`)
}

export function updateRoomStatus(id, roomStatus) {
  return patch(`/api/admin/rooms/${id}/status`, { room_status: roomStatus })
}

// 统计
export function getRoomStats() {
  return get('/api/admin/rooms/stats')
}

// 座位地图
export function getSeatMap(roomId, version) {
  return get(`/api/admin/rooms/${roomId}/seatmap`, { version }).then(seatMapToFrontend)
}

export function createSeatMapDraft(roomId, data) {
  return post(`/api/admin/rooms/${roomId}/seatmap`, data).then(seatMapToFrontend)
}

export function updateSeatMap(roomId, mapId, data) {
  return put(`/api/admin/rooms/${roomId}/seatmap/${mapId}`, data)
}

export function publishSeatMap(roomId, mapId) {
  return put(`/api/admin/rooms/${roomId}/seatmap/${mapId}/publish`)
}

// 座位操作
export function addSeat(roomId, mapId, data) {
  return post(`/api/admin/rooms/${roomId}/seatmap/${mapId}/seats`, seatMapSeatToBackend(data))
    .then(seatMapSeatToFrontend)
}

export function updateSeat(roomId, mapId, seatId, data) {
  return put(`/api/admin/rooms/${roomId}/seatmap/${mapId}/seats/${seatId}`, seatMapSeatToBackend(data))
}

export function deleteSeat(roomId, mapId, seatId) {
  return del(`/api/admin/rooms/${roomId}/seatmap/${mapId}/seats/${seatId}`)
}

export function duplicateSeat(roomId, mapId, seatId, pos) {
  return post(`/api/admin/rooms/${roomId}/seatmap/${mapId}/seats/${seatId}/duplicate`, pos)
    .then(seatMapSeatToFrontend)
}

// 字典
export function getCampuses() {
  return get('/api/admin/dict/campuses')
}

export function getOrganizations() {
  return get('/api/admin/dict/organizations')
}

export function getRoomTypes() {
  return get('/api/admin/dict/room-types')
}
