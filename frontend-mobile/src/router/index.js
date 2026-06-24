import { createRouter, createWebHashHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../components/MobileLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: () => import('../views/Home.vue') },
      { path: 'reservations', component: () => import('../views/MyReservations.vue') },
      { path: 'checkin', component: () => import('../views/CheckIn.vue') },
      { path: 'notices', component: () => import('../views/Notices.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') },
    ],
  },
  { path: '/room/:id', component: () => import('../views/RoomDetail.vue') },
  { path: '/booking/:roomId/:seatId', component: () => import('../views/BookingConfirm.vue') },
  { path: '/violations', component: () => import('../views/MyViolations.vue') },
  { path: '/notice/:id', component: () => import('../views/NoticeDetail.vue') },
  { path: '/feedback', component: () => import('../views/Feedback.vue') },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const auth = useAuthStore()
  await auth.checkAuth()
  if (!to.meta.public && !auth.isLoggedIn) {
    next('/login')
    return
  }
  next()
})

export default router
