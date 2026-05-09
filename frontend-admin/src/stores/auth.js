import { defineStore } from 'pinia'
import { ref } from 'vue'
import { mockAdminUsers, mockRoles, PERMISSIONS } from '../mock/data'

export const useAdminAuthStore = defineStore('adminAuth', () => {
  const user = ref(null)
  const isLoggedIn = ref(false)

  function login(email, password) {
    // Mock: accept admin@study.edu.cn / admin123 or any admin user
    const found = mockAdminUsers.find(u => u.email === email) || mockAdminUsers[0]
    user.value = { ...found }
    isLoggedIn.value = true
    localStorage.setItem('admin_token', 'mock_admin_token')
    return Promise.resolve(user.value)
  }

  function logout() {
    user.value = null
    isLoggedIn.value = false
    localStorage.removeItem('admin_token')
  }

  function checkAuth() {
    const token = localStorage.getItem('admin_token')
    if (token && !isLoggedIn.value) {
      user.value = { ...mockAdminUsers[0] }
      isLoggedIn.value = true
    }
  }

  function hasPermission(permKey) {
    if (!user.value) return false
    const role = mockRoles.find(r => r.id === user.value.role)
    return role?.permissions.includes(permKey) ?? false
  }

  function getUserPermissions() {
    if (!user.value) return []
    const role = mockRoles.find(r => r.id === user.value.role)
    return role?.permissions ?? []
  }

  return { user, isLoggedIn, login, logout, checkAuth, hasPermission, getUserPermissions }
})
