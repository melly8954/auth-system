import { useAuthStore } from "../../stores/authStore";
import { createApiClient } from "../http";

const baseURL = import.meta.env.VITE_JWT_API_BASE_URL;
const api = createApiClient(baseURL);

// Axios 인터셉터
//  - 요청(Request) 또는 응답(Response)이 처리되기 전에 가로채서 특정 로직을 수행하도록 하는 기능이다.

// 요청(Request) 인터셉터
//  - HTTP 요청이 서버로 전송되기 전에 실행된다.
api.interceptors.request.use(
  (config) => {
    // 모든 요청에 JWT 토큰을 헤더에 추가하도록 요청(Request) 인터셉터를 구현
    // config는 Axios가 요청을 보내기 전에 사용하는 요청 설정 객체
    // (url, method, headers, params, data 등 요청에 대한 모든 정보 포함)
    if (config._skipAuthRefresh) {
      return config;
    }

    // Pinia Store에서 accessToken을 가져온다.
    const authStore = useAuthStore();
    const accessToken = authStore.accessToken;

    config.headers = config.headers || {};

    // accessToken을 검증 후 Authorization 헤더에 accessToken을 추가한다.
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }

    // config를 반환해야 요청이 계속 진행됨
    return config;
  },
  // 비동기 코드에서 에러를 처리하거나 에러를 즉시 반환할 때 사용한다.
  (error) => Promise.reject(error),
);

// 응답(Response) 인터셉터
//  - 서버에서 HTTP 응답이 도착한 후에 실행된다.
api.interceptors.response.use(
  // (response) => {
  //   return response;
  // },
  (response) => response,

  // 액세스 토큰이 만료되면 액세스 토큰을 재발급 받아서 요청을 다시 시도하도록 구현
  async (error) => {
    // 이전 요청에 대한 config 객체를 얻어온다.
    const originConfig = error.config;

    if (originConfig?._skipAuthRefresh) {
      return Promise.reject(error);
    }

    // 토큰이 만료되어 401 에러가 발생한 경우
    if (error.response?.status === 401 && !originConfig?._retry) {
      originConfig._retry = true;
      const authStore = useAuthStore();

      try {
        // 리프레시 토큰을 사용하여 새 액세스 토큰을 요청한다.
        await authStore.tokenRefresh();
        // 실패했던 원래 요청을 다시 재시도
        return api(originConfig);
      } catch (error) {
        const refreshFailureMessage = getApiErrorMessage(error, "인증 상태를 복구하지 못했습니다.");

        authStore.errorUiMessage = refreshFailureMessage;
        authStore.showToast(refreshFailureMessage);
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  },
);

export async function signUp(payload) {
  const response = await api.post("/api/v1/users", payload);
  return response.data;
}

export async function login(payload) {
  const response = await api.post("/api/v1/auth/login", payload);
  return response.data;
}

export async function logout() {
  const response = await api.post("/api/v1/auth/logout");
  return response.data;
}

export async function reissueToken() {
  const response = await api.post(
    "/api/v1/auth/reissue",
    {},
    { _skipAuthRefresh: true },
  );
  return response.data;
}

export async function fetchUser() {
  const response = await api.get("/api/v1/tests/user");
  return response.data;
}
