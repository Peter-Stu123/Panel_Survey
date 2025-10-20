<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>GuidePref</h1>
        <p>临床指南患者偏好问卷平台</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="rules"
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-button
          type="primary"
          size="large"
          :loading="loading"
          class="login-button"
          @click="handleLogin"
        >
          登录
        </el-button>
      </el-form>
      
      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="register-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { login } from '../api/auth'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    console.log('[Login] 开始登录:', loginForm.username)
    const res = await login(loginForm)
    console.log('[Login] 登录响应:', res)
    
    if (res && res.data) {
      console.log('[Login] Token:', res.data.token ? res.data.token.substring(0, 20) + '...' : 'null')
      console.log('[Login] User:', res.data.user)
      
      userStore.setToken(res.data.token)
      userStore.setUser(res.data.user)
      
      // 验证是否存储成功
      const storedToken = localStorage.getItem('token')
      const storedUser = localStorage.getItem('user')
      console.log('[Login] 验证存储 - Token:', storedToken ? storedToken.substring(0, 20) + '...' : 'null')
      console.log('[Login] 验证存储 - User:', storedUser)
      
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } else {
      console.error('[Login] 响应数据异常:', res)
      ElMessage.error('登录失败：响应数据异常')
    }
  } catch (error) {
    console.error('[Login] 登录失败:', error)
    ElMessage.error('登录失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 8px;
  position: relative;
  overflow: hidden;
}

.login-container::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  top: -300px;
  right: -200px;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  bottom: -200px;
  left: -100px;
}

.login-box {
  width: 100%;
  max-width: 1200px;
  padding: 40px 50px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-header h1 {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 8px;
}

.login-header p {
  font-size: 16px;
  color: #666;
  line-height: 1.5;
  font-weight: 500;
}

.login-form {
  margin-top: 24px;
}

:deep(.el-form-item) {
  margin-bottom: 28px;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  border-radius: 12px;
  padding: 12px 16px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

.login-button {
  width: 100%;
  margin-top: 12px;
  height: 50px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s;
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 13px;
  color: #666;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  margin-left: 5px;
  font-weight: 600;
  transition: color 0.3s;
}

.register-link:hover {
  color: #764ba2;
}

@media (max-width: 768px) {
  .login-box {
    padding: 32px 24px;
  }
  
  .login-header h1 {
    font-size: 28px;
  }
}
</style>

