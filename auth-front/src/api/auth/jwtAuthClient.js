import { createApiClient, getApiResult } from '../http'

const baseURL = import.meta.env.VITE_JWT_API_BASE_URL;
const api = createApiClient(baseURL);

let accessToken = '';

api.interceptors.request.use((config) => {
  if (!accessToken) {
    return config;
  }

  config.headers = config.headers || {};
  config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

function applyAuthorizationHeader(token) {
  accessToken = token || '';

  if (accessToken) {
    api.defaults.headers.common.Authorization = `Bearer ${accessToken}`;
    return;
  }

  delete api.defaults.headers.common.Authorization;
}

export function getAccessToken() {
  return accessToken;
}

export function clearAccessToken() {
  applyAuthorizationHeader('');
}

export async function signUp(payload) {
  try {
    const response = await api.post('/api/v1/users', payload);
    return response.data;
  } catch (error) {
    throw error;
  }
}

export async function login(payload) {
  try {
    const response = await api.post('/api/v1/auth/login', payload)
    const token = getApiResult(response)?.accessToken || ''
    applyAuthorizationHeader(token)
    return response.data
  } catch (error) {
    clearAccessToken()
    throw new Error(getErrorMessage(error, 'JWT 로그인에 실패했습니다.'))
  }
}

export async function reissueToken() {
  try {
    const response = await api.post('/api/v1/auth/reissue')
    const token = getApiResult(response)?.newAccessToken || ''
    applyAuthorizationHeader(token)
    return response.data
  } catch (error) {
    clearAccessToken()
    throw new Error(getErrorMessage(error, 'JWT 토큰 재발급에 실패했습니다.'))
  }
}

export async function logout() {
  try {
    const response = await api.post('/api/v1/auth/logout')
    clearAccessToken()
    return response.data
  } catch (error) {
    clearAccessToken()
    throw new Error(getErrorMessage(error, 'JWT 로그아웃에 실패했습니다.'))
  }
}

export async function fetchUser() {
  try {
    const response = await api.get('/api/v1/tests/user')
    return response.data
  } catch (error) {
    throw error
  }
}
