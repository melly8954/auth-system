import { defineStore } from 'pinia'
import * as jwtAuthClient from '../api/auth/jwtAuthClient'
import { getApiErrorCode, getApiResult } from '../api/http'
import { JWT_ERROR_CODES } from '../api/errorCodes'

function isRefreshTokenFailure(error) {
  const errorCode = getApiErrorCode(error)
  return (
    errorCode === JWT_ERROR_CODES.REFRESH_TOKEN_NOT_FOUND ||
    errorCode === JWT_ERROR_CODES.REFRESH_TOKEN_EXPIRED ||
    errorCode === JWT_ERROR_CODES.REFRESH_TOKEN_INVALID ||
    errorCode === JWT_ERROR_CODES.REFRESH_TOKEN_NOT_IN_REDIS
  )
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    userId: null,
    accessToken: '',
    tokenVerified: false,
    isAuthenticated: false,
    isLoading: false,
    errorMessage: '',
  }),

  actions: {
    syncAccessToken() {
      this.accessToken = jwtAuthClient.getAccessToken()
      this.isAuthenticated = !!this.accessToken
    },

    applyLoginResult(response) {
      const result = getApiResult(response)

      this.accessToken = result?.accessToken || jwtAuthClient.getAccessToken()
      this.isAuthenticated = !!this.accessToken
      this.tokenVerified = false
    },

    applyVerifyResult(response) {
      const result = getApiResult(response)
      this.userId = result?.userId ?? null
      this.tokenVerified = !!result?.verified
      this.syncAccessToken()
    },

    async bootstrap() {
      this.isLoading = true
      this.errorMessage = ''

      try {
        const reissueResponse = await jwtAuthClient.reissueToken()
        const reissueResult = getApiResult(reissueResponse)

        this.accessToken = reissueResult?.newAccessToken || jwtAuthClient.getAccessToken()
        this.isAuthenticated = !!this.accessToken
        this.tokenVerified = false
      } catch (error) {
        if (!isRefreshTokenFailure(error)) {
          this.errorMessage = error.message || ''
        }

        this.resetState()
      } finally {
        this.isLoading = false
      }
    },

    async signUp(payload) {
      this.isLoading = true
      this.errorMessage = ''

      try {
        return await jwtAuthClient.signUp(payload)
      } catch (error) {
        this.errorMessage = error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async login(payload) {
      this.isLoading = true
      this.errorMessage = ''

      try {
        const response = await jwtAuthClient.login(payload)
        this.applyLoginResult(response)
        return response
      } catch (error) {
        this.resetState()
        this.errorMessage = error.message
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async verifyAccessToken() {
      this.isLoading = true
      this.errorMessage = ''

      try {
        const response = await jwtAuthClient.fetchUser()
        this.applyVerifyResult(response)
        return response
      } catch (error) {
        this.errorMessage = error.message || ''
        this.tokenVerified = false
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async logout() {
      this.isLoading = true
      this.errorMessage = ''

      try {
        await jwtAuthClient.logout()
      } catch (error) {
        this.errorMessage = error.message
      } finally {
        this.resetState()
        this.isLoading = false
      }
    },

    resetState() {
      this.userId = null
      this.accessToken = ''
      this.tokenVerified = false
      this.isAuthenticated = false
      this.errorMessage = ''
      jwtAuthClient.clearAccessToken()
    },
  },
})
