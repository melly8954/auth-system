<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { authMode, isLoading, errorMessage } = storeToRefs(authStore)

const form = reactive({
  username: '',
  password: '',
})

async function submit() {
  await authStore.login({
    username: form.username,
    password: form.password,
  })

  router.push('/')
}
</script>

<template>
  <form class="panel form-card" @submit.prevent="submit">
    <div class="stack-sm">
      <p class="eyebrow">Login</p>
      <h2>{{ authMode === 'session' ? '세션 로그인' : 'JWT 로그인' }}</h2>
      <p class="description">
        두 모드 모두 <code>/api/v1/auth/login</code>을 사용하고, JWT 모드만 access token을 응답으로
        받습니다.
      </p>
    </div>

    <label class="field">
      <span>Username</span>
      <input v-model.trim="form.username" type="text" autocomplete="username" required />
    </label>

    <label class="field">
      <span>Password</span>
      <input
        v-model.trim="form.password"
        type="password"
        autocomplete="current-password"
        required
      />
    </label>

    <button type="submit" class="primary-button" :disabled="isLoading">
      {{ isLoading ? '처리 중...' : '로그인' }}
    </button>

    <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
  </form>
</template>
