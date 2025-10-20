<template>
  <div class="step-container">
    <el-card class="step-card">
      <template #header>
        <div class="card-header">
          <h2>Step 3: 生成问卷</h2>
          <el-button type="text" @click="handleBack">返回上一步</el-button>
        </div>
      </template>
      
      <div v-if="!questionnaire" class="generate-section">
        <el-empty description="问卷尚未生成">
          <el-button type="primary" size="large" :loading="generating" @click="handleGenerate">
            生成问卷
          </el-button>
        </el-empty>
      </div>
      
      <div v-else class="questionnaire-preview">
        <div class="questionnaire-header">
          <h3>{{ questionnaire.title }}</h3>
          <p class="description">{{ questionnaire.description }}</p>
          <el-divider />
          <div class="introduction">
            <h4>问卷说明</h4>
            <p>{{ questionnaire.introduction }}</p>
          </div>
        </div>
        
        <div class="questions-list">
          <div
            v-for="(question, index) in questionnaire.questions"
            :key="question.id"
            class="question-item"
          >
            <div class="question-header">
              <span class="question-number">Q{{ index + 1 }}</span>
              <span class="question-type">{{ getQuestionTypeText(question.questionType) }}</span>
            </div>
            
            <h4 class="question-text">{{ question.questionText }}</h4>
            
            <p v-if="question.questionDescription" class="question-desc">
              {{ question.questionDescription }}
            </p>
            
            <div v-if="question.options" class="options-list">
              <div
                v-for="option in question.options"
                :key="option.value"
                class="option-item"
              >
                <el-radio v-if="question.questionType === 'SINGLE_CHOICE'" :label="option.value" disabled>
                  {{ option.label }}
                </el-radio>
                <el-checkbox v-else-if="question.questionType === 'MULTIPLE_CHOICE'" disabled>
                  {{ option.label }}
                </el-checkbox>
                <div v-else-if="question.questionType === 'RATING'">
                  <span>{{ option.label }}:</span>
                  <el-rate v-model="ratingDemo" disabled />
                </div>
              </div>
            </div>
            
            <el-input
              v-if="question.questionType === 'TEXT'"
              type="textarea"
              :rows="3"
              placeholder="受访者填写区域"
              disabled
            />
          </div>
        </div>
        
        <div class="actions">
          <el-button type="success" size="large" @click="handleNext">
            下一步：AI审核
          </el-button>
          <el-button size="large" @click="handleRegenerate">
            重新生成
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { generateQuestionnaire, getQuestionnaireByProject } from '../../api/questionnaire'

const router = useRouter()
const route = useRoute()

const generating = ref(false)
const questionnaire = ref(null)
const ratingDemo = ref(0)

// 返回上一步
const handleBack = () => {
  router.push(`/project/${route.params.id}/step2`)
}

const handleGenerate = async () => {
  try {
    generating.value = true
    ElMessage.info('正在生成问卷，请稍候...')
    
    const res = await generateQuestionnaire(route.params.id)
    questionnaire.value = res.data
    
    ElMessage.success('问卷生成成功')
  } catch (error) {
    console.error('生成问卷失败:', error)
  } finally {
    generating.value = false
  }
}

const handleRegenerate = async () => {
  try {
    await ElMessageBox.confirm('确定要重新生成问卷吗？当前问卷将被覆盖。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    questionnaire.value = null
    handleGenerate()
  } catch (error) {
    // User cancelled
  }
}

const handleNext = () => {
  router.push(`/project/${route.params.id}/step4`)
}

const getQuestionTypeText = (type) => {
  const map = {
    'SINGLE_CHOICE': '单选题',
    'MULTIPLE_CHOICE': '多选题',
    'RATING': '评分题',
    'TEXT': '简答题',
    'MATRIX': '矩阵题'
  }
  return map[type] || type
}

const loadQuestionnaire = async () => {
  try {
    const res = await getQuestionnaireByProject(route.params.id)
    if (res.data) {
      questionnaire.value = res.data
    }
  } catch (error) {
    console.error('加载问卷失败:', error)
  }
}

onMounted(() => {
  loadQuestionnaire()
})
</script>

<style scoped>
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
  width: 700px;
  height: 700px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  top: -350px;
  right: -250px;
}

.step-card {
  max-width: 1000px;
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
  padding: 16px 24px;
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
  font-weight: 800;
  color: #2c3e50;
  margin: 0;
}

.generate-section {
  padding: 60px 36px;
  text-align: center;
}

:deep(.generate-section .el-empty__description) {
  margin-top: 20px;
  font-size: 15px;
  color: #666;
}

:deep(.generate-section .el-button) {
  margin-top: 28px;
  height: 46px;
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 23px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

:deep(.generate-section .el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.4);
}

.questionnaire-preview {
  padding: 28px 36px;
}

.questionnaire-header {
  text-align: center;
  margin-bottom: 32px;
}

.questionnaire-header h3 {
  font-size: 24px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 10px;
}

.description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.introduction {
  background: linear-gradient(135deg, #f8f9ff 0%, #f5f7ff 100%);
  padding: 18px 22px;
  border-radius: 10px;
  margin: 20px 0;
  border-left: 4px solid #667eea;
}

.introduction h4 {
  font-size: 15px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 10px 0;
}

.introduction p {
  color: #555;
  line-height: 1.7;
  margin: 0;
  font-size: 14px;
}

.questions-list {
  margin: 28px 0;
}

.question-item {
  background: #fafbfc;
  padding: 28px;
  margin-bottom: 20px;
  border-radius: 16px;
  border: 2px solid #e9ecef;
  transition: all 0.3s;
}

.question-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

.question-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.question-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 14px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.question-type {
  background: #e9ecef;
  color: #495057;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
}

.question-text {
  font-size: 17px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  line-height: 1.6;
}

.question-desc {
  color: #666;
  font-size: 14px;
  margin-bottom: 20px;
  white-space: pre-wrap;
  line-height: 1.6;
  padding-left: 4px;
}

.options-list {
  margin-top: 20px;
}

.option-item {
  margin-bottom: 12px;
  padding: 14px 18px;
  background: white;
  border-radius: 10px;
  border: 2px solid #e4e7ed;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #667eea;
  background: #f8f9ff;
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 2px solid #e4e7ed;
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

@media (max-width: 768px) {
  .step-container {
    padding: 20px 16px;
  }
  
  .step-card {
    max-width: 100%;
  }
  
  .questionnaire-preview {
    padding: 24px 20px;
  }
  
  .question-item {
    padding: 20px;
  }
}
</style>

