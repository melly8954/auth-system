<script setup>
import { computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from './stores/auth'

const authStore = useAuthStore()
const { authMode, userId, isAuthenticated, isLoading, errorMessage, accessToken } =
  storeToRefs(authStore)

const authLabel = computed(() => (authMode.value === 'session' ? 'Session' : 'JWT'))

onMounted(() => {
  authStore.bootstrap()
})
</script>

<template>
  <div class="shell">
    <header class="hero">
      <div>
        <p class="eyebrow">Vue 3 + Pinia + Axios</p>
        <h1>Auth System Frontend</h1>
        <p class="hero-copy">
          하나의 프론트에서 <strong>auth-session</strong>과 <strong>auth-jwt</strong>를
          전환해서 테스트할 수 있게 구성했습니다.
        </p>
      </div>

      <div class="status-card">
        <p><span>현재 모드</span><strong>{{ authLabel }}</strong></p>
        <p><span>인증 상태</span><strong>{{ isAuthenticated ? '로그인됨' : '로그아웃' }}</strong></p>
        <p><span>사용자 ID</span><strong>{{ userId ?? '-' }}</strong></p>
        <p v-if="authMode === 'jwt'">
          <span>Access Token</span>
          <strong>{{ accessToken ? '보유 중' : '없음' }}</strong>
        </p>
        <p v-if="isLoading"><span>상태</span><strong>처리 중</strong></p>
      </div>
    </header>

    <main class="layout">
      <aside class="sidebar">
        <div class="panel">
          <h2>인증 모드</h2>
          <div class="mode-toggle">
            <button
              type="button"
              class="mode-button"
              :class="{ active: authMode === 'session' }"
              @click="authStore.setAuthMode('session')"
            >
              Session
            </button>
            <button
              type="button"
              class="mode-button"
              :class="{ active: authMode === 'jwt' }"
              @click="authStore.setAuthMode('jwt')"
            >
              JWT
            </button>
          </div>
          <p class="hint">
            `session`은 쿠키 기반, `jwt`는 `Authorization` 헤더 + refresh cookie 기반입니다.
          </p>
        </div>

        <div class="panel">
          <h2>바로가기</h2>
          <nav class="nav-links">
            <RouterLink to="/">대시보드</RouterLink>
            <RouterLink to="/login">로그인</RouterLink>
            <RouterLink to="/signup">회원가입</RouterLink>
          </nav>
        </div>

        <div v-if="errorMessage" class="panel error-panel">
          <h2>에러</h2>
          <p>{{ errorMessage }}</p>
        </div>
      </aside>

      <section class="content">
        <RouterView />
      </section>
    </main>
  </div>
</template>
