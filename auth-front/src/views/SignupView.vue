<template>
  <form class="panel form-card" @submit.prevent="submit">
    <div class="stack-sm">
      <p class="eyebrow">Sign Up</p>
      <h2>회원가입</h2>
      <p class="description">
        <code>POST /api/v1/users</code>로 회원가입을 요청합니다.
      </p>
    </div>

    <label class="field">
      <span>Username</span>
      <input v-model.trim="form.username" type="text" autocomplete="username" required />
    </label>

    <label class="field">
      <span>Password</span>
      <input v-model.trim="form.password" type="password" autocomplete="new-password" required />
    </label>

    <label class="field">
      <span>Email</span>
      <input v-model.trim="form.email" type="email" autocomplete="email" required />
    </label>

    <label class="field">
      <span>Name</span>
      <input v-model.trim="form.name" type="text" required />
    </label>

    <label class="field">
      <span>Nickname</span>
      <input v-model.trim="form.nickname" type="text" required />
    </label>

    <button type="submit" class="primary-button" :disabled="isLoading">
      {{ isLoading ? '처리 중...' : '회원가입' }}
    </button>
  </form>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const { isLoading } = storeToRefs(authStore)

const form = reactive({
  username: '',
  password: '',
  email: '',
  name: '',
  nickname: '',
})

async function submit() {
  await authStore.signUp({
    username: form.username,
    password: form.password,
    email: form.email,
    name: form.name,
    nickname: form.nickname,
  })

  router.push('/login')
}
</script>
