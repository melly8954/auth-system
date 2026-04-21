import { defineStore } from "pinia";
import * as jwtAuthClient from "../api/auth/jwtAuthClient";
import { mapApiError } from "../api/apiErrorMapper";
import { getApiResult } from "../api/http";

let toastTimerId = null;

export const useAuthStore = defineStore("auth", {
  state: () => ({
    userId: null,
    accessToken: "",
    isAuthenticated: false,
    isInitialized: false,
    isLoading: false,
    errorUiMessage: "",
    toastUiMessage: "",
    toastUiType: "success",
  }),

  actions: {
    showToast(message, type = "success") {
      this.toastUiMessage = message || "";
      this.toastUiType = type;

      if (toastTimerId) {
        clearTimeout(toastTimerId);
      }

      toastTimerId = setTimeout(() => {
        this.hideToast();
      }, 2500);
    },

    hideToast() {
      this.toastUiMessage = "";

      if (toastTimerId) {
        clearTimeout(toastTimerId);
        toastTimerId = null;
      }
    },

    syncAccessToken() {
      this.isAuthenticated = !!this.accessToken;
    },

    applyLoginState(response) {
      const result = getApiResult(response);

      this.accessToken = result?.accessToken || "";
      this.syncAccessToken();
    },

    resetState() {
      this.userId = null;
      this.accessToken = "";
      this.isAuthenticated = false;
    },

    applyUserState(response) {
      const result = getApiResult(response);
      this.userId = result ?? null;
    },

    applyMappedError(error, fallbackMessage, options = {}) {
      const mappedError =
        error?.mappedError ||
        mapApiError(error, {
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
      this.errorUiMessage = "";

      try {
        return await jwtAuthClient.signUp(payload);
      } catch (error) {
        this.applyMappedError(error, "회원가입에 실패했습니다.");
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async login(payload) {
      this.isLoading = true;
      this.errorUiMessage = "";

      try {
        const response = await jwtAuthClient.login(payload);
        this.applyLoginState(response);
        return response;
      } catch (error) {
        this.resetState();
        this.applyMappedError(error, "로그인에 실패했습니다.", {
          stage: "login",
        });
        this.showToast(this.errorUiMessage, "error");
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async logout() {
      this.isLoading = true;
      this.errorUiMessage = "";

      try {
        const response = await jwtAuthClient.logout();
        this.showToast(response?.message);
      } catch (error) {
        this.applyMappedError(error, "로그아웃에 실패했습니다.", {
          stage: "logout",
        });
        this.showToast(this.errorUiMessage, "error");
      } finally {
        this.resetState();
        this.isLoading = false;
      }
    },

    async fetchCurrentUser() {
      this.isLoading = true;
      this.errorUiMessage = "";

      try {
        const response = await jwtAuthClient.fetchUser();
        this.applyUserState(response);
        this.showToast(response?.message);
        return response;
      } catch (error) {
        this.applyMappedError(error, "토큰 검증에 실패했습니다.", {
          stage: "access",
        });
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    // 라우터 가드에서 앱 첫 진입 시 로그인 상태 복구에 사용합니다.
    async initializeAuth() {
      if (this.isInitialized) {
        return;
      }

      try {
        // 앱이 처음 열릴 때 refresh token으로 로그인 상태 복구를 시도합니다.
        await this.tokenRefresh();
      } catch (error) {
        // refresh에 실패하면 비로그인 상태로 처리하고 다음 로직을 계속 진행합니다.
        this.resetState();
      } finally {
        this.isInitialized = true;
      }
    },

    async tokenRefresh() {
      this.isLoading = true;
      this.errorUiMessage = "";

      try {
        const reissueResponse = await jwtAuthClient.reissueToken();

        this.accessToken = getApiResult(reissueResponse)?.newAccessToken || "";
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
