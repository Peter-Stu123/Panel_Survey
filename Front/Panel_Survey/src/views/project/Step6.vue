<template>
  <div class="step-container">
    <el-card class="step-card">
      <template #header>
        <div class="card-header">
          <h2>Step 6: 分析报告</h2>
          <el-button type="text" @click="handleBackToProjects">返回项目列表</el-button>
        </div>
      </template>
      
      <div v-if="!report" class="generate-section">
        <el-alert
          v-if="questionnaire && questionnaire.wenjuanShortId"
          title="问卷已发布到问卷网 ✅"
          type="success"
          :closable="false"
          show-icon
        >
          <template #default>
            <div style="line-height: 1.8;">
              <p style="font-size: 15px; font-weight: 600; color: #67C23A;">✨ 您的问卷已成功发布到问卷网！受访者的填写数据将实时保存在问卷网平台。</p>
              
              <div style="margin-top: 16px; padding: 12px; background: #f0f9ff; border-radius: 6px;">
                <p style="font-weight: 600; color: #409EFF; margin-bottom: 8px;">📊 查看实时数据（推荐）</p>
                <p style="margin-left: 20px;">
                  点击下方绿色的 <strong>"🌐 查看问卷网实时数据（推荐）"</strong> 按钮，可以直接访问问卷网后台，查看：
                </p>
                <ul style="margin: 8px 0 0 40px;">
                  <li>实时答卷统计和图表</li>
                  <li>详细的答卷列表</li>
                  <li>数据导出（Excel/SPSS等格式）</li>
                  <li>更多专业的数据分析工具</li>
                </ul>
              </div>

              <div style="margin-top: 12px; padding: 12px; background: #fff7e6; border-radius: 6px;">
                <p style="font-weight: 600; color: #E6A23C; margin-bottom: 8px;">📈 AI分析报告（针对医生）</p>
                <p style="margin-left: 20px;">
                  点击下方蓝色的 <strong>"📊 上传Excel生成AI分析报告"</strong> 按钮，上传从问卷网导出的Excel文件，系统将自动生成专业的医学调查分析报告。
                </p>
                <ul style="margin: 8px 0 0 40px; font-size: 13px; color: #606266;">
                  <li>在问卷网后台点击"导出" → 选择"Excel格式"</li>
                  <li>下载Excel文件到本地</li>
                  <li>在此页面上传Excel文件即可生成AI分析报告</li>
                </ul>
              </div>

              <p style="margin-top: 16px; padding: 10px; background: #f5f7fa; border-radius: 4px; font-size: 13px; color: #909399;">
                <strong>💡 提示：</strong>问卷网已同步显示当前有 <strong style="color: #67C23A;">{{ questionnaire.wenjuanRespondentCount || 0 }}</strong> 份答卷。
              </p>
            </div>
          </template>
        </el-alert>
        
        <el-alert
          v-else
          title="生成分析报告"
          type="info"
          description="基于收集到的问卷数据，生成患者偏好分析报告"
          :closable="false"
          show-icon
        />
        
        <div class="stats-info">
          <el-statistic title="总回复数" :value="responseCount" />
          <el-statistic title="目标数量" :value="project?.targetRespondents || 0" />
        </div>
        
        <div class="generate-actions">
          <el-button
            v-if="questionnaire && questionnaire.wenjuanShortId"
            type="success"
            size="large"
            :loading="loadingWenjuanReport"
            @click="handleOpenWenjuanReport"
            style="flex: 1; max-width: 400px;"
          >
            🌐 查看问卷网实时数据（推荐）
          </el-button>
          <el-button
            type="primary"
            size="large"
            @click="handleShowUploadDialog"
            style="flex: 1; max-width: 400px;"
          >
            📊 上传Excel生成AI分析报告
          </el-button>
        </div>
        
        <div v-if="responseCount === 0 && questionnaire && questionnaire.wenjuanShortId" class="tips-section">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; align-items: center; gap: 8px;">
                <el-icon color="#409EFF"><InfoFilled /></el-icon>
                <span>温馨提示</span>
              </div>
            </template>
            <div style="line-height: 1.8; color: #606266;">
              <p style="font-size: 15px; font-weight: 600; color: #409EFF;">📋 如何生成AI分析报告？</p>
              
              <div style="margin-top: 16px; padding: 14px; background: #fff7e6; border-left: 4px solid #E6A23C; border-radius: 4px;">
                <p style="font-weight: 600; color: #E6A23C; margin-bottom: 10px;">📥 步骤一：导出问卷网数据</p>
                <ol style="margin: 8px 0 0 20px; line-height: 2;">
                  <li>点击上方绿色按钮 <strong>"🌐 查看问卷网实时数据"</strong></li>
                  <li>进入问卷网后台，点击页面上的 <strong>"导出"</strong> 按钮</li>
                  <li>选择 <strong>"Excel格式"</strong>，下载数据文件到本地</li>
                </ol>
              </div>

              <div style="margin-top: 12px; padding: 14px; background: #f0f9ff; border-left: 4px solid #409EFF; border-radius: 4px;">
                <p style="font-weight: 600; color: #409EFF; margin-bottom: 10px;">📊 步骤二：上传Excel生成报告</p>
                <ol style="margin: 8px 0 0 20px; line-height: 2;">
                  <li>点击上方蓝色按钮 <strong>"📊 上传Excel生成AI分析报告"</strong></li>
                  <li>选择刚才下载的Excel文件</li>
                  <li>系统将自动解析数据并生成专业的医学调查分析报告</li>
                </ol>
              </div>

              <p style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 4px; font-size: 13px;">
                <strong>💡 提示：</strong>AI分析报告将包含数据统计、趋势分析、患者偏好洞察等内容，专为医疗专业人士设计。
              </p>
            </div>
          </el-card>
        </div>
      </div>
      
      <div v-else class="report-section">
        <div class="report-header">
          <h3>{{ report.reportTitle }}</h3>
          <div class="export-buttons">
            <el-button 
              type="success" 
              @click="handleOpenWenjuanReport"
              :loading="loadingWenjuanReport"
              v-if="questionnaire && questionnaire.wenjuanShortId"
            >
              🌐 查看问卷网实时数据
            </el-button>
            <el-button type="primary" @click="handleExportPdf">导出PDF</el-button>
            <el-button type="success" @click="handleExportExcel">导出Excel</el-button>
          </div>
        </div>
        
        <el-divider />
        
        <el-row :gutter="20" class="stats-row">
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic title="总回复数" :value="report.totalResponses" />
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic title="有效回复数" :value="report.validResponses" />
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic
                title="完成率"
                :value="report.completionRate"
                suffix="%"
                :precision="2"
              />
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card">
              <el-statistic
                title="平均耗时"
                :value="report.averageDuration"
                suffix="秒"
              />
            </el-card>
          </el-col>
        </el-row>
        
        <!-- 临床建议卡片 - 核心内容 -->
        <el-card class="clinical-advice-card" style="border: 2px solid #67C23A; box-shadow: 0 4px 20px rgba(103, 194, 58, 0.2);">
          <template #header>
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-icon :size="24" color="#67C23A"><Memo /></el-icon>
              <h3 style="margin: 0; color: #67C23A; font-size: 20px; font-weight: bold;">临床建议报告</h3>
            </div>
          </template>
          <div class="analysis-content clinical-content" v-html="formatAnalysisContent(report.preferenceSummary)"></div>
        </el-card>
        
        <!-- 关键发现卡片 - 次要内容 -->
        <el-card class="findings-card" v-if="report.keyFindings">
          <template #header>
            <h4 style="color: #909399;">📋 关键发现摘要</h4>
          </template>
          <div class="findings-content">
            <pre style="color: #606266; line-height: 1.8;">{{ report.keyFindings }}</pre>
          </div>
        </el-card>
        
        
        <div class="complete-actions">
          <el-button type="success" size="large" @click="$router.push('/projects')">
            完成
          </el-button>
          <el-button size="large" @click="handleRegenerate">
            重新生成报告
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- Excel上传对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="📊 上传Excel生成AI分析报告"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="upload-dialog-content">
        <el-alert
          title="操作说明"
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 20px;"
        >
          <template #default>
            <ol style="margin: 8px 0; padding-left: 20px; line-height: 2;">
              <li>请先在问卷网后台导出Excel格式的答卷数据</li>
              <li>选择导出的Excel文件上传</li>
              <li>系统将自动解析数据并生成专业的医学调查分析报告</li>
            </ol>
          </template>
        </el-alert>

        <el-upload
          ref="uploadRef"
          class="upload-demo"
          drag
          :auto-upload="false"
          :on-change="handleFileChange"
          :limit="1"
          accept=".xlsx,.xls"
          :file-list="fileList"
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将Excel文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 .xlsx 和 .xls 格式，文件大小不超过10MB
            </div>
          </template>
        </el-upload>

        <div v-if="selectedFile" class="file-info" style="margin-top: 20px;">
          <el-tag type="success" size="large">
            📄 {{ selectedFile.name }} ({{ formatFileSize(selectedFile.size) }})
          </el-tag>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="uploadDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="uploading"
            :disabled="!selectedFile"
            @click="handleUploadAndGenerate"
          >
            {{ uploading ? '正在生成报告...' : '上传并生成报告' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled, UploadFilled, Memo } from '@element-plus/icons-vue'
import { getProject } from '../../api/project'
import { getQuestionnaireByProject, syncWenjuanData } from '../../api/questionnaire'
import { generateReport, getReportByQuestionnaire, getWenjuanReportUrl } from '../../api/report'
import { getResponseCount } from '../../api/response'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'

const route = useRoute()
const router = useRouter()

const generating = ref(false)
const loadingWenjuanReport = ref(false)
const report = ref(null)
const project = ref(null)
const questionnaire = ref(null)
const responseCount = ref(0)
const chartInstances = ref([])

// Excel上传相关
const uploadDialogVisible = ref(false)
const uploading = ref(false)
const selectedFile = ref(null)
const fileList = ref([])
const uploadRef = ref(null)

// 返回上一步
const handleBackToProjects = () => {
  router.push('/projects')
}

const handleGenerate = async () => {
  try {
    if (responseCount.value === 0) {
      ElMessage.warning('暂无回复数据，无法生成报告')
      return
    }
    
    generating.value = true
    ElMessage.info('正在生成分析报告...')
    
    const res = await generateReport(questionnaire.value.id)
    report.value = res.data
    
    ElMessage.success('报告生成成功')
    
    // 渲染图表
    await nextTick()
    initCharts()
  } catch (error) {
    console.error('生成报告失败:', error)
  } finally {
    generating.value = false
  }
}

const handleRegenerate = async () => {
  try {
    await ElMessageBox.confirm('确定要重新生成报告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    report.value = null
    handleGenerate()
  } catch (error) {
    // User cancelled
  }
}

// 打开问卷网实时数据报表
const handleOpenWenjuanReport = async () => {
  try {
    loadingWenjuanReport.value = true
    ElMessage.info('正在获取问卷网报表链接...')
    
    // 调用后端API获取报表链接
    const result = await getWenjuanReportUrl(questionnaire.value.id)
    
    if (result.data) {
      // 在新窗口打开问卷网报表
      window.open(result.data, '_blank')
      ElMessage.success('已打开问卷网实时数据报表')
    } else {
      ElMessage.error('获取报表链接失败')
    }
  } catch (error) {
    console.error('打开问卷网报表失败:', error)
    ElMessage.error('获取报表链接失败: ' + error.message)
  } finally {
    loadingWenjuanReport.value = false
  }
}

// 显示上传对话框
const handleShowUploadDialog = () => {
  uploadDialogVisible.value = true
  selectedFile.value = null
  fileList.value = []
}

// 文件选择变化
const handleFileChange = (file, fileListParam) => {
  // 验证文件类型
  const isExcel = file.name.endsWith('.xlsx') || file.name.endsWith('.xls')
  if (!isExcel) {
    ElMessage.error('只支持 .xlsx 和 .xls 格式的Excel文件')
    fileList.value = []
    selectedFile.value = null
    return
  }

  // 验证文件大小 (10MB)
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
    fileList.value = []
    selectedFile.value = null
    return
  }

  selectedFile.value = file.raw
  fileList.value = [file]
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 上传并生成报告
const handleUploadAndGenerate = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择Excel文件')
    return
  }

  try {
    uploading.value = true
    ElMessage.info('正在上传文件并解析数据...')

    // 创建FormData
    const formData = new FormData()
    formData.append('file', selectedFile.value)
    formData.append('questionnaireId', questionnaire.value.id)

    // 使用代理路径调用后端API
    const response = await fetch('/api/report/generate-from-excel', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formData
    })

    if (!response.ok) {
      let errorMessage = '生成报告失败'
      try {
        const errorData = await response.json()
        errorMessage = errorData.message || errorMessage
      } catch (e) {
        errorMessage = `HTTP ${response.status}: ${response.statusText}`
      }
      throw new Error(errorMessage)
    }

    const result = await response.json()
    
    if (result.code === 200 && result.data) {
      ElMessage.success('报告生成成功！')
      uploadDialogVisible.value = false
      report.value = result.data
      
      // 等待DOM更新后渲染图表
      await nextTick()
      renderCharts()
    } else {
      throw new Error(result.message || '生成报告失败')
    }
  } catch (error) {
    console.error('生成报告失败:', error)
    ElMessage.error('生成报告失败: ' + error.message)
  } finally {
    uploading.value = false
  }
}

// 渲染图表（已移除，不再需要图表）
const renderCharts = () => {
  console.log('已移除图表可视化功能')
}

// 导出PDF
const handleExportPdf = async () => {
  try {
    ElMessage.info('正在生成PDF，请稍候...')
    
    const element = document.querySelector('.report-section')
    if (!element) {
      ElMessage.error('报告内容未找到')
      return
    }
    
    // 使用html2canvas截图
    const canvas = await html2canvas(element, {
      scale: 2,
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff'
    })
    
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    
    const pdfWidth = pdf.internal.pageSize.getWidth()
    const pdfHeight = pdf.internal.pageSize.getHeight()
    const imgWidth = canvas.width
    const imgHeight = canvas.height
    const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight)
    
    const imgX = (pdfWidth - imgWidth * ratio) / 2
    const imgY = 10
    
    pdf.addImage(imgData, 'PNG', imgX, imgY, imgWidth * ratio, imgHeight * ratio)
    
    const fileName = `${report.value.reportTitle || '分析报告'}_${new Date().toLocaleDateString()}.pdf`
    pdf.save(fileName)
    
    ElMessage.success('PDF导出成功')
  } catch (error) {
    console.error('导出PDF失败:', error)
    ElMessage.error('PDF导出失败，请重试')
  }
}

// 导出Excel
const handleExportExcel = async () => {
  try {
    ElMessage.info('正在生成Excel，请稍候...')
    
    // 创建工作簿
    const wb = XLSX.utils.book_new()
    
    // 报告摘要工作表
    const summaryData = [
      ['报告标题', report.value.reportTitle],
      ['生成时间', new Date().toLocaleString()],
      [''],
      ['统计指标', ''],
      ['总回复数', report.value.totalResponses],
      ['有效回复数', report.value.validResponses],
      ['完成率', `${report.value.completionRate}%`],
      ['平均耗时', `${report.value.averageDuration}秒`],
      [''],
      ['偏好分析总结', ''],
      [report.value.preferenceSummary],
      [''],
      ['关键发现', ''],
      [report.value.keyFindings]
    ]
    
    const ws1 = XLSX.utils.aoa_to_sheet(summaryData)
    
    // 设置列宽
    ws1['!cols'] = [{ wch: 20 }, { wch: 60 }]
    
    XLSX.utils.book_append_sheet(wb, ws1, '报告摘要')
    
    // 问卷数据工作表（如果有详细数据）
    if (questionnaire.value && questionnaire.value.questions) {
      const questionData = [['题号', '题目', '题型', '是否必填']]
      questionnaire.value.questions.forEach((q, index) => {
        questionData.push([
          index + 1,
          q.questionText,
          q.questionType,
          q.isRequired ? '是' : '否'
        ])
      })
      
      const ws2 = XLSX.utils.aoa_to_sheet(questionData)
      ws2['!cols'] = [{ wch: 8 }, { wch: 50 }, { wch: 15 }, { wch: 10 }]
      XLSX.utils.book_append_sheet(wb, ws2, '问卷题目')
    }
    
    // 导出文件
    const fileName = `${report.value.reportTitle || '分析报告'}_${new Date().toLocaleDateString()}.xlsx`
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('Excel导出成功')
  } catch (error) {
    console.error('导出Excel失败:', error)
    ElMessage.error('Excel导出失败，请重试')
  }
}

// 格式化分析内容 - 支持旧格式自动转换
const formatAnalysisContent = (content) => {
  if (!content) return ''
  
  let formatted = content.trim()
  
  // ========== 第一步：深度清洗 ==========
  
  // 1. 移除所有前缀开场白（多行处理）
  formatted = formatted.replace(/^(好的[，,。.！!]?|我将|让我|下面|以下是|根据|基于|首先|现在)[^【##\n]*[\n]*/gi, '')
  formatted = formatted.replace(/^[^【##]*按照[^【##]*[\n]+/gi, '')
  
  // 2. 移除所有Markdown分隔线
  formatted = formatted.replace(/^-{3,}$/gm, '')
  formatted = formatted.replace(/^={3,}$/gm, '')
  
  // 3. 处理Markdown标题 ##、# 转换为【】格式
  // ## 1. 患者偏好的主要趋势 -> 【患者偏好主要趋势】
  formatted = formatted.replace(/^##?\s*\d+\.\s*(.+)$/gm, '【$1】')
  formatted = formatted.replace(/^##?\s+(.+)$/gm, '【$1】')
  
  // 4. 移除所有Markdown加粗符号 **
  formatted = formatted.replace(/\*\*([^*]+)\*\*/g, '<span class="inline-highlight">$1</span>')
  
  // 5. 处理特殊符号列表 ①、②
  formatted = formatted.replace(/[①②③④⑤⑥⑦⑧⑨⑩]/g, (match) => {
    const num = '①②③④⑤⑥⑦⑧⑨⑩'.indexOf(match) + 1
    return `${num}. `
  })
  
  // 6. 移除结尾的引导性问句（更严格）
  formatted = formatted.replace(/[。.！!]*\s*(如果您提供|您是否希望|是否需要|请问是否|您需要|我可以进一步|您想|是否希望)[^]*$/gi, '')
  
  // 7. 清理多余空行
  formatted = formatted.replace(/\n{3,}/g, '\n\n')
  
  // ========== 第二步：智能格式转换 ==========
  
  // 处理【】标题
  formatted = formatted.replace(/【([^】]+)】/g, '<div class="section-title"><span class="title-icon">📊</span>$1</div>')
  
  // 处理「」关键词
  formatted = formatted.replace(/「([^」]+)」/g, '<span class="keyword">$1</span>')
  
  // ========== 第三步：逐行处理生成HTML ==========
  
  const lines = formatted.split('\n')
  let result = []
  let listItems = []
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    // 空行处理
    if (!line) {
      // 如果有未输出的列表，先输出
      if (listItems.length > 0) {
        result.push('<div class="list-container">' + listItems.join('') + '</div>')
        listItems = []
      }
      continue
    }
    
    // 检查是否为标题
    if (line.includes('<div class="section-title">')) {
      // 输出之前的列表
      if (listItems.length > 0) {
        result.push('<div class="list-container">' + listItems.join('') + '</div>')
        listItems = []
      }
      result.push(line)
      continue
    }
    
    // 检查是否为列表项（数字. 开头，包括中文数字和①②③）
    const listMatch = line.match(/^(\d+)\.\s+(.+)$/)
    if (listMatch) {
      const number = listMatch[1]
      const text = listMatch[2]
      listItems.push(`
        <div class="list-item">
          <span class="list-number">${number}</span>
          <span class="list-text">${text}</span>
        </div>
      `)
      continue
    }
    
    // 普通段落
    if (listItems.length > 0) {
      result.push('<div class="list-container">' + listItems.join('') + '</div>')
      listItems = []
    }
    
    // 段落文本，保持原有的HTML标签
    if (line.includes('<span class="inline-highlight">')) {
      result.push(`<p class="paragraph">${line}</p>`)
    } else {
      result.push(`<p class="paragraph">${line}</p>`)
    }
  }
  
  // 输出最后的列表
  if (listItems.length > 0) {
    result.push('<div class="list-container">' + listItems.join('') + '</div>')
  }
  
  return result.join('')
}

// 初始化图表
const initCharts = () => {
  // 清除旧图表
  chartInstances.value.forEach(chart => chart.dispose())
  chartInstances.value = []
  
  // 完成率饼图
  const completionChart = echarts.init(document.getElementById('completion-chart'))
  const completionOption = {
    title: {
      text: '问卷完成情况',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 10,
      left: 'center'
    },
    series: [
      {
        name: '完成情况',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: report.value.validResponses, name: '有效回复', itemStyle: { color: '#667eea' } },
          { 
            value: report.value.totalResponses - report.value.validResponses, 
            name: '无效回复',
            itemStyle: { color: '#e9ecef' }
          }
        ]
      }
    ]
  }
  completionChart.setOption(completionOption)
  chartInstances.value.push(completionChart)
  
  // 回复趋势柱状图（示例数据）
  const trendChart = echarts.init(document.getElementById('trend-chart'))
  const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  const trendOption = {
    title: {
      text: '每日回复趋势',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: days,
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#999'
        }
      }
    },
    series: [
      {
        name: '回复数',
        type: 'bar',
        data: days.map(() => Math.floor(Math.random() * report.value.totalResponses / 2)),
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#667eea' },
            { offset: 1, color: '#764ba2' }
          ]),
          borderRadius: [5, 5, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#764ba2' },
              { offset: 1, color: '#667eea' }
            ])
          }
        }
      }
    ]
  }
  trendChart.setOption(trendOption)
  chartInstances.value.push(trendChart)
  
  // 设备类型分布饼图（示例数据）
  const deviceChart = echarts.init(document.getElementById('device-chart'))
  const deviceOption = {
    title: {
      text: '设备类型分布',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 16,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      bottom: 10,
      left: 'center'
    },
    series: [
      {
        name: '设备类型',
        type: 'pie',
        radius: '65%',
        center: ['50%', '50%'],
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        data: [
          { value: Math.floor(report.value.totalResponses * 0.6), name: 'PC', itemStyle: { color: '#667eea' } },
          { value: Math.floor(report.value.totalResponses * 0.3), name: '手机', itemStyle: { color: '#38ef7d' } },
          { value: Math.floor(report.value.totalResponses * 0.1), name: '平板', itemStyle: { color: '#f093fb' } }
        ]
      }
    ]
  }
  deviceChart.setOption(deviceOption)
  chartInstances.value.push(deviceChart)
  
  // 响应式调整
  window.addEventListener('resize', () => {
    chartInstances.value.forEach(chart => chart.resize())
  })
}

const loadData = async () => {
  try {
    const [pRes, qRes] = await Promise.all([
      getProject(route.params.id),
      getQuestionnaireByProject(route.params.id)
    ])
    
    project.value = pRes.data
    questionnaire.value = qRes.data
    
    if (questionnaire.value) {
      // 获取回复数 - 优先同步问卷网数据
      let currentResponseCount = 0
      
      if (questionnaire.value.wenjuanShortId) {
        // 如果已发布到问卷网，先同步最新数据
        try {
          console.log('Step6: 同步问卷网数据，shortId:', questionnaire.value.wenjuanShortId)
          const syncRes = await syncWenjuanData(questionnaire.value.id)
          currentResponseCount = syncRes.data || 0
          console.log('Step6: 同步成功，答卷数:', currentResponseCount)
        } catch (error) {
          console.warn('Step6: 同步问卷网数据失败，使用缓存数据:', error)
          currentResponseCount = questionnaire.value.wenjuanRespondentCount || 0
        }
      } else {
        // 使用本地回复数
        try {
          const rRes = await getResponseCount(questionnaire.value.id)
          currentResponseCount = rRes.data || 0
        } catch (error) {
          currentResponseCount = 0
        }
      }
      
      responseCount.value = currentResponseCount
      
      // 获取报告
      try {
        const repRes = await getReportByQuestionnaire(questionnaire.value.id)
        if (repRes.data) {
          report.value = repRes.data
          // 如果报告已存在，初始化图表
          await nextTick()
          initCharts()
        }
      } catch (error) {
        console.log('暂无报告数据')
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    ElMessage.error('加载数据失败')
  }
}

onMounted(() => {
  loadData()
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
  width: 800px;
  height: 800px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  top: -400px;
  right: -300px;
  z-index: 0;
  pointer-events: none;
}

.step-card {
  max-width: 1100px;
  margin: 0 auto 80px auto;
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.5s ease-out;
  position: relative;
  z-index: 1;
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
  padding: 24px 32px;
  position: relative;
  z-index: 150;
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
  z-index: 200;
}

.card-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
  position: relative;
  z-index: 200;
}

.card-header .el-button {
  position: relative;
  z-index: 200;
  cursor: pointer !important;
  pointer-events: auto !important;
}

.generate-section {
  padding: 50px 36px;
  text-align: center;
}

:deep(.generate-section .el-alert) {
  border-radius: 12px;
  margin-bottom: 36px;
}

.stats-info {
  display: flex;
  justify-content: center;
  gap: 60px;
  margin: 36px 0;
}

:deep(.stats-info .el-statistic) {
  text-align: center;
}

:deep(.stats-info .el-statistic__head) {
  font-size: 15px;
  font-weight: 600;
  color: #666;
  margin-bottom: 10px;
}

:deep(.stats-info .el-statistic__content) {
  font-size: 42px;
  font-weight: 700;
  color: #667eea;
}

.generate-actions {
  margin-top: 36px;
  display: flex;
  gap: 20px;
  justify-content: center;
  flex-wrap: wrap;
}

:deep(.generate-actions .el-button) {
  height: 46px;
  padding: 0 40px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 23px;
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

:deep(.generate-actions .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

:deep(.generate-actions .el-button--success) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  box-shadow: 0 8px 20px rgba(17, 153, 142, 0.3);
}

:deep(.generate-actions .el-button:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.4);
}

:deep(.generate-actions .el-button--success:hover) {
  box-shadow: 0 12px 28px rgba(17, 153, 142, 0.4);
}

:deep(.generate-actions .el-button.is-disabled) {
  opacity: 0.5;
  cursor: not-allowed;
}

.tips-section {
  margin-top: 32px;
  animation: fadeIn 0.6s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.report-section {
  padding: 28px 36px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  z-index: 10;
}

.report-header h3 {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
}

.export-buttons {
  display: flex;
  gap: 12px;
  position: relative;
  z-index: 10;
}

:deep(.export-buttons .el-button) {
  height: 38px;
  padding: 0 24px;
  font-weight: 600;
  border-radius: 19px;
  transition: all 0.3s;
  position: relative;
  z-index: 10;
  cursor: pointer;
  pointer-events: auto;
}

:deep(.export-buttons .el-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.export-buttons .el-button--success) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
}

:deep(.el-divider) {
  margin: 24px 0;
}

.stats-row {
  margin: 32px 0;
}

.stat-card {
  text-align: center;
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s;
}

.stat-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
}

:deep(.stat-card .el-card__body) {
  padding: 24px;
}

:deep(.stat-card .el-statistic__head) {
  font-size: 14px;
  font-weight: 600;
  color: #666;
  margin-bottom: 12px;
}

:deep(.stat-card .el-statistic__content) {
  font-size: 36px;
  font-weight: 700;
  color: #667eea;
}

.analysis-card,
.findings-card,
.charts-card {
  margin-top: 24px;
  border-radius: 16px;
  border: 2px solid #e9ecef;
}

:deep(.analysis-card .el-card__header),
:deep(.findings-card .el-card__header),
:deep(.charts-card .el-card__header) {
  background: #fafbfc;
  border-bottom: 2px solid #e9ecef;
  padding: 20px 24px;
}

:deep(.analysis-card h4),
:deep(.findings-card h4),
:deep(.charts-card h4) {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 0;
}

.analysis-content,
.findings-content {
  padding: 24px;
}

.analysis-content {
  line-height: 2;
  color: #2c3e50;
  font-size: 15px;
  background: linear-gradient(to bottom, #ffffff 0%, #f9fafb 100%);
  border-radius: 8px;
}

/* 章节标题样式 - 更加显眼 */
:deep(.analysis-content .section-title) {
  font-size: 18px;
  font-weight: 700;
  color: #2c3e50;
  margin: 32px 0 20px 0;
  padding: 14px 18px 14px 16px;
  border-left: 5px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-radius: 0 8px 8px 0;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.1);
  position: relative;
  transition: all 0.3s ease;
}

:deep(.analysis-content .section-title:hover) {
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.18);
}

:deep(.analysis-content .section-title:first-child) {
  margin-top: 0;
}

/* 标题图标 */
:deep(.analysis-content .title-icon) {
  margin-right: 8px;
  font-size: 16px;
  display: inline-block;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

/* 关键词样式 - 更突出 */
:deep(.analysis-content .keyword) {
  color: #764ba2;
  font-weight: 600;
  padding: 3px 8px;
  background: linear-gradient(135deg, rgba(118, 75, 162, 0.12) 0%, rgba(102, 126, 234, 0.08) 100%);
  border-radius: 4px;
  margin: 0 3px;
  border: 1px solid rgba(118, 75, 162, 0.2);
  display: inline-block;
  transition: all 0.2s;
}

:deep(.analysis-content .keyword:hover) {
  background: linear-gradient(135deg, rgba(118, 75, 162, 0.18) 0%, rgba(102, 126, 234, 0.12) 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(118, 75, 162, 0.15);
}

/* 内联高亮（原Markdown加粗） */
:deep(.analysis-content .inline-highlight) {
  color: #e74c3c;
  font-weight: 600;
  padding: 2px 4px;
  background: rgba(231, 76, 60, 0.08);
  border-radius: 3px;
}

/* 段落样式 - 更清晰 */
:deep(.analysis-content .paragraph) {
  margin: 16px 0;
  padding: 0 8px;
  text-indent: 2em;
  text-align: justify;
  color: #4a5568;
  line-height: 2;
  font-size: 15px;
}

/* 列表容器 */
:deep(.analysis-content .list-container) {
  margin: 16px 0;
  padding: 12px;
  background: #ffffff;
  border-radius: 10px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}

/* 列表项样式 - 更精致 */
:deep(.analysis-content .list-item) {
  display: flex;
  align-items: flex-start;
  margin: 12px 0;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
  border-radius: 8px;
  border-left: 3px solid transparent;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

:deep(.analysis-content .list-item::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  width: 3px;
  height: 100%;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
  opacity: 0;
  transition: opacity 0.3s;
}

:deep(.analysis-content .list-item:hover) {
  background: linear-gradient(135deg, #f0f2ff 0%, #fefeff 100%);
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.12);
}

:deep(.analysis-content .list-item:hover::before) {
  opacity: 1;
}

:deep(.analysis-content .list-number) {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  margin-right: 16px;
  box-shadow: 0 3px 8px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

:deep(.analysis-content .list-item:hover .list-number) {
  transform: rotate(360deg) scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

:deep(.analysis-content .list-text) {
  flex: 1;
  color: #4a5568;
  line-height: 1.8;
  padding-top: 4px;
  font-size: 15px;
}

.findings-content pre {
  line-height: 1.8;
  color: #555;
  font-size: 15px;
  white-space: pre-wrap;
  margin: 0;
  font-family: inherit;
}

/* ========== 临床建议卡片样式（核心内容）========== */
.clinical-advice-card {
  margin-top: 30px;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #f5f9ff 0%, #fafffc 100%);
}

:deep(.clinical-content) {
  font-size: 16px;
  line-height: 2;
  color: #2c3e50;
  padding: 10px;
}

:deep(.clinical-content h2) {
  color: #67C23A !important;
  font-size: 24px;
  margin-top: 30px;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 3px solid #67C23A;
  font-weight: bold;
}

:deep(.clinical-content h3) {
  color: #409EFF !important;
  font-size: 18px;
  margin-top: 25px;
  margin-bottom: 15px;
  font-weight: bold;
}

:deep(.clinical-content strong) {
  color: #E6A23C !important;
  font-weight: 700;
  font-size: 17px;
}

:deep(.clinical-content ul),
:deep(.clinical-content ol) {
  margin: 15px 0;
  padding-left: 30px;
}

:deep(.clinical-content li) {
  margin: 10px 0;
  line-height: 1.8;
}

:deep(.clinical-content table) {
  width: 100%;
  border-collapse: collapse;
  margin: 20px 0;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  border-radius: 8px;
  overflow: hidden;
}

:deep(.clinical-content table th) {
  background: #67C23A;
  color: white;
  padding: 14px;
  text-align: left;
  font-weight: bold;
  font-size: 15px;
}

:deep(.clinical-content table td) {
  padding: 12px 14px;
  border-bottom: 1px solid #EBEEF5;
  font-size: 15px;
}

:deep(.clinical-content table tr:hover) {
  background: #f5f7fa;
}

:deep(.clinical-content hr) {
  border: none;
  border-top: 2px solid #DCDFE6;
  margin: 30px 0;
}

:deep(.clinical-content p) {
  margin: 12px 0;
  line-height: 1.9;
}

.charts-content {
  padding: 20px;
}

.chart-item {
  height: 350px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  position: relative;
  z-index: 1;
}

.chart-item:hover {
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
}

.complete-actions {
  text-align: center;
  margin-top: 48px;
  padding-top: 32px;
  padding-bottom: 40px;
  border-top: 2px solid #dee2e6;
  position: relative;
  z-index: 10;
}

:deep(.complete-actions .el-button) {
  height: 44px;
  padding: 0 32px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 22px;
  transition: all 0.3s;
  margin: 0 8px;
  position: relative;
  z-index: 10;
  cursor: pointer;
  pointer-events: auto;
}

:deep(.complete-actions .el-button--success) {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(17, 153, 142, 0.3);
}

:deep(.complete-actions .el-button--success:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(17, 153, 142, 0.4);
}

:deep(.complete-actions .el-button--default) {
  border: 2px solid #e4e7ed;
}

:deep(.complete-actions .el-button--default:hover) {
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
  
  .report-section {
    padding: 24px 20px;
  }
  
  .report-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .stats-info {
    flex-direction: column;
    gap: 32px;
  }
  
  .export-buttons {
    width: 100%;
    flex-direction: column;
  }
  
  :deep(.export-buttons .el-button) {
    width: 100%;
  }
  
  .chart-item {
    height: 300px;
    margin-bottom: 20px;
  }
  
  :deep(.charts-content .el-col) {
    margin-bottom: 20px;
  }
}
</style>

