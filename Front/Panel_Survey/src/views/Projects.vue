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
                  <el-dropdown-item>个人信息</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </el-header>
      
      <el-container class="main-layout">
        <!-- 侧边栏 -->
        <el-aside width="260px" class="sidebar">
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
          <div class="page-header">
            <div>
              <h2 class="page-title">我的项目</h2>
              <p class="page-subtitle">管理您创建的所有问卷项目</p>
            </div>
            <el-button type="primary" size="large" round @click="$router.push('/project/create')">
              <el-icon style="margin-right: 8px"><Plus /></el-icon>
              创建新项目
            </el-button>
          </div>
          
          <el-card class="table-card" shadow="hover">
            <el-table
              v-loading="loading"
              :data="projects"
              style="width: 100%"
            >
              <el-table-column prop="projectName" label="项目名称" min-width="200" show-overflow-tooltip />
              <el-table-column prop="objectiveType" label="问卷目标" width="150" />
              <el-table-column prop="diseaseName" label="疾病名称" width="150" />
              <el-table-column prop="stepStatus" label="当前进度" width="100" align="center">
                <template #default="{ row }">
                  <el-tag type="primary" round>Step {{ row.stepStatus }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)" round>{{ getStatusText(row.status) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="420" fixed="right" align="center">
                <template #default="{ row }">
                  <el-button-group>
                    <!-- 已发布：显示链接按钮 -->
                    <el-button 
                      v-if="row.status === 'PUBLISHED' || row.status === 'COMPLETED'" 
                      type="success" 
                      size="small" 
                      @click="showLinkDialog(row)"
                    >
                      <el-icon><Link /></el-icon>
                      问卷链接
                    </el-button>
                    
                    <!-- 已发布：显示查看报告按钮 -->
                    <el-button 
                      v-if="row.status === 'PUBLISHED' || row.status === 'COMPLETED'" 
                      type="warning" 
                      size="small" 
                      @click="viewProject(row)"
                    >
                      <el-icon><DataAnalysis /></el-icon>
                      查看报告
                    </el-button>
                    
                    <!-- 未发布：显示继续按钮 -->
                    <el-button 
                      v-if="row.status !== 'PUBLISHED' && row.status !== 'COMPLETED'" 
                      type="primary" 
                      size="small" 
                      @click="continueProject(row)"
                    >
                      <el-icon><VideoPlay /></el-icon>
                      继续编辑
                    </el-button>
                    
                    <!-- 未发布：显示查看按钮 -->
                    <el-button 
                      v-if="row.status !== 'PUBLISHED' && row.status !== 'COMPLETED'" 
                      type="info" 
                      size="small" 
                      @click="viewProject(row)"
                    >
                      <el-icon><View /></el-icon>
                      查看详情
                    </el-button>
                    
                    <!-- 删除按钮 -->
                    <el-button type="danger" size="small" @click="deleteProject(row)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
    
    <!-- 链接和二维码对话框 -->
    <el-dialog
      v-model="linkDialogVisible"
      title="问卷链接"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="link-dialog-content">
        <el-alert
          title="问卷已发布"
          type="success"
          description="您可以通过以下方式分享问卷给受访者"
          :closable="false"
          show-icon
        />
        
        <div class="link-section">
          <h4>问卷链接</h4>
          <el-input
            v-model="currentSurveyUrl"
            readonly
            size="large"
          >
            <template #append>
              <el-button @click="copyLink">
                <el-icon><CopyDocument /></el-icon>
                复制链接
              </el-button>
            </template>
          </el-input>
          <el-alert 
            type="info" 
            :closable="false" 
            show-icon
            style="margin-top: 12px;"
          >
            <template #title>
              <span style="font-size: 13px;">
                ✅ 每个设备/IP仅可填写一次
              </span>
            </template>
          </el-alert>
        </div>
        
        <div class="stats-section">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="stat-item">
                <span class="stat-label">已收集回复</span>
                <span class="stat-value">{{ currentResponseCount }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <span class="stat-label">目标回复数</span>
                <span class="stat-value">{{ currentProject?.targetRespondents || 0 }}</span>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="linkDialogVisible = false">关闭</el-button>
        <el-button type="warning" @click="handleRepublish">
          <el-icon><Refresh /></el-icon>
          重新发布
        </el-button>
        <el-button type="primary" @click="openSurvey">
          <el-icon><View /></el-icon>
          预览问卷
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { House, Folder, Plus, VideoPlay, View, Delete, Link, CopyDocument, Refresh, DataAnalysis } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'
import { getUserProjects, deleteProject as deleteProjectApi } from '../api/project'
import { getQuestionnaireByProject, publishQuestionnaire, syncWenjuanData } from '../api/questionnaire'
import { getResponseCount } from '../api/response'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const projects = ref([])
const linkDialogVisible = ref(false)
const currentProject = ref(null)
const currentSurveyUrl = ref('')
const currentResponseCount = ref(0)

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const loadProjects = async () => {
  try {
    loading.value = true
    const res = await getUserProjects()
    projects.value = res.data
  } catch (error) {
    console.error('加载项目列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 继续编辑项目 - 跳转到下一步
const continueProject = (project) => {
  const step = project.stepStatus
  if (step === 1) {
    router.push(`/project/${project.id}/step2`)
  } else if (step === 2) {
    router.push(`/project/${project.id}/step3`)
  } else if (step === 3) {
    router.push(`/project/${project.id}/step4`)
  } else if (step === 4) {
    router.push(`/project/${project.id}/step5`)
  } else if (step === 5) {
    router.push(`/project/${project.id}/step6`)
  } else {
    router.push(`/project/${project.id}/step6`)
  }
}

// 查看项目 - 智能跳转
const viewProject = (project) => {
  // 如果已发布，直接跳转到分析报告页面（Step6）
  if (project.status === 'PUBLISHED' || project.status === 'COMPLETED') {
    router.push(`/project/${project.id}/step6`)
    return
  }
  
  // 如果未发布，根据当前进度查看
  // Step 1-4: 查看当前步骤
  // Step 5: 查看发布页面
  router.push(`/project/${project.id}/step${project.stepStatus}`)
}

const deleteProject = async (project) => {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteProjectApi(project.id)
    ElMessage.success('删除成功')
    loadProjects()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除项目失败:', error)
    }
  }
}

const getStatusType = (status) => {
  const map = {
    'DRAFT': 'info',
    'GENERATED': 'warning',
    'REVIEWED': 'primary',
    'PUBLISHED': 'success',
    'COMPLETED': 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    'DRAFT': '草稿',
    'GENERATED': '已生成',
    'REVIEWED': '已审核',
    'PUBLISHED': '已发布',
    'COMPLETED': '已完成'
  }
  return map[status] || status
}

// 显示链接对话框
const showLinkDialog = async (project) => {
  try {
    currentProject.value = project
    
    // 获取问卷信息
    const qRes = await getQuestionnaireByProject(project.id)
    const questionnaire = qRes.data
    
    if (!questionnaire) {
      ElMessage.warning('该项目还没有问卷')
      return
    }
    
    // 优先使用问卷网公网链接，如果没有则生成本地链接
    if (questionnaire.surveyUrl && questionnaire.surveyUrl.includes('wenjuan.com')) {
      // 使用问卷网公网链接
      currentSurveyUrl.value = questionnaire.surveyUrl
      
      // 如果已发布到问卷网，先同步最新数据
      if (questionnaire.wenjuanShortId) {
        try {
          console.log('开始同步问卷网数据，questionnaireId:', questionnaire.id, 'shortId:', questionnaire.wenjuanShortId)
          const syncRes = await syncWenjuanData(questionnaire.id)
          console.log('同步成功，返回数据:', syncRes)
          currentResponseCount.value = syncRes.data || 0
          ElMessage.success(`已同步问卷网数据，当前答卷数：${syncRes.data || 0}`)
        } catch (error) {
          console.error('同步问卷网数据失败:', error)
          ElMessage.warning('同步问卷网数据失败，显示缓存数据')
          currentResponseCount.value = questionnaire.wenjuanRespondentCount || 0
        }
      } else {
        console.warn('问卷未保存wenjuanShortId')
        currentResponseCount.value = questionnaire.wenjuanRespondentCount || 0
      }
    } else {
      // 生成本地问卷链接（测试用）
      const baseUrl = window.location.origin
      currentSurveyUrl.value = questionnaire.surveyUrl || `${baseUrl}/survey/${questionnaire.id}`
      
      // 获取本地回复数
      try {
        const rRes = await getResponseCount(questionnaire.id)
        currentResponseCount.value = rRes.data || 0
      } catch (error) {
        currentResponseCount.value = 0
      }
    }
    
    // 打开对话框
    linkDialogVisible.value = true
  } catch (error) {
    console.error('加载问卷信息失败:', error)
    ElMessage.error('加载问卷信息失败')
  }
}

// 复制链接
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(currentSurveyUrl.value)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = currentSurveyUrl.value
    document.body.appendChild(textarea)
    textarea.select()
    try {
      document.execCommand('copy')
      ElMessage.success('链接已复制到剪贴板')
    } catch (err) {
      ElMessage.error('复制失败，请手动复制')
    }
    document.body.removeChild(textarea)
  }
}

// 重新发布
const handleRepublish = async () => {
  try {
    await ElMessageBox.confirm(
      '重新发布将会更新问卷链接，之前的链接仍然有效。确认继续吗？',
      '重新发布问卷',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const qRes = await getQuestionnaireByProject(currentProject.value.id)
    const questionnaire = qRes.data
    
    if (!questionnaire) {
      ElMessage.warning('未找到问卷')
      return
    }
    
    // 重新发布
    await publishQuestionnaire(questionnaire.id, currentSurveyUrl.value)
    ElMessage.success('问卷已重新发布')
    
    // 刷新数据
    await showLinkDialog(currentProject.value)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重新发布失败:', error)
      ElMessage.error('重新发布失败')
    }
  }
}

// 预览问卷
const openSurvey = () => {
  window.open(currentSurveyUrl.value, '_blank')
}

onMounted(() => {
  loadProjects()
})
</script>

<style scoped>
.dashboard-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e9ecef 100%);
}

.layout-container {
  min-height: 100vh;
}

/* 顶部导航栏 */
.top-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  padding: 0;
  height: 56px;
  z-index: 1000;
  position: relative;
}

.top-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0) 0%, rgba(255, 255, 255, 0.3) 50%, rgba(255, 255, 255, 0) 100%);
}

.header-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.logo-section {
  display: flex;
  align-items: baseline;
  gap: 16px;
  cursor: pointer;
  transition: transform 0.3s;
}

.logo-section:hover {
  transform: scale(1.02);
}

.logo-title {
  font-size: 28px;
  font-weight: 800;
  color: white;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.logo-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.95);
  font-weight: 500;
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.user-dropdown:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.username {
  color: white;
  font-weight: 600;
  font-size: 15px;
}

/* 主布局 */
.main-layout {
  height: calc(100vh - 56px);
}

/* 侧边栏 */
.sidebar {
  background: white;
  box-shadow: 4px 0 16px rgba(0, 0, 0, 0.08);
  overflow-x: hidden;
  width: 200px !important;
}

.sidebar-menu {
  border: none;
  padding: 10px;
}

:deep(.sidebar-menu .el-menu-item) {
  border-radius: 10px;
  margin-bottom: 4px;
  height: 44px;
  line-height: 44px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
}

:deep(.sidebar-menu .el-menu-item:hover) {
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  color: #667eea;
  transform: translateX(4px);
}

:deep(.sidebar-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.sidebar-menu .el-menu-item .el-icon) {
  font-size: 20px;
}

/* 主内容区 */
.main-content {
  background: transparent;
  padding: 8px;
  overflow-y: auto;
  width: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 24px 28px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.page-title {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 6px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
  font-weight: 500;
}

:deep(.page-header .el-button) {
  height: 50px;
  padding: 0 32px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

:deep(.page-header .el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.table-card {
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  border: 2px solid rgba(102, 126, 234, 0.1);
  overflow: hidden;
}

:deep(.table-card .el-card__body) {
  padding: 0;
}

:deep(.el-table) {
  border-radius: 0;
  font-size: 15px;
}

:deep(.el-table th.el-table__cell) {
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  font-weight: 700;
  font-size: 15px;
  color: #2c3e50;
  padding: 16px 0;
}

:deep(.el-table .el-table__cell) {
  padding: 16px 0;
}

:deep(.el-table tbody tr:hover > td) {
  background: #f8f9ff !important;
}

:deep(.el-table__body tr) {
  transition: all 0.3s;
}

:deep(.el-tag) {
  font-weight: 600;
  padding: 6px 14px;
  border-radius: 12px;
  border: none;
}

:deep(.el-button-group) {
  display: flex;
  gap: 6px;
}

:deep(.el-button-group .el-button) {
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s;
  padding: 8px 14px;
}

:deep(.el-button-group .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.el-button-group .el-button--primary:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-button-group .el-button--info) {
  background: linear-gradient(135deg, #909399 0%, #606266 100%);
  border: none;
  color: white;
}

:deep(.el-button-group .el-button--info:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}

:deep(.el-button-group .el-button--danger) {
  background: linear-gradient(135deg, #f56c6c 0%, #f44336 100%);
  border: none;
}

:deep(.el-button-group .el-button--danger:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
}

:deep(.el-table__empty-block) {
  padding: 60px 0;
}

:deep(.el-table__empty-text) {
  font-size: 15px;
  color: #999;
}

/* 链接对话框样式 */
.link-dialog-content {
  padding: 10px 0;
}

.link-section,
.stats-section {
  margin-top: 24px;
}

.link-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}

:deep(.link-section .el-input-group__append) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.link-section .el-input-group__append .el-button) {
  color: white;
  font-weight: 600;
}

.stats-section {
  padding: 20px;
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  border-radius: 12px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

:deep(.el-button-group .el-button--success) {
  background: linear-gradient(135deg, #67c23a 0%, #52a328 100%);
  border: none;
  color: white;
}

:deep(.el-button-group .el-button--success:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
}

:deep(.el-button-group .el-button--warning) {
  background: linear-gradient(135deg, #e6a23c 0%, #d89614 100%);
  border: none;
  color: white;
}

:deep(.el-button-group .el-button--warning:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .header-wrapper {
    padding: 0 16px;
  }
  
  .logo-title {
    font-size: 22px;
  }
  
  .logo-subtitle {
    display: none;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 20px;
  }
  
  .page-title {
    font-size: 24px;
  }
  
  :deep(.page-header .el-button) {
    width: 100%;
  }
  
  :deep(.el-table) {
    font-size: 13px;
  }
  
  :deep(.el-button-group) {
    flex-direction: column;
    width: 100%;
  }
  
  :deep(.el-button-group .el-button) {
    width: 100%;
    margin: 0;
  }
}
</style>

