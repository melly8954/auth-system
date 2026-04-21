<template>
  <div class="shell">
    <header class="page-header">
      <div>
        <p class="eyebrow">Auth JWT</p>
        <h1>JWT Authentication Frontend</h1>
      </div>
    </header>

    <main class="layout">
      <aside class="sidebar">
        <div class="panel">
          <h2>바로가기</h2>
          <nav class="nav-links">
            <template v-if="isAuthenticated">
              <RouterLink to="/">대시보드</RouterLink>
            </template>
            <template v-else>
              <RouterLink to="/signup">회원가입</RouterLink>
              <RouterLink to="/login">로그인</RouterLink>
            </template>
          </nav>
        </div>

        <div v-if="errorUiMessage" class="panel error-panel">
          <h2>에러</h2>
          <p>{{ errorUiMessage }}</p>
        </div>
      </aside>

      <section class="content">
        <RouterView />
      </section>
    </main>

    <transition name="toast-fade">
      <div v-if="toastUiMessage" class="toast" :class="toastUiType">
        {{ toastUiMessage }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia';
import { useAuthStore } from './stores/authStore';

const authStore = useAuthStore();
const { errorUiMessage, toastUiMessage, toastUiType, isAuthenticated } = storeToRefs(authStore);
</script>
