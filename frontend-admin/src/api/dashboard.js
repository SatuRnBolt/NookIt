import { get } from './request'

export function getSummary() {
  return get('/api/admin/dashboard/summary')
}

export function getWeeklyTrend() {
  return get('/api/admin/dashboard/weekly-trend')
}

export function getRoomOccupancy(top = 4) {
  return get('/api/admin/dashboard/room-occupancy', { top })
}

export function getHeatmap() {
  return get('/api/admin/dashboard/heatmap')
}

export function getTodos() {
  return get('/api/admin/dashboard/todos')
}
