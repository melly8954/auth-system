import {
  COMMON_ERROR_CODES,
  USER_ERROR_CODES,
  AUTH_ERROR_CODES,
} from "./apiErrorCodes";
import { getApiErrorCode, getApiErrorMessage } from "./http";

const USER_ERROR_CODE_VALUES = Object.values(USER_ERROR_CODES);
const AUTH_ERROR_CODE_VALUES = Object.values(AUTH_ERROR_CODES);

const ACCESS_TOKEN_ERROR_CODES_VALUES = [
  AUTH_ERROR_CODES.ACCESS_TOKEN_NOT_FOUND,
  AUTH_ERROR_CODES.ACCESS_TOKEN_EXPIRED,
  AUTH_ERROR_CODES.ACCESS_TOKEN_INVALID,
  AUTH_ERROR_CODES.ACCESS_TOKEN_OF_BLACK_LIST,
];

const REFRESH_TOKEN_ERROR_CODES_VALUES = [
  AUTH_ERROR_CODES.REFRESH_TOKEN_NOT_FOUND,
  AUTH_ERROR_CODES.REFRESH_TOKEN_EXPIRED,
  AUTH_ERROR_CODES.REFRESH_TOKEN_INVALID,
  AUTH_ERROR_CODES.REFRESH_TOKEN_NOT_IN_REDIS,
];

export function isAccessTokenError(error) {
  const errorCode = getApiErrorCode(error);
  return ACCESS_TOKEN_ERROR_CODES_VALUES.includes(errorCode);
}

export function isRefreshTokenError(error) {
  const errorCode = getApiErrorCode(error);
  return REFRESH_TOKEN_ERROR_CODES_VALUES.includes(errorCode);
}

export function mapApiError(error, options = {}) {
  const status = error?.response?.status ?? null;
  const errorCode = getApiErrorCode(error);
  const serverMessage = getApiErrorMessage(error, "");

  const mappedError = {
    originalError: error,
    status,
    errorCode,
    message: serverMessage,
    uiMessage: serverMessage,
    domain: "unknown",
    stage: options.stage || null,
    shouldLogout: false,
  };

  if (!error?.response) {
    mappedError.domain = "network";
    mappedError.uiMessage = "네트워크 오류가 발생했습니다.";
    return mappedError;
  }

  if (USER_ERROR_CODE_VALUES.includes(errorCode)) {
    mappedError.domain = "user";
    return mappedError;
  }

  if (AUTH_ERROR_CODE_VALUES.includes(errorCode)) {
    mappedError.domain = "auth";

    if (ACCESS_TOKEN_ERROR_CODES_VALUES.includes(errorCode)) {
      mappedError.stage = options.stage || "access";
    }

    if (REFRESH_TOKEN_ERROR_CODES_VALUES.includes(errorCode)) {
      mappedError.stage = options.stage || "refresh";
      mappedError.shouldLogout = true;
    }

    if (errorCode === AUTH_ERROR_CODES.LOGIN_FAILED) {
      mappedError.stage = options.stage || "login";
    }

    return mappedError;
  }

  if (errorCode === COMMON_ERROR_CODES.INTERNAL_SERVER_ERROR) {
    mappedError.domain = "common";
    return mappedError;
  }

  return mappedError;
}
