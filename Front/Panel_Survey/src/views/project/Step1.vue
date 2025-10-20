<template>
  <div class="step-container">
    <el-card class="step-card">
      <template #header>
        <div class="card-header">
          <h2>Step 1: 选择问卷目标</h2>
          <el-button type="text" @click="$router.push('/projects')">返回</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="step-form"
      >
        <el-form-item label="项目名称" prop="projectName">
          <el-input
            v-model="form.projectName"
            placeholder="请输入项目名称，例如：糖尿病治疗方案偏好调查"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="问卷目标" prop="objectiveType">
          <el-radio-group v-model="form.objectiveType">
            <el-radio label="treatment_preference" border>
              <div class="radio-content">
                <h4>治疗方案偏好</h4>
                <p>了解患者对不同治疗方案的偏好程度</p>
              </div>
            </el-radio>
            <el-radio label="outcome_importance" border>
              <div class="radio-content">
                <h4>结局重要性评估</h4>
                <p>评估患者对各种治疗结局的重要性认知</p>
              </div>
            </el-radio>
            <el-radio label="risk_benefit" border>
              <div class="radio-content">
                <h4>风险收益权衡</h4>
                <p>了解患者在风险和收益之间的权衡取舍</p>
              </div>
            </el-radio>
            <el-radio label="value_preference" border>
              <div class="radio-content">
                <h4>价值观和偏好综合</h4>
                <p>全面了解患者的价值观和偏好分布</p>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleNext">
            下一步
          </el-button>
          <el-button size="large" @click="$router.push('/projects')">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createProjectStep1, saveStep1Draft, getStep1Draft } from '../../api/project'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  projectName: '',
  objectiveType: 'treatment_preference'
})

const rules = {
  projectName: [
    { required: true, message: '请输入项目名称', trigger: 'blur' }
  ],
  objectiveType: [
    { required: true, message: '请选择问卷目标', trigger: 'change' }
  ]
}

// 自动保存到Redis
let saveTimer = null

const saveDraft = () => {
  clearTimeout(saveTimer)
  saveTimer = setTimeout(async () => {
    try {
      // 只有在有内容时才保存
      if (form.projectName || form.objectiveType !== 'treatment_preference') {
        await saveStep1Draft(form)
        console.log('草稿已自动保存到Redis')
      }
    } catch (error) {
      console.error('保存草稿失败:', error)
    }
  }, 2000) // 2秒防抖，避免频繁请求
}

// 监听表单变化，自动保存
watch(form, () => {
  saveDraft()
}, { deep: true })

// 加载保存的草稿
const loadDraft = async () => {
  try {
    const res = await getStep1Draft()
    if (res.data) {
      form.projectName = res.data.projectName || ''
      form.objectiveType = res.data.objectiveType || 'treatment_preference'
      ElMessage.success('已从Redis恢复上次填写的内容')
    }
  } catch (error) {
    console.error('加载草稿失败:', error)
  }
}

// 清除草稿
const clearDraft = () => {
  clearTimeout(saveTimer)
}

const handleNext = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    const res = await createProjectStep1(form)
    
    // 创建成功后清除计时器（后端会自动删除Redis草稿）
    clearDraft()
    
    ElMessage.success('项目创建成功，草稿已清除')
    router.push(`/project/${res.data.id}/step2`)
  } catch (error) {
    console.error('创建项目失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDraft()
})

onBeforeUnmount(() => {
  clearTimeout(saveTimer)
})
</script>

<style scoped>
.step-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: flex-start;
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
  width: 100%;
  max-width: 800px;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease-out;
  margin: 20px auto 60px auto;
  min-height: auto;
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

.step-form {
  padding: 28px 36px 60px 36px;
  max-width: 100%;
  height: auto;
  overflow: visible;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
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

:deep(.el-radio) {
  width: 100%;
  margin-right: 0;
  margin-bottom: 16px;
  height: auto;
  white-space: normal;
}

:deep(.el-radio.is-bordered) {
  padding: 20px;
  border-radius: 10px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s;
}

:deep(.el-radio.is-bordered:hover) {
  border-color: #667eea;
  background: #f8f9ff;
}

:deep(.el-radio.is-checked.is-bordered) {
  border-color: #667eea;
  background: #f0f2ff;
}

.radio-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 6px;
}

.radio-content p {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}

:deep(.el-form-item:last-child) {
  margin-top: 40px;
  padding-top: 30px;
  padding-bottom: 20px;
  border-top: 2px solid #e9ecef;
  margin-bottom: 0;
}

:deep(.el-button) {
  height: 50px;
  padding: 0 32px;
  font-size: 17px;
  font-weight: 600;
  border-radius: 25px;
  transition: all 0.3s;
}

:deep(.el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

:deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

:deep(.el-button--default) {
  border: 2px solid #e4e7ed;
}

:deep(.el-button--default:hover) {
  border-color: #667eea;
  color: #667eea;
}

@media (max-width: 768px) {
  .step-form {
    padding: 24px 20px;
  }
  
  :deep(.el-radio.is-bordered) {
    padding: 16px;
  }
  
  .card-header h2 {
    font-size: 20px;
  }
}
</style>

