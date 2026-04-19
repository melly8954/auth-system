import { createApiClient, extractErrorMessage } from '../http'

const baseURL = import.meta.env.VITE_SESSION_API_BASE_URL || 'http://localhost:8080'
const api = createApiClient(baseURL)

export async function signUp(payload) {
  try {
    const response = await api.post('/api/v1/users', payload)
    return response.data
  } catch (error) {
    throw new Error(extractErrorMessage(error, '세션 회원가입에 실패했습니다.'))
  }
}

export async function login(payload) {
  try {
    const response = await api.post('/api/v1/auth/login', payload)
    return response.data
  } catch (error) {
    throw new Error(extractErrorMessage(error, '세션 로그인에 실패했습니다.'))
  }
}

export async function logout() {
  try {
    const response = await api.post('/api/v1/auth/logout')
    return response.data
  } catch (error) {
    throw new Error(extractErrorMessage(error, '세션 로그아웃에 실패했습니다.'))
  }
}

export async function fetchUser() {
  try {
    const response = await api.get('/api/v1/tests/user')
    return response.data
  } catch (error) {
    throw new Error(extractErrorMessage(error, '세션 사용자 조회에 실패했습니다.'))
  }
}
