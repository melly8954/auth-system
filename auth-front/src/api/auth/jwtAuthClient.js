import { createApiClient, extractErrorMessage } from '../http'

const baseURL = import.meta.env.VITE_JWT_API_BASE_URL || 'http://localhost:8081'
const api = createApiClient(baseURL)

let accessToken = ''

function applyAuthorizationHeader(token) {
  accessToken = token || ''

  if (accessToken) {
    api.defaults.headers.common.Authorization = `Bearer ${accessToken}`
    return
  }

  delete api.defaults.headers.common.Authorization
}

export function getAccessToken() {
  return accessToken
}

export function clearAccessToken() {
  applyAuthorizationHeader('')
}

export async function signUp(payload) {
  try {
    const response = await api.post('/api/v1/users', payload)
    return response.data
  } catch (error) {
    throw new Error(extractErrorMessage(error, 'JWT 회원가입에 실패했습니다.'))
  }
}

export async function login(payload) {
  try {
    const response = await api.post('/api/v1/auth/login', payload)
    const token = response?.data?.data?.accessToken || ''
    applyAuthorizationHeader(token)
    return response.data
  } catch (error) {
    clearAccessToken()
    throw new Error(extractErrorMessage(error, 'JWT 로그인에 실패했습니다.'))
  }
}

export async function reissueToken() {
  try {
    const response = await api.post('/api/v1/auth/reissue')
    const token = response?.data?.data?.newAccessToken || ''
    applyAuthorizationHeader(token)
    return response.data
  } catch (error) {
    clearAccessToken()
    throw new Error(extractErrorMessage(error, 'JWT 토큰 재발급에 실패했습니다.'))
  }
}

export async function logout() {
  try {
    const response = await api.post('/api/v1/auth/logout')
    clearAccessToken()
    return response.data
  } catch (error) {
    clearAccessToken()
    throw new Error(extractErrorMessage(error, 'JWT 로그아웃에 실패했습니다.'))
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
