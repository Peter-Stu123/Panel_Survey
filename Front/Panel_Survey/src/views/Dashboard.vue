<template>
  <div class="dashboard-layout">
    <el-container class="layout-container">
      <!-- 顶部导航栏 -->
      <el-header class="top-header">
        <div class="header-wrapper">
          <div class="logo-section">
            <h1 class="logo-title">GuidePref</h1>
            <span class="logo-subtitle">问卷平台</span>
          </div>
          
          <div class="header-actions">
            <el-dropdown trigger="click">
              <div class="user-dropdown">
                <el-avatar :size="36" style="background-color: #409EFF">
                  {{ userStore.user?.username?.charAt(0).toUpperCase() }}
                </el-avatar>
                <span class="username">{{ userStore.user?.username }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showProfileDialog = true">个人信息</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-container class="main-layout">
        <!-- 侧边栏 -->
        <el-aside width="160px" class="sidebar">
          <el-menu
            :default-active="$route.path"
            router
            class="sidebar-menu"
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>仪表盘</span>
            </el-menu-item>
            <el-menu-item index="/projects">
              <el-icon><Folder /></el-icon>
              <span>我的项目</span>
            </el-menu-item>
            <el-menu-item index="/project/create">
              <el-icon><Plus /></el-icon>
              <span>创建项目</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <!-- 主内容区 -->
        <el-main class="main-content">
          <!-- 欢迎区域 -->
          <div class="welcome-banner">
            <h2 class="banner-title">欢迎使用 GuidePref</h2>
            <p class="banner-desc">临床实践指南患者偏好问卷生成与分析平台</p>
          </div>
          
          <!-- 统计卡片 -->
          <div class="stats-grid">
            <div class="stat-card stat-card-blue" @click="$router.push('/projects')">
              <div class="stat-card-inner">
                <div class="stat-icon-wrapper">
                  <el-icon class="stat-icon-large"><DocumentCopy /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stats.projectCount }}</div>
                  <div class="stat-label">我的项目</div>
                  <div class="stat-desc">管理所有问卷项目</div>
                </div>
              </div>
            </div>
            
            <div class="stat-card stat-card-green" @click="$router.push('/projects')">
              <div class="stat-card-inner">
                <div class="stat-icon-wrapper">
                  <el-icon class="stat-icon-large"><EditPen /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stats.surveyCount }}</div>
                  <div class="stat-label">已发布问卷</div>
                  <div class="stat-desc">正在收集数据</div>
                </div>
              </div>
            </div>
            
            <div class="stat-card stat-card-purple" @click="$router.push('/projects')">
              <div class="stat-card-inner">
                <div class="stat-icon-wrapper">
                  <el-icon class="stat-icon-large"><DataAnalysis /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">{{ stats.responseCount }}</div>
                  <div class="stat-label">收集回复</div>
                  <div class="stat-desc">来自受访者</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 快速开始 -->
          <el-card class="workflow-card" shadow="hover">
            <template #header>
              <div class="workflow-header">
                <div>
                  <h3>工作流程</h3>
                  <p class="workflow-subtitle">6个步骤完成问卷项目</p>
                </div>
                <el-tag type="primary" effect="plain">简单高效</el-tag>
              </div>
            </template>
            
            <div class="workflow-content">
              <div class="workflow-steps">
                <div class="workflow-step">
                  <div class="step-number">1</div>
                  <div class="step-info">
                    <h4>选择目标</h4>
                    <p>确定问卷目标类型</p>
                  </div>
                </div>
                
                <div class="workflow-step">
                  <div class="step-number">2</div>
                  <div class="step-info">
                    <h4>填写信息</h4>
                    <p>输入疾病和干预措施</p>
                  </div>
                </div>
                
                <div class="workflow-step">
                  <div class="step-number">3</div>
                  <div class="step-info">
                    <h4>生成问卷</h4>
                    <p>自动创建问卷内容</p>
                  </div>
                </div>
                
                <div class="workflow-step">
                  <div class="step-number">4</div>
                  <div class="step-info">
                    <h4>AI审核</h4>
                    <p>智能润色优化内容</p>
                  </div>
                </div>
                
                <div class="workflow-step">
                  <div class="step-number">5</div>
                  <div class="step-info">
                    <h4>发布问卷</h4>
                    <p>生成分发链接</p>
                  </div>
                </div>
                
                <div class="workflow-step">
                  <div class="step-number">6</div>
                  <div class="step-info">
                    <h4>分析报告</h4>
                    <p>生成数据分析报告</p>
                  </div>
                </div>
              </div>
              
              <div class="workflow-action">
                <el-button type="primary" size="large" round @click="$router.push('/project/create')">
                  <el-icon><Plus /></el-icon>
                  <span>开始创建项目</span>
                </el-button>
                <el-button size="large" round @click="$router.push('/projects')">
                  <el-icon><Folder /></el-icon>
                  <span>查看已有项目</span>
                </el-button>
              </div>
            </div>
          </el-card>
          
          <!-- 最近项目和快速操作 -->
          <div class="bottom-grid">
            <!-- 最近项目 -->
            <el-card class="recent-projects-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <h3>最近项目</h3>
                  <el-link type="primary" :underline="false" @click="$router.push('/projects')">
                    查看全部 <el-icon><ArrowRight /></el-icon>
                  </el-link>
                </div>
              </template>
              
              <div v-if="recentProjects.length > 0" class="projects-list">
                <div
                  v-for="project in recentProjects"
                  :key="project.id"
                  class="project-item"
                  @click="handleProjectClick(project)"
                >
                  <div class="project-icon">
                    <el-icon><DocumentCopy /></el-icon>
                  </div>
                  <div class="project-details">
                    <h4>{{ project.projectName }}</h4>
                    <p>{{ formatDate(project.createTime) }}</p>
                  </div>
                  <el-tag :type="getStatusType(project.status)" size="small">
                    {{ getStatusText(project.status) }}
                  </el-tag>
                </div>
              </div>
              
              <el-empty
                v-else
                description="暂无项目"
                :image-size="100"
              >
                <el-button type="primary" round @click="$router.push('/project/create')">
                  创建第一个项目
                </el-button>
              </el-empty>
            </el-card>
            
            <!-- 快速操作 -->
            <el-card class="quick-actions-card" shadow="hover">
              <template #header>
                <div class="card-header">
                  <h3>快速操作</h3>
                </div>
              </template>
              
              <div class="quick-actions-grid">
                <div class="action-item" @click="$router.push('/project/create')">
                  <div class="action-icon action-icon-blue">
                    <el-icon><Plus /></el-icon>
                  </div>
                  <div class="action-text">
                    <h4>创建项目</h4>
                    <p>开始新的问卷</p>
                  </div>
                </div>
                
                <div class="action-item" @click="$router.push('/projects')">
                  <div class="action-icon action-icon-green">
                    <el-icon><Folder /></el-icon>
                  </div>
                  <div class="action-text">
                    <h4>我的项目</h4>
                    <p>查看所有项目</p>
                  </div>
                </div>
                
                <div class="action-item" @click="showHelpDialog = true">
                  <div class="action-icon action-icon-orange">
                    <el-icon><QuestionFilled /></el-icon>
                  </div>
                  <div class="action-text">
                    <h4>帮助中心</h4>
                    <p>使用指南</p>
                  </div>
                </div>
                
                <div class="action-item" @click="showProfileDialog = true">
                  <div class="action-icon action-icon-purple">
                    <el-icon><User /></el-icon>
                  </div>
                  <div class="action-text">
                    <h4>个人中心</h4>
                    <p>账号设置</p>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 个人信息对话框 -->
    <el-dialog
      v-model="showProfileDialog"
      title="个人信息"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="profileForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="注册时间">
          <el-input v-model="profileForm.createTime" disabled />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </template>
    </el-dialog>
    
    <!-- 帮助中心对话框 -->
    <el-dialog
      v-model="showHelpDialog"
      title="帮助中心"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="help-content">
        <el-collapse v-model="activeHelp" accordion>
          <el-collapse-item title="如何创建问卷项目？" name="1">
            <div class="help-text">
              <p>1. 点击侧边栏的"创建项目"或首页的"开始创建项目"按钮</p>
              <p>2. 选择问卷目标类型（效果研究或患者偏好）</p>
              <p>3. 填写项目基本信息（疾病名称、干预措施等）</p>
              <p>4. 系统自动生成问卷内容</p>
              <p>5. 使用AI审核优化问卷内容</p>
              <p>6. 发布问卷并获取分享链接</p>
            </div>
          </el-collapse-item>
          
          <el-collapse-item title="如何分发问卷？" name="2">
            <div class="help-text">
              <p>1. 在"我的项目"中找到已发布的问卷</p>
              <p>2. 点击"分享"按钮获取问卷链接</p>
              <p>3. 通过二维码或链接分享给受访者</p>
              <p>4. 受访者填写完成后数据会自动收集</p>
            </div>
          </el-collapse-item>
          
          <el-collapse-item title="如何查看和分析数据？" name="3">
            <div class="help-text">
              <p>1. 进入项目详情页面</p>
              <p>2. 点击"查看报告"查看统计数据</p>
              <p>3. 系统会自动生成可视化图表和分析报告</p>
              <p>4. 支持导出数据和报告</p>
            </div>
          </el-collapse-item>
          
          <el-collapse-item title="常见问题" name="4">
            <div class="help-text">
              <p><strong>Q: 问卷可以修改吗？</strong></p>
              <p>A: 未发布的问卷可以随时编辑，已发布的问卷不建议修改以保证数据一致性。</p>
              <br />
              <p><strong>Q: 数据安全吗？</strong></p>
              <p>A: 所有数据都经过加密存储，只有您能访问自己的项目数据。</p>
              <br />
              <p><strong>Q: 支持多少份回复？</strong></p>
              <p>A: 没有限制，您可以收集任意数量的问卷回复。</p>
            </div>
          </el-collapse-item>
        </el-collapse>
      </div>
      <template #footer>
        <el-button type="primary" @click="showHelpDialog = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  House, Folder, Plus, DocumentCopy, EditPen, DataAnalysis,
  ArrowRight, QuestionFilled, User
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getUserProjects } from '../api/project'
import { getQuestionnaireByProject, syncWenjuanData } from '../api/questionnaire'

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = ref({
  projectCount: 0,
  surveyCount: 0,
  responseCount: 0
})

// 最近项目
const recentProjects = ref([])

// 对话框控制
const showProfileDialog = ref(false)
const showHelpDialog = ref(false)
const activeHelp = ref('1')

// 个人信息表单
const profileForm = ref({
  username: '',
  email: '',
  phone: '',
  createTime: ''
})

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

// 加载统计数据
const loadStats = async () => {
  try {
    const res = await getUserProjects()
    const projects = res.data || []
    stats.value.projectCount = projects.length
    stats.value.surveyCount = projects.filter(p => p.status === 'PUBLISHED').length
    
    // 计算总回复数 - 优先使用问卷网数据
    let totalResponseCount = 0
    
    for (const project of projects) {
      if (project.status === 'PUBLISHED') {
        try {
          // 获取问卷信息
          const qRes = await getQuestionnaireByProject(project.id)
          const questionnaire = qRes.data
          
          if (questionnaire && questionnaire.wenjuanShortId) {
            // 如果已发布到问卷网，同步最新数据
            try {
              console.log(`同步项目${project.id}的问卷网数据，shortId:`, questionnaire.wenjuanShortId)
              const syncRes = await syncWenjuanData(questionnaire.id)
              console.log(`项目${project.id}同步成功，答卷数:`, syncRes.data)
              totalResponseCount += (syncRes.data || 0)
            } catch (error) {
              console.warn(`同步项目${project.id}的问卷网数据失败:`, error)
              totalResponseCount += (questionnaire.wenjuanRespondentCount || 0)
            }
          } else {
            // 使用本地数据
            console.log(`项目${project.id}使用本地数据`)
            totalResponseCount += (project.responseCount || 0)
          }
        } catch (error) {
          console.warn(`获取项目${project.id}的问卷信息失败:`, error)
        }
      }
    }
    
    stats.value.responseCount = totalResponseCount
    console.log('Dashboard总回复数:', totalResponseCount)
    
    // 获取最近3个项目
    recentProjects.value = projects
      .sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      .slice(0, 3)
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

// 加载个人信息
const loadProfile = () => {
  profileForm.value.username = userStore.user?.username || ''
  profileForm.value.email = userStore.user?.email || ''
  profileForm.value.phone = userStore.user?.phone || ''
  profileForm.value.createTime = userStore.user?.createTime 
    ? formatDate(userStore.user.createTime) 
    : ''
}

// 保存个人信息
const saveProfile = () => {
  // 这里可以调用API保存用户信息
  ElMessage.success('个人信息已保存')
  showProfileDialog.value = false
}

// 处理项目点击
const handleProjectClick = (project) => {
  router.push(`/projects`)
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    'DRAFT': 'info',
    'PUBLISHED': 'success',
    'CLOSED': 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PUBLISHED': '已发布',
    'CLOSED': '已关闭'
  }
  return statusMap[status] || '未知'
}

// 监听个人信息对话框打开
const handleProfileDialogOpen = () => {
  loadProfile()
}

onMounted(() => {
  loadStats()
  loadProfile()
})
</script>

<style scoped>
* {
  box-sizing: border-box;
}

.dashboard-layout {
  height: 100vh;
  width: 100vw;
  background: #f0f2f5;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden !important;
  position: relative;
}

.layout-container {
  height: 100vh;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden !important;
}

/* 顶部导航栏 */
.top-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0;
  height: 56px;
  z-index: 1000;
}

.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px 0 12px;
}

.logo-section {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.logo-title {
  font-size: 24px;
  font-weight: 700;
  color: white;
  margin: 0;
  letter-spacing: 0.5px;
}

.logo-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 400;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.1);
}

.username {
  color: white;
  font-weight: 500;
}

/* 主布局 */
.main-layout {
  height: calc(100vh - 56px);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden !important;
}

/* 侧边栏 */
.sidebar {
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  overflow: hidden !important;
  width: 160px !important;
  min-width: 160px !important;
  max-width: 160px !important;
  flex-shrink: 0;
}

.sidebar-menu {
  border: none;
  padding: 12px 8px;
}

.sidebar-menu .el-menu-item {
  border-radius: 8px;
  margin-bottom: 8px;
  height: 48px;
  line-height: 48px;
  font-size: 14px;
  padding-left: 16px !important;
}

.sidebar-menu .el-menu-item:hover {
  background: #f0f2f5;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

/* 主内容区 */
.main-content {
  background: #f0f2f5;
  padding: 20px !important;
  overflow-y: auto !important;
  overflow-x: hidden !important;
  width: 100% !important;
  max-width: 100% !important;
  box-sizing: border-box;
  flex: 1;
  height: 100%;
}

:deep(.el-main) {
  padding: 20px !important;
  overflow-y: auto !important;
  overflow-x: hidden !important;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 32px 24px;
  margin-bottom: 20px;
  text-align: center;
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.35);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.banner-title {
  font-size: 32px;
  font-weight: 800;
  color: white;
  margin: 0 0 8px 0;
  letter-spacing: 1px;
}

.banner-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  margin: 0;
  font-weight: 500;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 0;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  width: 100%;
}

.stat-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  transition: height 0.3s ease;
}

.stat-card-blue::before {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card-green::before {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.stat-card-purple::before {
  background: linear-gradient(135deg, #ee0979 0%, #ff6a00 100%);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.18);
}

.stat-card:hover::before {
  height: 6px;
}

.stat-card-inner {
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
}

.stat-icon-wrapper {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.stat-card-blue .stat-icon-wrapper {
  background: linear-gradient(135deg, #667eea20 0%, #764ba240 100%);
}

.stat-card-green .stat-icon-wrapper {
  background: linear-gradient(135deg, #11998e20 0%, #38ef7d40 100%);
}

.stat-card-purple .stat-icon-wrapper {
  background: linear-gradient(135deg, #ee097920 0%, #ff6a0040 100%);
}

.stat-icon-large {
  font-size: 32px;
}

.stat-card-blue .stat-icon-large {
  color: #667eea;
}

.stat-card-green .stat-icon-large {
  color: #11998e;
}

.stat-card-purple .stat-icon-large {
  color: #ee0979;
}

.stat-content {
  width: 100%;
}

.stat-value {
  font-size: 40px;
  font-weight: 800;
  color: #333;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-label {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 3px;
}

.stat-desc {
  font-size: 12px;
  color: #999;
}

/* 工作流程卡片 */
.workflow-card {
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  margin-bottom: 20px;
}

:deep(.workflow-card .el-card__header) {
  padding: 20px 24px;
}

:deep(.workflow-card .el-card__body) {
  padding: 20px 24px;
}

.workflow-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.workflow-header h3 {
  font-size: 22px;
  font-weight: 800;
  color: #333;
  margin: 0 0 4px 0;
}

.workflow-subtitle {
  font-size: 13px;
  color: #666;
  margin: 0;
  font-weight: 500;
}

:deep(.workflow-header .el-tag) {
  font-size: 12px;
  padding: 4px 12px;
}

.workflow-content {
  padding: 0;
}

.workflow-steps {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
  margin-bottom: 20px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

.workflow-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 10px;
  padding: 18px 10px;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.3s;
  min-width: 0;
}

.workflow-step:hover {
  background: #e9ecef;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.step-number {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.35);
}

.step-info {
  width: 100%;
  min-width: 0;
}

.step-info h4 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
  word-break: keep-all;
}

.step-info p {
  font-size: 12px;
  color: #666;
  margin: 0;
  line-height: 1.4;
  word-break: keep-all;
}

.workflow-action {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding-top: 20px;
  border-top: 2px solid #e9ecef;
  flex-wrap: wrap;
}

.workflow-action .el-button {
  min-width: 180px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 22px;
  padding: 0 24px;
}

/* 底部网格布局 */
.bottom-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

/* 最近项目卡片 */
.recent-projects-card,
.quick-actions-card {
  border-radius: 10px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
}

:deep(.recent-projects-card .el-card__header),
:deep(.quick-actions-card .el-card__header) {
  padding: 20px 24px;
  border-bottom: 2px solid #e9ecef;
}

:deep(.recent-projects-card .el-card__body),
:deep(.quick-actions-card .el-card__body) {
  padding: 20px 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0;
}

.card-header .el-link {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 项目列表 */
.projects-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.project-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-radius: 8px;
  background: #f8f9fa;
  transition: all 0.3s;
  cursor: pointer;
}

.project-item:hover {
  background: #e9ecef;
  transform: translateX(4px);
}

.project-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.project-icon .el-icon {
  font-size: 24px;
  color: white;
}

.project-details {
  flex: 1;
  min-width: 0;
}

.project-details h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.project-details p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

/* 快速操作网格 */
.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 10px;
  background: #f8f9fa;
  transition: all 0.3s;
  cursor: pointer;
}

.action-item:hover {
  background: #e9ecef;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.action-icon .el-icon {
  font-size: 28px;
  color: white;
}

.action-icon-blue {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.action-icon-green {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.action-icon-orange {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.action-icon-purple {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.action-text {
  flex: 1;
}

.action-text h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 4px 0;
}

.action-text p {
  font-size: 13px;
  color: #666;
  margin: 0;
}

/* 帮助内容 */
.help-content {
  max-height: 500px;
  overflow-y: auto;
}

.help-text {
  line-height: 1.8;
}

.help-text p {
  margin: 8px 0;
  color: #333;
}

.help-text strong {
  color: #667eea;
}

/* 响应式 */
@media (max-width: 1400px) {
  .workflow-steps {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .workflow-steps {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .bottom-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .logo-subtitle {
    display: none;
  }
  
  .main-content {
    padding: 16px !important;
  }
  
  .welcome-banner {
    padding: 24px 16px;
  }
  
  .banner-title {
    font-size: 24px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .workflow-steps {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    margin-bottom: 12px;
  }
  
  .bottom-grid {
    grid-template-columns: 1fr;
  }
  
  .quick-actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>

