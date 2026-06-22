import { get, post, put } from './request'

export function login(identity, password) {
  return post('/api/auth/login', { identity, password })
}

export function logout() {
  return post('/api/auth/logout')
}

export function getMe() {
  return get('/api/auth/me')
}

export function updateProfile(data) {
  return put('/api/auth/me', data)
}

export function updateMySignature(signature) {
  return put('/api/auth/me/signature', { signature })
}

export function changePassword(data) {
  return put('/api/auth/me/password', data)
}
