import { get, post } from './request'

export function login(identity, password) {
  return post('/api/auth/login', { identity, password })
}

export function logout() {
  return post('/api/auth/logout')
}

export function getMe() {
  return get('/api/auth/me')
}
