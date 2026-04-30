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

    <label class="field">
      <span>Profile Image</span>
      <input
        accept="image/jpeg,image/png,image/webp"
        type="file"
        @change="handleImageChange"
      />
      <small v-if="selectedImageName">{{ selectedImageName }}</small>
    </label>

    <img
      v-if="previewUrl"
      :src="previewUrl"
      alt="Profile image preview"
      style="width: 96px; height: 96px; border-radius: 50%; object-fit: cover;"
    />

    <button type="submit" class="primary-button" :disabled="isSubmitting">
      {{ isSubmitting ? '처리 중...' : '회원가입' }}
    </button>
  </form>
</template>

<script setup>
import axios from 'axios';
import { computed, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '../stores/authStore';
import { requestProfileImageUploadUrl } from '../api/auth/auth';

const router = useRouter();
const authStore = useAuthStore();
const { isLoading } = storeToRefs(authStore);

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

const form = reactive({
  username: '',
  password: '',
  email: '',
  name: '',
  nickname: '',
  imageUrl: '',
});

const selectedImageName = ref('');
const previewUrl = ref('');
const isUploadingImage = ref(false);

const isSubmitting = computed(() => isLoading.value || isUploadingImage.value);

function resetSelectedImage(inputElement) {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }

  selectedImageName.value = '';
  previewUrl.value = '';
  form.imageUrl = '';

  if (inputElement) {
    inputElement.value = '';
  }
}

async function uploadSelectedImage(file) {
  const uploadInfoResponse = await requestProfileImageUploadUrl({
    fileName: file.name,
    contentType: file.type,
    fileSize: file.size,
  });
  const uploadInfo = uploadInfoResponse?.result;

  await axios.put(uploadInfo.uploadUrl, file, {
    headers: {
      'Content-Type': file.type,
    },
  });

  form.imageUrl = uploadInfo.imageUrl;
}

async function handleImageChange(event) {
  const file = event.target.files?.[0] ?? null;

  if (!file) {
    resetSelectedImage(event.target);
    return;
  }

  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) {
    authStore.showToast('jpg, png, webp 파일만 업로드할 수 있습니다.', 'error');
    resetSelectedImage(event.target);
    return;
  }

  if (file.size > MAX_FILE_SIZE_BYTES) {
    authStore.showToast('프로필 이미지는 5MB 이하만 업로드할 수 있습니다.', 'error');
    resetSelectedImage(event.target);
    return;
  }

  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }

  selectedImageName.value = file.name;
  previewUrl.value = URL.createObjectURL(file);
  form.imageUrl = '';
  isUploadingImage.value = true;

  try {
    await uploadSelectedImage(file);
    authStore.showToast('프로필 이미지 업로드가 완료되었습니다.');
  } catch (error) {
    resetSelectedImage(event.target);
    authStore.showToast('프로필 이미지 업로드에 실패했습니다.', 'error');
  } finally {
    isUploadingImage.value = false;
  }
}

async function submit() {
  try {
    const response = await authStore.signUp({
      username: form.username,
      password: form.password,
      email: form.email,
      name: form.name,
      nickname: form.nickname,
      imageUrl: form.imageUrl || null,
    });

    authStore.showToast(response?.message);
    router.push('/login');
  } catch (error) {
    if (!authStore.errorUiMessage) {
      authStore.showToast('회원가입에 실패했습니다.', 'error');
    }
  }
}
</script>
