<template>
  <div class="step-container">
    <el-card class="step-card">
      <template #header>
        <div class="card-header">
          <h2>Step 2: 填写问卷基本信息</h2>
          <el-button type="text" @click="handleBack">返回上一步</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="140px"
        class="step-form"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-form-item label="疾病名称" prop="diseaseName">
          <el-input
            v-model="form.diseaseName"
            placeholder="例如：2型糖尿病"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="干预措施名称" prop="interventionName">
          <el-input
            v-model="form.interventionName"
            placeholder="例如：GLP-1受体激动剂"
            clearable
          />
        </el-form-item>
        
        <el-form-item label="对照措施名称" prop="comparisonName">
          <el-input
            v-model="form.comparisonName"
            placeholder="例如：二甲双胍"
            clearable
          />
        </el-form-item>
        
        <el-divider content-position="left">详细描述</el-divider>
        
        <el-form-item label="患者人群描述" prop="patientDescription">
          <el-input
            v-model="form.patientDescription"
            type="textarea"
            :rows="3"
            placeholder="请描述目标患者人群特征"
          />
        </el-form-item>
        
        <el-form-item label="关注的结局" prop="outcomeList">
          <el-select
            v-model="form.outcomeList"
            multiple
            filterable
            allow-create
            placeholder="请选择或输入关注的结局指标"
            style="width: 100%"
          >
            <el-option label="疾病控制效果" value="疾病控制效果" />
            <el-option label="生活质量改善" value="生活质量改善" />
            <el-option label="副作用发生率" value="副作用发生率" />
            <el-option label="治疗费用" value="治疗费用" />
            <el-option label="治疗便利性" value="治疗便利性" />
            <el-option label="长期预后" value="长期预后" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="干预措施详情" prop="interventionDetails">
          <el-input
            v-model="form.interventionDetails"
            type="textarea"
            :rows="3"
            placeholder="请详细描述干预措施的具体内容、使用方法、预期效果等"
          />
        </el-form-item>
        
        <el-form-item label="对照措施详情" prop="comparisonDetails">
          <el-input
            v-model="form.comparisonDetails"
            type="textarea"
            :rows="3"
            placeholder="请详细描述对照措施的具体内容"
          />
        </el-form-item>
        
        <el-form-item label="副作用说明" prop="sideEffects">
          <el-input
            v-model="form.sideEffects"
            type="textarea"
            :rows="2"
            placeholder="请说明可能出现的副作用"
          />
        </el-form-item>
        
        <el-divider content-position="left">调查设置</el-divider>
        
        <el-form-item label="预计完成时间" prop="surveyDuration">
          <el-input-number
            v-model="form.surveyDuration"
            :min="5"
            :max="60"
            :step="5"
          />
          <span class="input-suffix">分钟</span>
        </el-form-item>
        
        <el-form-item label="目标受访者数" prop="targetRespondents">
          <el-input-number
            v-model="form.targetRespondents"
            :min="10"
            :max="1000"
            :step="10"
          />
          <span class="input-suffix">人</span>
        </el-form-item>
        
        <el-form-item label="其他补充信息" prop="additionalInfo">
          <el-input
            v-model="form.additionalInfo"
            type="textarea"
            :rows="3"
            placeholder="其他需要补充的信息"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="handleNext">
            下一步：生成问卷
          </el-button>
          <el-button size="large" @click="$router.push('/projects')">
            返回
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateProjectStep2, getProject, saveStep2Draft, getStep2Draft } from '../../api/project'

const router = useRouter()
const route = useRoute()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  projectId: null,
  diseaseName: '',
  interventionName: '',
  comparisonName: '',
  patientDescription: '',
  outcomeList: [],
  interventionDetails: '',
  comparisonDetails: '',
  sideEffects: '',
  surveyDuration: 15,
  targetRespondents: 100,
  additionalInfo: ''
})

const rules = {
  diseaseName: [
    { required: true, message: '请输入疾病名称', trigger: 'blur' }
  ],
  interventionName: [
    { required: true, message: '请输入干预措施名称', trigger: 'blur' }
  ]
}

// 自动保存到Redis
let saveTimer = null

const saveDraft = () => {
  if (!form.projectId) return // 项目ID加载完成后才保存
  
  clearTimeout(saveTimer)
  saveTimer = setTimeout(async () => {
    try {
      // 只在有实际内容时才保存
      if (form.diseaseName || form.interventionName) {
        const draftData = {
          projectId: form.projectId,
          diseaseName: form.diseaseName,
          interventionName: form.interventionName,
          comparisonName: form.comparisonName,
          patientDescription: form.patientDescription,
          outcomeList: form.outcomeList,
          interventionDetails: form.interventionDetails,
          comparisonDetails: form.comparisonDetails,
          sideEffects: form.sideEffects,
          surveyDuration: form.surveyDuration,
          targetRespondents: form.targetRespondents,
          additionalInfo: form.additionalInfo
        }
        await saveStep2Draft(draftData)
        console.log('Step2草稿已自动保存到Redis')
      }
    } catch (error) {
      console.error('保存草稿失败:', error)
    }
  }, 2000) // 2秒防抖
}

// 监听表单变化，自动保存
watch(form, () => {
  saveDraft()
}, { deep: true })

// 从Redis加载保存的草稿
const loadDraft = async () => {
  try {
    const res = await getStep2Draft()
    if (res.data) {
      const draft = res.data
      // 只恢复有值的字段
      if (draft.diseaseName) form.diseaseName = draft.diseaseName
      if (draft.interventionName) form.interventionName = draft.interventionName
      if (draft.comparisonName) form.comparisonName = draft.comparisonName
      if (draft.patientDescription) form.patientDescription = draft.patientDescription
      if (draft.outcomeList && draft.outcomeList.length > 0) form.outcomeList = draft.outcomeList
      if (draft.interventionDetails) form.interventionDetails = draft.interventionDetails
      if (draft.comparisonDetails) form.comparisonDetails = draft.comparisonDetails
      if (draft.sideEffects) form.sideEffects = draft.sideEffects
      if (draft.surveyDuration) form.surveyDuration = draft.surveyDuration
      if (draft.targetRespondents) form.targetRespondents = draft.targetRespondents
      if (draft.additionalInfo) form.additionalInfo = draft.additionalInfo
      
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

// 返回上一步
const handleBack = () => {
  router.push(`/project/${route.params.id}/step1`)
}

const handleNext = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    await updateProjectStep2(form)
    
    // 提交成功后清除计时器（后端会自动删除Redis草稿）
    clearDraft()
    
    ElMessage.success('信息保存成功，草稿已清除')
    router.push(`/project/${form.projectId}/step3`)
  } catch (error) {
    console.error('保存信息失败:', error)
  } finally {
    loading.value = false
  }
}

const loadProject = async () => {
  try {
    const res = await getProject(route.params.id)
    form.projectId = res.data.id
    
    // 优先使用后端数据
    if (res.data.diseaseName) {
      Object.assign(form, res.data)
    } else {
      // 如果后端没有数据，尝试从Redis加载草稿
      await loadDraft()
    }
  } catch (error) {
    console.error('加载项目失败:', error)
    // 加载失败也尝试从Redis恢复草稿
    await loadDraft()
  }
}

onMounted(() => {
  loadProject()
})

onBeforeUnmount(() => {
  clearTimeout(saveTimer)
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
  width: 600px;
  height: 600px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  top: -300px;
  right: -200px;
}

.step-card {
  width: 100%;
  max-width: 900px;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease-out;
  margin: 0 auto 80px auto;
  min-height: auto;
  height: auto;
  overflow: visible;
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
  font-size: 15px;
  color: #2c3e50;
}

:deep(.el-divider__text) {
  background-color: white;
  font-weight: 700;
  font-size: 16px;
  color: #667eea;
  padding: 0 20px;
}

:deep(.el-divider--horizontal) {
  margin: 32px 0 28px 0;
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

:deep(.el-textarea__inner) {
  font-size: 14px;
  line-height: 1.6;
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-input-number) {
  width: 180px;
}

:deep(.el-input-number .el-input__wrapper) {
  border-radius: 12px;
}

.input-suffix {
  margin-left: 12px;
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

:deep(.el-form-item:last-child) {
  margin-top: 40px;
  padding-top: 30px;
  padding-bottom: 20px;
  border-top: 2px solid #e9ecef;
  margin-bottom: 0;
}

:deep(.el-button) {
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 22px;
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
  .step-container {
    padding: 20px 16px;
  }
  
  .step-card {
    max-width: 100%;
  }
  
  .step-form {
    padding: 24px 20px;
  }
  
  :deep(.el-form-item__label) {
    font-size: 14px;
  }
  
  .card-header h2 {
    font-size: 20px;
  }
  
  :deep(.el-input-number) {
    width: 140px;
  }
}
</style>

