import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/Dashboard.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/projects',
    name: 'Projects',
    component: () => import('../views/Projects.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/create',
    name: 'CreateProject',
    component: () => import('../views/project/Step1.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/step2',
    name: 'ProjectStep2',
    component: () => import('../views/project/Step2.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/step3',
    name: 'ProjectStep3',
    component: () => import('../views/project/Step3.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/step4',
    name: 'ProjectStep4',
    component: () => import('../views/project/Step4.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/step5',
    name: 'ProjectStep5',
    component: () => import('../views/project/Step5.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/project/:id/step6',
    name: 'ProjectStep6',
    component: () => import('../views/project/Step6.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/survey/:id',
    name: 'SurveyFill',
    component: () => import('../views/SurveyFill.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  console.log('[Router] 导航:', from.path, '->', to.path, '| requiresAuth:', to.meta.requiresAuth, '| isLoggedIn:', userStore.isLoggedIn())
  
  if (to.meta.requiresAuth && !userStore.isLoggedIn()) {
    console.log('[Router] 需要认证但未登录，重定向到登录页')
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && userStore.isLoggedIn()) {
    console.log('[Router] 已登录用户访问登录/注册页，重定向到仪表盘')
    next('/dashboard')
  } else {
    console.log('[Router] 允许导航')
    next()
  }
})

export default router

