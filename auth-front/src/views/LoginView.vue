<template>
  <div class="stack">
    <form class="panel form-card" @submit.prevent="submit">
      <div class="stack-sm">
        <p class="eyebrow">Login</p>
        <h2>일반 로그인</h2>
        <p class="description">
          <code>POST /api/v1/auth/login</code>으로 로그인을 요청합니다.
        </p>
      </div>

      <label class="field">
        <span>Username</span>
        <input v-model.trim="form.username" type="text" autocomplete="username" required />
      </label>

      <label class="field">
        <span>Password</span>
        <input v-model.trim="form.password" type="password" autocomplete="current-password" required />
      </label>

      <button type="submit" class="primary-button" :disabled="isLoading">
        {{ isLoading ? '처리 중...' : '로그인' }}
      </button>
    </form>

    <section class="panel form-card">
      <div class="stack-sm">
        <p class="eyebrow">Social Login</p>
        <h2>소셜 로그인</h2>
        <p class="description">
          OAuth2 인증은 백엔드의 `/oauth2/authorization/{provider}` 엔드포인트로 바로 이동합니다.
        </p>
      </div>

      <div class="social-actions">
        <a class="social-button google" :href="googleLoginUrl">Google로 로그인</a>
        <a class="social-button kakao" :href="kakaoLoginUrl">Kakao로 로그인</a>
      </div>
    </section>
  </div>
</template>

<script setup>
import { reactive } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../stores/authStore';

const router = useRouter();
const authStore = useAuthStore();
const { isLoading } = storeToRefs(authStore);

const baseUrl = import.meta.env.VITE_JWT_API_BASE_URL || 'http://localhost:8081';
const googleLoginUrl = `${baseUrl}/oauth2/authorization/google`;
const kakaoLoginUrl = `${baseUrl}/oauth2/authorization/kakao`;

const form = reactive({
  username: '',
  password: '',
});

async function submit() {
  const response = await authStore.login({
    username: form.username,
    password: form.password,
  });

  authStore.showToast(response?.message);
  router.push('/');
}
</script>
