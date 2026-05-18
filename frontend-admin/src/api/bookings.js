import { get, post } from './request'

export function getBookings(query = {}) {
  return get('/api/admin/bookings', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
    search: query.search || '',
    status: query.status || '',
    date: query.date || '',
    tab: query.tab || 'all',
  })
}

export function getBookingStats(tab = 'all') {
  return get('/api/admin/bookings/stats', { tab })
}

export function cancelBooking(id, reason) {
  return post(`/api/admin/bookings/${id}/cancel`, { reason })
}

export function checkinBooking(id) {
  return post(`/api/admin/bookings/${id}/checkin`)
}
