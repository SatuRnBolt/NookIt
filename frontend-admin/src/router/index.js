import { createRouter, createWebHashHistory } from 'vue-router'
import { useAdminAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../components/Layout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', component: () => import('../views/Dashboard.vue') },
      { path: 'rooms', component: () => import('../views/Rooms.vue'), meta: { perm: 'room.view' } },
      { path: 'seats', component: () => import('../views/Seats.vue'), meta: { perm: 'seat.view' } },
      { path: 'bookings', component: () => import('../views/Bookings.vue'), meta: { perm: 'booking.view' } },
      { path: 'violations', component: () => import('../views/Violations.vue'), meta: { perm: 'violation.view' } },
      { path: 'notices', component: () => import('../views/Notices.vue') },
      { path: 'feedback', component: () => import('../views/Feedback.vue') },
      { path: 'users', component: () => import('../views/Users.vue'), meta: { perm: 'user.view' } },
      { path: 'roles', component: () => import('../views/Roles.vue'), meta: { perm: 'role.manage' } },
      { path: 'settings', component: () => import('../views/Settings.vue'), meta: { perm: 'settings.manage' } },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const auth = useAdminAuthStore()
  auth.checkAuth()
  if (!to.meta.public && !auth.isLoggedIn) {
    next('/login')
    return
  }
  if (to.meta.perm && !auth.hasPermission(to.meta.perm)) {
    next('/dashboard')
    return
  }
  next()
})

export default router
