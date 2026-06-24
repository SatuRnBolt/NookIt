import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getMe } from '../api/auth'

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const user = ref(null)
  const isLoggedIn = ref(false)

  async function login(email, password) {
    const data = await loginApi(email, password)
    localStorage.setItem('admin_token', data.token)
    user.value = data.user
    isLoggedIn.value = true
    return data.user
  }

  function logout() {
    user.value = null
    isLoggedIn.value = false
    localStorage.removeItem('admin_token')
  }

  function checkAuth() {
    const token = localStorage.getItem('admin_token')
    if (token && !isLoggedIn.value) {
      isLoggedIn.value = true
    }
  }

  async function loadMe() {
    if (!localStorage.getItem('admin_token')) return
    try {
      const me = await getMe()
      user.value = me
      isLoggedIn.value = true
    } catch {
      localStorage.removeItem('admin_token')
      isLoggedIn.value = false
    }
  }

  function hasPermission(permKey) {
    if (!user.value) return true
    const perms = user.value.permissions ?? []
    return perms.length === 0 || perms.includes(permKey)
  }

  function getUserPermissions() {
    return user.value?.permissions ?? []
  }

  return { user, isLoggedIn, login, logout, checkAuth, loadMe, hasPermission, getUserPermissions }
})
