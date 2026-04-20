import { defineStore } from 'pinia';
import * as jwtAuthClient from '../api/auth/jwtAuthClient';
import { mapApiError } from '../api/apiErrorMapper';
import { getApiResult } from '../api/http';

let toastTimerId = null;

export const useAuthStore = defineStore('auth', {
  state: () => ({
    userId: null,
    accessToken: '',
    isAuthenticated: false,
    isLoading: false,
    errorUiMessage: '',
    toastUiMessage: '',
    toastUiType: 'success',
  }),

  actions: {
    showToast(message, type = 'success') {
      this.toastUiMessage = message || '';
      this.toastUiType = type;

      if (toastTimerId) {
        clearTimeout(toastTimerId);
      }

      toastTimerId = setTimeout(() => {
        this.hideToast();
      }, 2500);
    },

    hideToast() {
      this.toastUiMessage = '';

      if (toastTimerId) {
        clearTimeout(toastTimerId);
        toastTimerId = null;
      }
    },

    syncAccessToken() {
      this.isAuthenticated = !!this.accessToken;
    },

    applyLoginResult(response) {
      const result = getApiResult(response);

      this.accessToken = result?.accessToken || '';
      this.syncAccessToken();
    },

    resetState() {
      this.userId = null;
      this.accessToken = '';
      this.isAuthenticated = false;
    },

    applyVerifyResult(response) {
      const result = getApiResult(response);
      this.userId = result ?? null;
    },

    applyMappedError(error, fallbackMessage, options = {}) {
      const mappedError = error?.mappedError || mapApiError(error, {
        fallbackMessage,
        ...options,
      });

      if (!this.errorUiMessage) {
        this.errorUiMessage = mappedError.uiMessage;
      }

      return mappedError;
    },

    async signUp(payload) {
      this.isLoading = true;
      this.errorUiMessage = '';

      try {
        return await jwtAuthClient.signUp(payload);
      } catch (error) {
        this.applyMappedError(error, '회원가입에 실패했습니다.');
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async login(payload) {
      this.isLoading = true;
      this.errorUiMessage = '';

      try {
        const response = await jwtAuthClient.login(payload);
        this.applyLoginResult(response);
        return response;
      } catch (error) {
        this.resetState();
        this.applyMappedError(error, '로그인에 실패했습니다.', { stage: 'login' });
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async logout() {
      this.isLoading = true;
      this.errorUiMessage = '';

      try {
        const response = await jwtAuthClient.logout();
        this.showToast(response?.message);
      } catch (error) {
        this.applyMappedError(error, '로그아웃에 실패했습니다.', { stage: 'logout' });
      } finally {
        this.resetState();
        this.isLoading = false;
      }
    },

    async verifyAccessToken() {
      this.isLoading = true;
      this.errorUiMessage = '';

      try {
        const response = await jwtAuthClient.fetchUser();
        this.applyVerifyResult(response);
        this.showToast(response?.message);
        return response;
      } catch (error) {
        this.applyMappedError(error, '토큰 검증에 실패했습니다.', { stage: 'access' });
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async tokenRefresh() {
      this.isLoading = true;
      this.errorUiMessage = '';

      try {
        const reissueResponse = await jwtAuthClient.reissueToken();
        const reissueResult = getApiResult(reissueResponse);

        this.accessToken = reissueResult?.newAccessToken || '';
        this.syncAccessToken();
      } catch (error) {
        // 리프레시 토큰도 만료된 경우, 로그아웃 처리
        this.resetState();
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
  },
});
