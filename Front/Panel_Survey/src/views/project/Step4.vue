<template>
  <div class="step-container">
    <el-card class="step-card">
      <template #header>
        <div class="card-header">
          <h2>Step 4: AI润色与预览</h2>
          <el-button type="text" @click="handleBack">返回上一步</el-button>
        </div>
      </template>
      
      <div v-if="!project" class="loading-section">
        <el-empty description="正在加载项目信息..." />
      </div>
      
      <div v-else class="review-section">
        <el-alert
          title="AI润色说明"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            AI将对Step2填写的治疗方案信息进行润色，生成专业、易懂的表格展示，这个表格将在问卷中供患者阅读。表格内容会精炼为50-80字，副作用说明等会保留完整描述。
          </template>
        </el-alert>
        
        <div class="review-actions">
          <el-button
            type="primary"
            size="large"
            :loading="reviewing"
            @click="handleReview"
            :icon="reviewing ? '' : 'MagicStick'"
          >
            {{ reviewing ? 'AI正在润色...' : '开始AI润色' }}
          </el-button>
        </div>
        
        <!-- 原始内容 -->
        <el-card class="content-card" shadow="never">
          <template #header>
            <h3><span class="icon">📋</span> 原始内容</h3>
          </template>
          
          <el-descriptions :column="1" border>
            <el-descriptions-item label="疾病名称">{{ project.diseaseName }}</el-descriptions-item>
            <el-descriptions-item label="干预方案">{{ project.interventionName }}</el-descriptions-item>
            <el-descriptions-item label="对照方案">{{ project.comparisonName || '标准治疗' }}</el-descriptions-item>
            <el-descriptions-item label="患者描述">
              <div class="text-content">{{ project.patientDescription }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="干预方案详情">
              <div class="text-content">{{ project.interventionDetails }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="对照方案详情">
              <div class="text-content">{{ project.comparisonDetails }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="副作用说明">
              <div class="text-content">{{ project.sideEffects }}</div>
            </el-descriptions-item>
            <el-descriptions-item label="其他信息" v-if="project.additionalInfo">
              <div class="text-content">{{ project.additionalInfo }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <!-- AI润色后的表格预览 -->
        <el-card v-if="polishedData" class="content-card polished" shadow="never">
          <template #header>
            <div class="header-with-actions">
              <h3><span class="icon">✨</span> AI润色后的表格（将展示在问卷中）</h3>
            </div>
          </template>
          
          <el-table :data="polishedData" border class="polished-table">
            <el-table-column label="对比项" prop="category" width="150" align="center">
              <template #default="{ row }">
                <strong>{{ row.category }}</strong>
              </template>
            </el-table-column>
            <el-table-column :label="project?.interventionName || '干预方案'" prop="intervention">
              <template #default="{ row }">
                <div class="table-content">{{ row.intervention }}</div>
              </template>
            </el-table-column>
            <el-table-column :label="project?.comparisonName || '对照方案'" prop="comparison">
              <template #default="{ row }">
                <div class="table-content">{{ row.comparison }}</div>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 副作用说明（如果有） -->
          <div v-if="polishedSideEffects" class="additional-section">
            <h4>可能的副作用</h4>
            <p>{{ polishedSideEffects }}</p>
          </div>
          
          <!-- 其他重要信息（如果有） -->
          <div v-if="polishedAdditionalInfo" class="additional-section">
            <h4>其他重要信息</h4>
            <p>{{ polishedAdditionalInfo }}</p>
          </div>
        </el-card>
        
        <div class="actions">
          <el-button type="success" size="large" @click="handleNext">
            下一步：发布问卷
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProject } from '../../api/project'
import { polishReportBackground } from '../../api/questionnaire'

const router = useRouter()
const route = useRoute()

const reviewing = ref(false)
const project = ref(null)
const polishedData = ref(null)
const polishedSideEffects = ref('')
const polishedAdditionalInfo = ref('')

// 返回上一步
const handleBack = () => {
  router.push(`/project/${route.params.id}/step3`)
}

// AI润色
const handleReview = async () => {
  try {
    reviewing.value = true
    ElMessage.info('AI正在润色报告背景，这可能需要一些时间...')
    
    const res = await polishReportBackground(route.params.id)
    const data = res.data
    
    // 解析返回的数据
    polishedData.value = data.comparisonTable || []
    polishedSideEffects.value = data.sideEffects || ''
    polishedAdditionalInfo.value = data.additionalInfo || ''
    
    ElMessage.success('AI润色完成！表格内容已精炼为简洁版本')
  } catch (error) {
    console.error('AI润色失败:', error)
    ElMessage.error('AI润色失败，请重试')
  } finally {
    reviewing.value = false
  }
}

// 下一步
const handleNext = () => {
  router.push(`/project/${route.params.id}/step5`)
}

// 加载项目信息
const loadProject = async () => {
  try {
    const res = await getProject(route.params.id)
    if (res.data) {
      project.value = res.data
    }
  } catch (error) {
    console.error('加载项目信息失败:', error)
    ElMessage.error('加载项目信息失败')
  }
}

onMounted(() => {
  loadProject()
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

.review-section {
  padding: 28px 36px;
}

:deep(.review-section .el-alert) {
  border-radius: 12px;
  margin-bottom: 24px;
}

.review-actions {
  text-align: center;
  margin: 36px 0;
}

:deep(.review-actions .el-button) {
  height: 50px;
  padding: 0 48px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

:deep(.review-actions .el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.4);
}

.content-card {
  margin: 28px 0;
  border-radius: 16px;
  border: 2px solid #e9ecef;
  transition: all 0.3s;
}

.content-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

.content-card.polished {
  border-color: #11998e;
  background: linear-gradient(135deg, #f0fff4 0%, #e6fffa 100%);
}

.content-card.polished:hover {
  border-color: #0a7e6a;
  box-shadow: 0 6px 24px rgba(17, 153, 142, 0.15);
}

:deep(.content-card .el-card__header) {
  background: #fafbfc;
  border-bottom: 2px solid #e9ecef;
  padding: 20px 24px;
}

:deep(.content-card.polished .el-card__header) {
  background: linear-gradient(135deg, #d1f2eb 0%, #c8f7dc 100%);
  border-bottom: 2px solid #11998e;
}

:deep(.content-card h3) {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  display: flex;
  align-items: center;
}

:deep(.content-card .icon) {
  margin-right: 10px;
  font-size: 20px;
}

.header-with-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

:deep(.el-descriptions) {
  margin-top: 12px;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
  color: #555;
  min-width: 120px;
}

.text-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #333;
}

/* 润色后的表格样式 */
.polished-table {
  margin-top: 16px;
  font-size: 14px;
}

:deep(.polished-table .el-table__header th) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
  font-weight: 700;
  font-size: 15px;
  padding: 16px 12px;
}

:deep(.polished-table .el-table__body td) {
  padding: 14px 12px;
}

.table-content {
  line-height: 1.8;
  color: #333;
  text-align: justify;
  white-space: pre-wrap;
}

:deep(.polished-table .el-table__row:hover .table-content) {
  color: #0a7e6a;
}

/* 附加信息样式 */
.additional-section {
  margin-top: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #f0fff4 0%, #e6fffa 100%);
  border-radius: 12px;
  border-left: 4px solid #11998e;
}

.additional-section h4 {
  font-size: 16px;
  font-weight: 700;
  color: #11998e;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
}

.additional-section h4::before {
  content: '📌';
  margin-right: 8px;
}

.additional-section p {
  margin: 0;
  line-height: 1.8;
  color: #4a5568;
  text-align: justify;
  white-space: pre-wrap;
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
  
  .review-section {
    padding: 24px 20px;
  }
  
  .comparison {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .question-item {
    padding: 20px;
  }
}
</style>

