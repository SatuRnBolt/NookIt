import { createRouter, createWebHashHistory } from 'vue-router'
import { useStudentAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../components/StudentLayout.vue'),
    children: [
      { path: '', redirect: '/home' },
      { path: 'home', component: () => import('../views/Home.vue') },
      { path: 'room/:id', component: () => import('../views/RoomDetail.vue') },
      { path: 'booking-confirm/:roomId/:seatId', component: () => import('../views/BookingConfirm.vue') },
      { path: 'reservations', component: () => import('../views/MyReservations.vue') },
      { path: 'violations', component: () => import('../views/MyViolations.vue') },
      { path: 'violation/:id/appeal', component: () => import('../views/ViolationAppeal.vue') },
      { path: 'notices', component: () => import('../views/Notices.vue') },
      { path: 'notice/:id', component: () => import('../views/NoticeDetail.vue') },
      { path: 'feedback', component: () => import('../views/Feedback.vue') },
      { path: 'profile', component: () => import('../views/Profile.vue') },
      { path: 'ai-chat', component: () => import('../views/AiChat.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
})

router.beforeEach(async (to, from, next) => {
  const auth = useStudentAuthStore()
  await auth.checkAuth()
  if (!to.meta.public && !auth.isLoggedIn) {
    next('/login')
    return
  }
  next()
})

export default router
