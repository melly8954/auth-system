import { createRouter, createWebHistory } from 'vue-router'
import DashBoard from '../views/DashBoard.vue'
import LoginView from '../views/LoginView.vue'
import OAuthCallbackView from '../views/OAuthCallbackView.vue'
import SignupView from '../views/SignupView.vue'
import { useAuthStore } from '../stores/authStore'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: DashBoard,
      meta: { requiresAuth: true },
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
      meta: { guestOnly: true },
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { guestOnly: true },
    },
    {
      path: '/oauth/callback',
      name: 'oauth-callback',
      component: OAuthCallbackView,
    },
  ],
})

// 라우트 이동 전에 로그인 상태를 먼저 확인하고 접근 가능 여부를 판단합니다.
// beforeEach에 전달하는 인자 중 to는 이동하려는 경로를 나타내고 from은 이동 전 현재 경로 정보이다.
router.beforeEach(async (to, from) => {
  const authStore = useAuthStore()

  // 앱 첫 진입 시 refresh token 기반으로 로그인 상태를 복구합니다.
  await authStore.initializeAuth()

  // 목적지 페이지가 로그인 필요한 페이지(`requiresAuth`)인데 현재 비로그인 상태면 로그인 화면으로 보냅니다.
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { name: 'login' }
  }

  // 목적지 페이지가 비로그인 전용 페이지(`guestOnly`)인데 이미 로그인된 상태면 대시보드로 보냅니다.
  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return { name: 'home' }
  }
})

export default router
