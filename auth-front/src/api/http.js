import axios from 'axios'

const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
}

export function createApiClient(baseURL, options = {}) {
  return axios.create({
    baseURL,
    withCredentials: true,
    headers: DEFAULT_HEADERS,
    ...options,
  })
}

export function extractErrorMessage(error, fallbackMessage) {
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    fallbackMessage
  )
}
