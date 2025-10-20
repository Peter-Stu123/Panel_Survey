import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 初始化时从localStorage读取
  const storedToken = localStorage.getItem('token')
  const storedUser = localStorage.getItem('user')
  
  const token = ref(storedToken || '')
  const user = ref(storedUser ? JSON.parse(storedUser) : null)
  
  console.log('[User Store] 初始化:', { 
    hasToken: !!token.value, 
    tokenPreview: token.value ? token.value.substring(0, 20) + '...' : 'null',
    user: user.value 
  })

  const setToken = (newToken) => {
    console.log('[User Store] setToken:', newToken ? newToken.substring(0, 20) + '...' : 'null')
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUser = (newUser) => {
    console.log('[User Store] setUser:', newUser)
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  const logout = () => {
    console.log('[User Store] logout')
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const isLoggedIn = () => {
    const loggedIn = !!token.value
    console.log('[User Store] isLoggedIn:', loggedIn)
    return loggedIn
  }

  return {
    token,
    user,
    setToken,
    setUser,
    logout,
    isLoggedIn
  }
})

