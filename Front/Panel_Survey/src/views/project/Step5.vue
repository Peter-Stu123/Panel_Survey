<template>
  <!-- 问卷内容导出页面（全屏） -->
  <div v-if="showExportPage" class="export-page-wrapper">
    <WenjuanExport
      :questionnaireId="questionnaire.id"
      :wenjuanResult="wenjuanInfo"
      @close="showExportPage = false"
    />
  </div>
  
  <!-- 原有的发布页面 -->
  <div v-else class="step-container">
    <el-card class="step-card">
      <template #header>
        <div class="card-header">
          <h2>Step 5: 发布问卷</h2>
          <el-button type="text" @click="handleBack">返回上一步</el-button>
        </div>
      </template>
      
      <div v-if="!questionnaire" class="loading-section">
        <el-empty description="正在加载问卷..." />
      </div>
      
      <div v-else class="publish-section">
        <el-alert
          title="发布提示"
          type="info"
          description="问卷发布后，受访者可通过链接访问并填写问卷"
          :closable="false"
          show-icon
        />
        
        <div class="publish-form">
          <el-form label-width="120px">
            <el-form-item label="问卷标题">
              <el-input v-model="questionnaire.title" disabled />
            </el-form-item>
            
            <el-form-item label="问卷说明">
              <el-input v-model="questionnaire.description" type="textarea" :rows="2" disabled />
            </el-form-item>
            
            <el-form-item label="问卷链接">
              <el-input v-model="surveyUrl" placeholder="请点击下方按钮生成公网链接" readonly>
                <template #prepend>
                  <span style="color: #67C23A;">🌐 公网链接</span>
                </template>
              </el-input>
              <div v-if="surveyUrl && wenjuanInfo" style="margin-top: 8px;">
                <el-alert type="success" :closable="false" show-icon>
                  <template #title>
                    <span style="font-size: 13px;">
                      ✅ 公共链接已生成，支持多人多次填写
                      <br>
                      📱 支持微信/浏览器扫码填写，也可直接分享链接
                      <br>
                      💡 添加题目并发布后，受访者即可通过此链接填写问卷
                    </span>
                  </template>
                </el-alert>
              </div>
              <div style="margin-top: 12px; display: flex; gap: 12px;">
                <el-button 
                  type="success" 
                  :loading="publishingToWenjuan"
                  @click="handlePublishToWenjuan"
                  :icon="'Globe'"
                  style="flex: 1;"
                >
                  发布到问卷网（公网访问）
                </el-button>
                <el-button 
                  @click="generateLocalUrl"
                  :icon="'Link'"
                  style="flex: 1;"
                >
                  生成本地链接（测试用）
                </el-button>
              </div>
              <el-alert
                v-if="wenjuanInfo"
                type="warning"
                :closable="false"
                style="margin-top: 12px;"
              >
                <template #title>
                  <div style="font-weight: bold;">⚠️ 问卷已创建到问卷网，需要手动添加题目</div>
                </template>
                <div style="margin-top: 8px; line-height: 1.8;">
                  <p style="color: #E6A23C; font-weight: bold; font-size: 14px;">📌 由于问卷网API限制，题目需要手动添加</p>
                  
                  <div style="margin: 12px 0; padding: 12px; background: #FEF0F0; border-radius: 8px;">
                    <p style="font-weight: bold; margin-bottom: 8px;">🔗 快速访问</p>
                    <el-button 
                      type="warning" 
                      size="small"
                      @click="showExportPage = true"
                      style="margin-right: 8px;"
                    >
                      📋 打开智能导出页面（推荐）
                    </el-button>
                    <el-button 
                      v-if="wenjuanInfo.manageUrl" 
                      type="primary" 
                      size="small"
                      @click="openWenjuanEdit"
                      style="margin-right: 8px;"
                    >
                      🌐 打开问卷网编辑器
                    </el-button>
                    <el-button 
                      type="success" 
                      size="small"
                      @click="showQuestionDialog = true"
                    >
                      👁️ 查看题目列表
                    </el-button>
                  </div>
                  
                  <div style="margin: 12px 0; padding: 12px; background: #FFF7E6; border-radius: 8px;">
                    <p style="font-weight: bold; margin-bottom: 8px;">📋 操作步骤</p>
                    <ol style="margin: 0; padding-left: 20px; line-height: 2;">
                      <li>点击上方"打开问卷网编辑页面"按钮（或访问 <a href="https://www.wenjuan.com" target="_blank">www.wenjuan.com</a>）</li>
                      <li>登录后找到项目（ID: <code style="color: #F56C6C;">{{ wenjuanInfo.surveyId }}</code>）</li>
                      <li>点击"查看题目内容"按钮，复制AI润色后的题目</li>
                      <li>在问卷网编辑页面中添加题目并保存</li>
                      <li>添加完成后点击"发布"，完成问卷发布</li>
                    </ol>
                  </div>
                  
                  <div style="margin-top: 12px; padding-top: 12px; border-top: 1px dashed #E6A23C;">
                    <p><strong>📱 问卷链接：</strong></p>
                    <el-input 
                      :value="wenjuanInfo.surveyUrl" 
                      readonly 
                      size="small"
                      style="margin-top: 4px;"
                    >
                      <template #append>
                        <el-button @click="copyUrl(wenjuanInfo.surveyUrl)">复制</el-button>
                      </template>
                    </el-input>
                    <p style="font-size: 12px; color: #909399; margin-top: 4px;">
                      （公共链接格式，支持多人填写。添加题目并发布后即可使用）
                    </p>
                  </div>
                </div>
              </el-alert>
            </el-form-item>
            
            <el-form-item v-if="questionnaire.status === 'PUBLISHED'" label="收集情况">
              <el-progress
                :percentage="responsePercentage"
                :status="responsePercentage >= 100 ? 'success' : ''"
              />
              <p class="progress-text">
                已收集 {{ responseCount }} / {{ project?.targetRespondents || 100 }} 份
              </p>
            </el-form-item>
          </el-form>
        </div>
        
        <div class="actions">
          <el-button
            v-if="questionnaire.status !== 'PUBLISHED'"
            type="primary"
            size="large"
            :loading="publishing"
            :disabled="!surveyUrl"
            @click="handlePublish"
          >
            发布问卷
          </el-button>
          
          <el-button
            v-if="questionnaire.status === 'PUBLISHED'"
            type="success"
            size="large"
            @click="handleNext"
          >
            下一步：生成报告
          </el-button>
          
          <el-button size="large" @click="$router.push('/projects')">
            返回
          </el-button>
        </div>
      </div>
    </el-card>
    
    <!-- 题目内容对话框 -->
    <el-dialog
      v-model="showQuestionDialog"
      title="AI润色后的问卷题目"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-alert
        title="复制题目到问卷网"
        type="info"
        :closable="false"
        style="margin-bottom: 16px;"
      >
        <p>请将下方题目复制到问卷网的编辑页面中。您可以直接复制题目文本，然后在问卷网中逐个添加。</p>
      </el-alert>
      
      <div v-if="questionnaire && questionnaire.questions" class="questions-preview">
        <div 
          v-for="(question, index) in questionnaire.questions" 
          :key="question.id"
          class="question-item"
        >
          <div class="question-header">
            <span class="question-number">Q{{ index + 1 }}</span>
            <el-tag size="small" type="info">{{ getQuestionTypeText(question.questionType) }}</el-tag>
            <el-tag v-if="question.isRequired" size="small" type="danger">必填</el-tag>
          </div>
          
          <div class="question-content">
            <p class="question-text">{{ question.questionText }}</p>
            
            <div v-if="question.options && question.options.length > 0" class="options-list">
              <div 
                v-for="(option, optIndex) in question.options" 
                :key="optIndex"
                class="option-item"
              >
                <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
                <span>{{ option.label }}</span>
              </div>
            </div>
            
            <div v-if="question.questionDescription" class="question-desc">
              <el-icon><InfoFilled /></el-icon>
              <span>{{ question.questionDescription }}</span>
            </div>
          </div>
          
          <el-button 
            size="small" 
            type="primary" 
            text
            @click="copyQuestionText(question, index)"
          >
            <el-icon><CopyDocument /></el-icon>
            复制此题
          </el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showQuestionDialog = false">关闭</el-button>
        <el-button type="primary" @click="copyAllQuestions">
          <el-icon><CopyDocument /></el-icon>
          复制全部题目
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestionnaireByProject, publishQuestionnaire, publishToWenjuan } from '../../api/questionnaire'
import { getProject } from '../../api/project'
import { getResponseCount } from '../../api/response'
import WenjuanExport from './WenjuanExport.vue'

const router = useRouter()
const route = useRoute()

const publishing = ref(false)
const publishingToWenjuan = ref(false)
const questionnaire = ref(null)
const project = ref(null)
const surveyUrl = ref('')
const responseCount = ref(0)
const wenjuanInfo = ref(null)
const showQuestionDialog = ref(false)
const showExportPage = ref(false)

const responsePercentage = computed(() => {
  const target = project.value?.targetRespondents || 100
  return Math.min(Math.round((responseCount.value / target) * 100), 100)
})

// 生成本地链接（测试用）
const generateLocalUrl = () => {
  const baseUrl = window.location.origin
  surveyUrl.value = `${baseUrl}/survey/${questionnaire.value.id}`
  ElMessage.success('本地测试链接已生成')
}

// 发布到问卷网（公网访问）⭐ 新功能
const handlePublishToWenjuan = async () => {
  try {
    await ElMessageBox.confirm(
      '发布到问卷网后，将生成公网访问链接。系统会自动创建空白项目，然后打开导出页面帮助您将问卷内容复制到问卷网。',
      '确认发布',
      {
        confirmButtonText: '确认发布',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    publishingToWenjuan.value = true
    
    const res = await publishToWenjuan(questionnaire.value.id)
    
    if (res.code === 200) {
      wenjuanInfo.value = res.data
      surveyUrl.value = res.data.surveyUrl
      questionnaire.value.status = 'PUBLISHED'
      
      ElMessage.success({
        message: '空白项目已创建！正在打开导出页面...',
        duration: 2000
      })
      
      // 延迟500ms后显示导出页面，让用户看到成功提示
      setTimeout(() => {
        showExportPage.value = true
      }, 500)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发布到问卷网失败:', error)
      ElMessage.error('发布失败：' + (error.response?.data?.message || error.message || '未知错误'))
    }
  } finally {
    publishingToWenjuan.value = false
  }
}

// 返回上一步
const handleBack = () => {
  router.push(`/project/${route.params.id}/step4`)
}

const handlePublish = async () => {
  try {
    publishing.value = true
    
    await publishQuestionnaire(questionnaire.value.id, surveyUrl.value)
    
    ElMessage.success('问卷发布成功')
    questionnaire.value.status = 'PUBLISHED'
  } catch (error) {
    console.error('发布问卷失败:', error)
  } finally {
    publishing.value = false
  }
}

const handleNext = () => {
  router.push(`/project/${route.params.id}/step6`)
}

// 打开问卷网编辑页面
const openWenjuanEdit = () => {
  if (wenjuanInfo.value && wenjuanInfo.value.manageUrl) {
    window.open(wenjuanInfo.value.manageUrl, '_blank')
    ElMessage.success('已打开问卷网编辑页面')
  } else {
    window.open('https://www.wenjuan.com', '_blank')
    ElMessage.info('请登录后在"我的问卷"中找到对应项目')
  }
}

// 复制URL
const copyUrl = async (url) => {
  try {
    await navigator.clipboard.writeText(url)
    ElMessage.success('链接已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = url
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

// 获取题型文本
const getQuestionTypeText = (type) => {
  const typeMap = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'TEXT': '填空题',
    'TEXTAREA': '问答题',
    'RATING': '评分题',
    'MATRIX': '矩阵题'
  }
  return typeMap[type] || type
}

// 复制单个题目
const copyQuestionText = async (question, index) => {
  let text = `Q${index + 1}. ${question.questionText}\n`
  
  if (question.questionDescription) {
    text += `说明：${question.questionDescription}\n`
  }
  
  text += `题型：${getQuestionTypeText(question.questionType)}\n`
  text += `是否必填：${question.isRequired ? '是' : '否'}\n`
  
  if (question.options && question.options.length > 0) {
    text += '\n选项：\n'
    question.options.forEach((option, i) => {
      text += `${String.fromCharCode(65 + i)}. ${option.label}\n`
    })
  }
  
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success(`题目 Q${index + 1} 已复制`)
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

// 复制全部题目
const copyAllQuestions = async () => {
  if (!questionnaire.value || !questionnaire.value.questions) {
    ElMessage.warning('没有可复制的题目')
    return
  }
  
  let text = `${questionnaire.value.title}\n`
  text += `${questionnaire.value.description}\n`
  text += '\n=================\n\n'
  
  questionnaire.value.questions.forEach((question, index) => {
    text += `Q${index + 1}. ${question.questionText}\n`
    
    if (question.questionDescription) {
      text += `说明：${question.questionDescription}\n`
    }
    
    text += `题型：${getQuestionTypeText(question.questionType)}\n`
    text += `是否必填：${question.isRequired ? '是' : '否'}\n`
    
    if (question.options && question.options.length > 0) {
      text += '\n选项：\n'
      question.options.forEach((option, i) => {
        text += `${String.fromCharCode(65 + i)}. ${option.label}\n`
      })
    }
    
    text += '\n-----------------\n\n'
  })
  
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('全部题目已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
  }
}

const loadData = async () => {
  try {
    const [qRes, pRes] = await Promise.all([
      getQuestionnaireByProject(route.params.id),
      getProject(route.params.id)
    ])
    
    questionnaire.value = qRes.data
    project.value = pRes.data
    
    if (questionnaire.value.surveyUrl) {
      surveyUrl.value = questionnaire.value.surveyUrl
    }
    
    if (questionnaire.value.status === 'PUBLISHED') {
      const rRes = await getResponseCount(questionnaire.value.id)
      responseCount.value = rRes.data
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
/* 导出页面包装器 */
.export-page-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #f5f7fa;
  overflow-y: auto;
  overflow-x: hidden;
  z-index: 2000;
  box-sizing: border-box;
}

/* 自定义滚动条样式 */
.export-page-wrapper::-webkit-scrollbar {
  width: 10px;
}

.export-page-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.export-page-wrapper::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 5px;
}

.export-page-wrapper::-webkit-scrollbar-thumb:hover {
  background: #764ba2;
}

.step-container {
  height: 100vh;
  width: 100vw;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: fixed;
  top: 0;
  left: 0;
  overflow-x: hidden;
  overflow-y: scroll;
  box-sizing: border-box;
}

.step-container::before {
  content: '';
  position: absolute;
  width: 600px;
  height: 600px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  top: -300px;
  right: -200px;
}

.step-card {
  max-width: 900px;
  margin: 0 auto 40px auto;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
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

:deep(.el-card__header) {
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  border-bottom: 2px solid #e9ecef;
  padding: 24px 32px;
}

:deep(.el-card__body) {
  padding: 0;
  overflow: visible;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 100;
}

.card-header .el-button {
  position: relative;
  z-index: 100;
  cursor: pointer;
  pointer-events: auto;
}

.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.loading-section {
  padding: 60px 36px;
  text-align: center;
}

.publish-section {
  padding: 28px 36px;
}

:deep(.publish-section .el-alert) {
  border-radius: 12px;
  margin-bottom: 32px;
}

.publish-form {
  margin: 36px 0;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 15px;
  color: #2c3e50;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #e4e7ed inset;
  border-radius: 12px;
  padding: 12px 16px;
  transition: all 0.3s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #667eea inset;
}

:deep(.el-input-group__append) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 0 12px 12px 0;
}

:deep(.el-input-group__append .el-button) {
  color: white;
  font-weight: 600;
  border: none;
  background: transparent;
}

:deep(.el-progress) {
  margin-top: 8px;
}

:deep(.el-progress__text) {
  font-size: 16px !important;
  font-weight: 700 !important;
}

.progress-text {
  margin-top: 12px;
  color: #555;
  font-size: 15px;
  font-weight: 600;
  text-align: center;
}

.actions {
  text-align: center;
  margin-top: 48px;
  padding-top: 32px;
  border-top: 2px solid #dee2e6;
}

:deep(.actions .el-button) {
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 22px;
  transition: all 0.3s;
  margin: 0 8px;
}

:deep(.actions .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.actions .el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

:deep(.actions .el-button--success) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(17, 153, 142, 0.3);
}

:deep(.actions .el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(17, 153, 142, 0.4);
}

:deep(.actions .el-button--default) {
  border: 2px solid #e4e7ed;
}

:deep(.actions .el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
}

/* 题目对话框样式 */
.questions-preview {
  max-height: 500px;
  overflow-y: auto;
  padding: 4px;
}

.question-item {
  background: #f8f9ff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  border: 2px solid #e9ecef;
  transition: all 0.3s;
}

.question-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.15);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.question-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 16px;
  font-weight: 700;
  font-size: 14px;
}

.question-content {
  margin: 16px 0;
}

.question-text {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  line-height: 1.8;
  margin: 0 0 12px 0;
}

.options-list {
  margin-top: 12px;
  padding-left: 8px;
}

.option-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 12px;
  margin: 4px 0;
  background: white;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

.option-label {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  background: #667eea;
  color: white;
  border-radius: 4px;
  font-weight: 600;
  font-size: 12px;
  flex-shrink: 0;
}

.question-desc {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #fff7e6;
  border-radius: 8px;
  color: #666;
  font-size: 14px;
}

.question-desc .el-icon {
  color: #e6a23c;
}

@media (max-width: 768px) {
  .step-container {
    padding: 20px 16px;
  }
  
  .step-card {
    max-width: 100%;
  }
  
  .publish-section {
    padding: 24px 20px;
  }
  
  .qrcode-placeholder {
    width: 200px;
    height: 200px;
  }
  
  .question-item {
    padding: 16px;
  }
}
</style>

