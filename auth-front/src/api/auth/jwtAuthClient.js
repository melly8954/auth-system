import { useAuthStore } from '../../stores/auth';
import { createApiClient } from '../http';

const baseURL = import.meta.env.VITE_JWT_API_BASE_URL;
const api = createApiClient(baseURL);

api.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  const accessToken = authStore.accessToken;

  if (!accessToken) {
    return config;
  }

  config.headers = config.headers || {};
  config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

export async function signUp(payload) {
  const response = await api.post('/api/v1/users', payload);
  return response.data;
}

export async function login(payload) {
  const response = await api.post('/api/v1/auth/login', payload);
  return response.data;
}

export async function reissueToken() {
  const response = await api.post('/api/v1/auth/reissue');
  return response.data;
}

export async function logout() {
  const response = await api.post('/api/v1/auth/logout');
  return response.data;
}

export async function fetchUser() {
  const response = await api.get('/api/v1/tests/user');
  return response.data;
}
