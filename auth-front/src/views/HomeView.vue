<template>
  <div class="panel stack">
    <div>
      <p class="eyebrow">Dashboard</p>
      <h2>인증 상태와 토큰 검증</h2>
      <p class="description">
        로그인 응답으로 인증 상태와 access token을 세팅하고 아래 버튼으로 <br>
        <code>/api/v1/tests/user</code>를 호출해 현재 토큰이 실제로 작동한다면 User ID를 저장합니다.
      </p>
    </div>

    <div class="summary-grid">
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
        <strong>{{ accessToken ? 'set' : 'empty' }}</strong>
      </article>
    </div>

    <div class="actions">
      <button
        type="button"
        class="primary-button"
        :disabled="isLoading || !isAuthenticated"
        @click="authStore.verifyAccessToken()"
      >
        토큰 검증 테스트
      </button>
      <button
        type="button"
        class="secondary-button"
        :disabled="!isAuthenticated || isLoading"
        @click="authStore.logout()"
      >
        로그아웃
      </button>
    </div>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../stores/auth';

const authStore = useAuthStore();
const { userId, isAuthenticated, accessToken, isLoading } = storeToRefs(authStore);
</script>
