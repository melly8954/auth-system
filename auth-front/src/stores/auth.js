import { defineStore } from 'pinia'
import * as sessionAuthClient from '../api/auth/sessionAuthClient'
import * as jwtAuthClient from '../api/auth/jwtAuthClient'

const STORAGE_KEY = 'auth-system-mode'

function normalizeUserId(response) {
  return response?.data ?? null
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    authMode: import.meta.env.VITE_AUTH_MODE || 'session',
    userId: null,
    isAuthenticated: false,
    isLoading: false,
    errorMessage: '',
  }),

  getters: {
    activeClient(state) {
      return state.authMode === 'jwt' ? jwtAuthClient : sessionAuthClient
    },
    accessToken() {
      return jwtAuthClient.getAccessToken()
    },
  },

  actions: {
    async bootstrap() {
      const savedMode = localStorage.getItem(STORAGE_KEY)

      if (savedMode === 'session' || savedMode === 'jwt') {
        this.authMode = savedMode
      }

      await this.restoreSession()
    },

    async setAuthMode(mode) {
      if (mode !== 'session' && mode !== 'jwt') {
        return
      }

      if (this.authMode === mode) {
        return
      }

      this.authMode = mode
      localStorage.setItem(STORAGE_KEY, mode)
      this.resetState()
      await this.restoreSession()
    },

    async signUp(payload) {
      this.isLoading = true
      this.errorMessage = ''

      try {
        return await this.activeClient.signUp(payload)
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
        const response = await this.activeClient.login(payload)
        await this.restoreSession()
        return response
      } catch (error) {
        this.errorMessage = error.message
        this.isAuthenticated = false
        this.userId = null
        throw error
      } finally {
        this.isLoading = false
      }
    },

    async logout() {
      this.isLoading = true
      this.errorMessage = ''

      try {
        await this.activeClient.logout()
      } catch (error) {
        this.errorMessage = error.message
      } finally {
        this.resetState()
        this.isLoading = false
      }
    },

    async restoreSession() {
      this.isLoading = true
      this.errorMessage = ''

      try {
        if (this.authMode === 'jwt' && !this.accessToken) {
          await jwtAuthClient.reissueToken()
        }

        const response = await this.activeClient.fetchUser()
        this.userId = normalizeUserId(response)
        this.isAuthenticated = this.userId !== null
      } catch (error) {
        if (this.authMode === 'jwt') {
          jwtAuthClient.clearAccessToken()
        }

        this.userId = null
        this.isAuthenticated = false
      } finally {
        this.isLoading = false
      }
    },

    resetState() {
      this.userId = null
      this.isAuthenticated = false
      this.errorMessage = ''
      jwtAuthClient.clearAccessToken()
    },
  },
})
