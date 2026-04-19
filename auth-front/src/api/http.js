import axios from 'axios';

const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
};

export function createApiClient(baseURL, options = {}) {
  return axios.create({
    baseURL,
    withCredentials: true,
    headers: DEFAULT_HEADERS,
    ...options,
  });
}

export function getApiResult(response) {
  return response?.result ?? null;
}

export function getApiErrorCode(error) {
  return error?.response?.data?.errorCode ?? null;
}

export function getApiErrorMessage(error, fallbackMessage = '') {
  return error?.response?.data?.message || fallbackMessage;
}
