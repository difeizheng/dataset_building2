import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value.roles || [])
  const permissions = computed(() => userInfo.value.permissions || [])

  async function login(username, password) {
    const res = await loginApi({ username, password })
    if (res.success) {
      token.value = res.data.token
      userInfo.value = res.data
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
    }
    return res
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  function hasPermission(perm) {
    return permissions.value.includes(perm) || roles.value.includes('admin')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    roles,
    permissions,
    login,
    logout,
    hasPermission
  }
})
