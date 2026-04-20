<template>
  <div class="panel stack oauth-callback-card">
    <div class="stack-sm">
      <p class="eyebrow">OAuth Callback</p>
      <h2>소셜 로그인 처리 중</h2>
      <p class="description">
        리프레시 쿠키를 확인한 뒤 access token을 재발급하고 사용자 인증 상태를 동기화합니다.
      </p>
    </div>

    <div class="oauth-callback-status">
      <strong>{{ statusMessage }}</strong>
      <span>잠시만 기다려 주세요.</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useAuthStore } from '../stores/authStore';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isProcessing = ref(true);

const statusMessage = computed(() => (
  isProcessing.value ? '인증 정보를 불러오는 중입니다.' : '이동 중입니다.'
));

onMounted(async () => {
  const errorCode = route.query.errorCode;
  const errorMessage = route.query.message;

  if (errorCode) {
    const socialLoginErrorMessage = errorMessage || '소셜 로그인에 실패했습니다.';

    authStore.errorUiMessage = socialLoginErrorMessage;
    authStore.showToast(socialLoginErrorMessage, 'error');
    isProcessing.value = false;
    await router.replace('/login');
    return;
  }

  try {
    await authStore.tokenRefresh();
    authStore.showToast('소셜 로그인에 성공했습니다.');
    isProcessing.value = false;
    await router.replace('/');
  } catch (error) {
    const mappedError = mapApiError(error, {
      fallbackMessage: "소셜 로그인에 실패했습니다.",
      stage: "auth",
    });

    authStore.errorUiMessage = mappedError.uiMessage;
    authStore.showToast(mappedError.uiMessage, 'error');
    isProcessing.value = false;
    await router.replace('/login');
  }
});
</script>
