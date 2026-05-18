import { get, put } from './request'

export function getSettings() {
  return get('/api/admin/settings')
}

export function updateSettings(data) {
  return put('/api/admin/settings', data)
}

export function getSettingsLogs(query = {}) {
  return get('/api/admin/settings/logs', {
    page: query.page || 1,
    pageSize: query.pageSize || 10,
  })
}
