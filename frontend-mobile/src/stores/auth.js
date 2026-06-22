import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, logout as logoutApi, getMe } from '../api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isLoggedIn = ref(false)

  async function login(identity, password) {
    const data = await loginApi(identity, password)
    localStorage.setItem('student_token', data.token)
    user.value = data.user
    isLoggedIn.value = true
    return user.value
  }

  async function logout() {
    try { await logoutApi() } catch { /* ignore */ }
    user.value = null
    isLoggedIn.value = false
    localStorage.removeItem('student_token')
  }

  async function checkAuth() {
    const token = localStorage.getItem('student_token')
    if (!token) return
    try {
      const me = await getMe()
      user.value = me
      isLoggedIn.value = true
    } catch {
      localStorage.removeItem('student_token')
      user.value = null
      isLoggedIn.value = false
    }
  }

  return { user, isLoggedIn, login, logout, checkAuth }
})
