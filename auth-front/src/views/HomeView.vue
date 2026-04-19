<script setup>
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const authStore = useAuthStore()
const { authMode, userId, isAuthenticated, accessToken } = storeToRefs(authStore)
</script>

<template>
  <div class="panel stack">
    <div>
      <p class="eyebrow">Dashboard</p>
      <h2>인증 상태 확인</h2>
      <p class="description">
        현재 백엔드에서 사용 가능한 보호 API는 <code>/api/v1/tests/user</code>입니다. 이 값으로
        로그인 여부를 판단합니다.
      </p>
    </div>

    <div class="summary-grid">
      <article class="summary-card">
        <span>Mode</span>
        <strong>{{ authMode }}</strong>
      </article>
      <article class="summary-card">
        <span>Authenticated</span>
        <strong>{{ isAuthenticated ? 'true' : 'false' }}</strong>
      </article>
      <article class="summary-card">
        <span>User ID</span>
        <strong>{{ userId ?? '-' }}</strong>
      </article>
      <article class="summary-card">
        <span>Access Token</span>
        <strong>{{ authMode === 'jwt' ? (accessToken ? 'set' : 'empty') : 'not used' }}</strong>
      </article>
    </div>

    <div class="actions">
      <button type="button" class="primary-button" @click="authStore.restoreSession()">
        인증 상태 다시 확인
      </button>
      <button
        type="button"
        class="secondary-button"
        :disabled="!isAuthenticated"
        @click="authStore.logout()"
      >
        로그아웃
      </button>
    </div>
  </div>
</template>
