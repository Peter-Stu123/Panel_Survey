<template>
  <div class="survey-container">
    <el-card class="survey-card">
      <div v-if="loading" class="loading-section">
        <el-empty description="正在加载问卷..." />
      </div>
      
      <div v-else-if="!questionnaire" class="error-section">
        <el-empty description="问卷不存在或已关闭" />
      </div>
      
      <div v-else-if="submitted" class="success-section">
        <el-result
          icon="success"
          title="提交成功"
          sub-title="感谢您的参与！"
        >
          <template #extra>
            <el-button type="primary" @click="$router.push('/')">返回首页</el-button>
          </template>
        </el-result>
      </div>
      
      <div v-else class="survey-content">
        <div class="survey-header">
          <h2>{{ questionnaire.title }}</h2>
          <p class="description">{{ questionnaire.description }}</p>
          <el-divider />
          <div class="introduction" v-html="formatIntroduction(questionnaire.introduction)"></div>
          <div class="instruction">
            <el-alert
              :title="questionnaire.instruction"
              type="info"
              :closable="false"
              show-icon
            />
          </div>
        </div>
        
        <el-form
          ref="formRef"
          :model="answers"
          class="survey-form"
        >
          <div
            v-for="(question, index) in questionnaire.questions"
            :key="question.id"
            class="question-item"
          >
            <div class="question-header">
              <span class="question-number">{{ index + 1 }}</span>
              <span v-if="question.isRequired" class="required-mark">*</span>
            </div>
            
            <h3 class="question-text">{{ question.questionText }}</h3>
            
            <p v-if="question.questionDescription" class="question-desc">
              {{ question.questionDescription }}
            </p>
            
            <!-- 单选题 -->
            <el-form-item
              v-if="question.questionType === 'SINGLE_CHOICE'"
              :prop="`q_${question.id}`"
              :rules="question.isRequired ? [{ required: true, message: '此题为必填项' }] : []"
            >
              <el-radio-group
                v-model="answers[`q_${question.id}`]"
              >
                <el-radio
                  v-for="option in question.options"
                  :key="option.value"
                  :label="option.value"
                  border
                >
                  {{ option.label }}
                </el-radio>
              </el-radio-group>
            </el-form-item>
            
            <!-- 多选题 -->
            <el-form-item
              v-else-if="question.questionType === 'MULTIPLE_CHOICE'"
              :prop="`q_${question.id}`"
              :rules="question.isRequired ? [{ required: true, type: 'array', min: 1, message: '此题为必填项' }] : []"
            >
              <el-checkbox-group
                v-model="answers[`q_${question.id}`]"
              >
                <el-checkbox
                  v-for="option in question.options"
                  :key="option.value"
                  :label="option.value"
                  border
                >
                  {{ option.label }}
                </el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            
            <!-- 评分题 -->
            <div v-else-if="question.questionType === 'RATING'" class="rating-group">
              <div
                v-for="option in question.options"
                :key="option.value"
                class="rating-item"
              >
                <el-form-item
                  :prop="`q_${question.id}_${option.value}`"
                  :rules="question.isRequired ? [{ required: true, type: 'number', min: 1, message: '请为此项评分' }] : []"
                >
                  <label>{{ option.label }}：</label>
                  <el-rate
                    v-model="answers[`q_${question.id}_${option.value}`]"
                    :max="5"
                    show-text
                  />
                </el-form-item>
              </div>
            </div>
            
            <!-- 简答题 -->
            <el-form-item
              v-else-if="question.questionType === 'TEXT'"
              :prop="`q_${question.id}`"
              :rules="question.isRequired ? [{ required: true, message: '此题为必填项' }] : []"
            >
              <el-input
                v-model="answers[`q_${question.id}`]"
                type="textarea"
                :rows="4"
                placeholder="请输入您的答案"
              />
            </el-form-item>
          </div>
          
          <div class="submit-section">
            <el-button
              type="primary"
              size="large"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交问卷
            </el-button>
          </div>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPublicQuestionnaire } from '../api/questionnaire'
import { submitResponse } from '../api/response'

const route = useRoute()
const formRef = ref(null)

const loading = ref(true)
const submitting = ref(false)
const submitted = ref(false)
const questionnaire = ref(null)
const answers = reactive({})
const startTime = Date.now()

// 转换Markdown表格为HTML表格
const convertMarkdownTable = (text) => {
  console.log('开始转换Markdown表格')
  
  // 提取治疗方案说明部分（包含表格）
  const tableStart = text.indexOf('治疗方案对比说明')
  const tableEnd = text.indexOf('【可能的副作用】')
  
  if (tableStart === -1) return text
  
  const beforeTable = text.substring(0, tableStart)
  const tableSection = text.substring(tableStart, tableEnd > 0 ? tableEnd : text.length)
  const afterTable = tableEnd > 0 ? text.substring(tableEnd) : ''
  
  console.log('表格区域:', tableSection.substring(0, 200))
  
  // 解析表格内容
  const lines = tableSection.split('\n').map(l => l.trim()).filter(l => l)
  
  let tableHtml = '<div class="treatment-table-wrapper">'
  tableHtml += '<table class="treatment-table"><thead><tr>'
  tableHtml += '<th>对比项</th><th>开放性手术</th><th>保守治疗</th>'
  tableHtml += '</tr></thead><tbody>'
  
  // 提取各个维度的数据
  const dimensions = [
    { title: '治疗方式', icon: '📋' },
    { title: '适用情况', icon: '📋' },
    { title: '恢复过程', icon: '📋' },
    { title: '风险与干预', icon: '📋' }
  ]
  
  for (const dim of dimensions) {
    // 查找该维度的内容
    const dimIndex = lines.findIndex(l => l.includes(dim.title))
    if (dimIndex >= 0 && dimIndex + 1 < lines.length) {
      const nextLine = lines[dimIndex + 1]
      // 提取表格单元格内容 | xxx | yyy ||
      const cells = nextLine.split('|').map(c => c.trim()).filter(c => c)
      
      if (cells.length >= 2) {
        tableHtml += '<tr>'
        tableHtml += `<td class="category-cell"><strong>${dim.title}</strong></td>`
        tableHtml += `<td class="content-cell">${cells[0]}</td>`
        tableHtml += `<td class="content-cell">${cells[1]}</td>`
        tableHtml += '</tr>'
      }
    }
  }
  
  tableHtml += '</tbody></table></div>'
  
  console.log('表格HTML生成完成')
  
  return beforeTable + '【治疗方案说明】\n\n' + tableHtml + '\n\n' + afterTable
}

// 格式化引言内容 - 与Step6保持一致的样式
const formatIntroduction = (content) => {
  if (!content) return ''
  
  console.log('=== 开始格式化引言 ===')
  console.log('内容长度:', content.length)
  console.log('前100字符:', content.substring(0, 100))
  
  // 检查是否包含表格标记
  if (content.includes('##TABLE_START##')) {
    console.log('检测到表格标记，使用表格解析')
    return parseIntroductionWithTable(content)
  }
  
  console.log('未检测到表格标记，使用普通格式化')
  
  let formatted = content.trim()
  
  // 清理AI前缀（如果有）
  formatted = formatted.replace(/^(好的[，,。！!]*|以下是[^：:]*[：:]|---\s*)+/g, '')
  formatted = formatted.replace(/\*\*引言\*\*/g, '')
  formatted = formatted.replace(/^引言\s*/g, '')
  
  // 删除AI询问性后缀（包含问号的段落）
  const paragraphs = formatted.split('\n\n')
  const cleanedParagraphs = []
  for (const para of paragraphs) {
    // 跳过包含"如果您需要"、"您要我"等询问的段落
    if (para.includes('如果您需要') || para.includes('您要我') || 
        para.includes('您是否希望') || para.includes('我可以帮您')) {
      console.log('删除AI询问段落:', para.substring(0, 50))
      continue
    }
    // 跳过包含问号的最后段落
    if (para.includes('？') || para.includes('?')) {
      console.log('删除包含问号的段落:', para.substring(0, 50))
      continue
    }
    cleanedParagraphs.push(para)
  }
  formatted = cleanedParagraphs.join('\n\n')
  
  // 检查是否包含Markdown表格
  if (formatted.includes('| 方面 |') || formatted.includes('| 方案A') || formatted.includes('||||')) {
    console.log('检测到Markdown表格，进行转换')
    formatted = convertMarkdownTable(formatted)
  }
  
  // 处理【】标题
  formatted = formatted.replace(/【([^】]+)】/g, '<div class="section-title"><span class="title-icon">📋</span>$1</div>')
  
  // 处理「」关键词
  formatted = formatted.replace(/「([^」]+)」/g, '<span class="keyword">$1</span>')
  
  // 处理段落
  const lines = formatted.split('\n')
  let result = []
  let listItems = []
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i].trim()
    
    if (!line || line === '---') {
      if (listItems.length > 0) {
        result.push('<div class="list-container">' + listItems.join('') + '</div>')
        listItems = []
      }
      continue
    }
    
    // 检查是否为标题
    if (line.includes('<div class="section-title">')) {
      if (listItems.length > 0) {
        result.push('<div class="list-container">' + listItems.join('') + '</div>')
        listItems = []
      }
      result.push(line)
      continue
    }
    
    // 检查是否为列表项
    const listMatch = line.match(/^(\d+)\.\s+(.+)$/)
    if (listMatch) {
      listItems.push(`
        <div class="list-item">
          <span class="list-number">${listMatch[1]}</span>
          <span class="list-text">${listMatch[2]}</span>
        </div>
      `)
      continue
    }
    
    // 普通段落
    if (listItems.length > 0) {
      result.push('<div class="list-container">' + listItems.join('') + '</div>')
      listItems = []
    }
    result.push(`<p class="paragraph">${line}</p>`)
  }
  
  if (listItems.length > 0) {
    result.push('<div class="list-container">' + listItems.join('') + '</div>')
  }
  
  console.log('格式化完成，结果长度:', result.join('').length)
  return result.join('')
}

// 解析包含表格的引言
const parseIntroductionWithTable = (text) => {
  console.log('开始解析表格，原始文本长度:', text.length)
  console.log('是否包含TABLE_START标记:', text.includes('##TABLE_START##'))
  
  const result = []
  const parts = text.split('##TABLE_START##')
  
  console.log('分割后的parts数量:', parts.length)
  
  // 添加表格前的内容
  if (parts[0]) {
    const preContent = parts[0].trim()
    let formatted = preContent.replace(/【([^】]+)】/g, '<div class="section-title"><span class="title-icon">📋</span>$1</div>')
    const lines = formatted.split('\n').filter(line => line.trim())
    for (let line of lines) {
      if (line.includes('<div class="section-title">')) {
        result.push(line)
      } else {
        result.push(`<p class="paragraph">${line.trim()}</p>`)
      }
    }
  }
  
  // 解析表格
  if (parts.length > 1) {
    const tableParts = parts[1].split('##TABLE_END##')
    const tableContent = tableParts[0].trim()
    const tableLines = tableContent.split('\n').filter(line => line.trim())
    
    console.log('表格内容行数:', tableLines.length)
    console.log('表格第一行（表头）:', tableLines[0])
    
    if (tableLines.length > 0) {
      result.push('<div class="treatment-table-wrapper">')
      result.push('<table class="treatment-table">')
      result.push('<thead>')
      result.push('<tr>')
      
      // 表头
      const headers = tableLines[0].split('|')
      console.log('表头列数:', headers.length)
      headers.forEach(header => {
        result.push(`<th>${header.trim()}</th>`)
      })
      
      result.push('</tr>')
      result.push('</thead>')
      result.push('<tbody>')
      
      // 表格内容
      for (let i = 1; i < tableLines.length; i++) {
        const cells = tableLines[i].split('|')
        console.log(`第${i}行数据列数:`, cells.length, '内容:', cells)
        result.push('<tr>')
        cells.forEach((cell, index) => {
          if (index === 0) {
            result.push(`<td class="category-cell"><strong>${cell.trim()}</strong></td>`)
          } else {
            result.push(`<td class="content-cell">${cell.trim()}</td>`)
          }
        })
        result.push('</tr>')
      }
      
      result.push('</tbody>')
      result.push('</table>')
      result.push('</div>')
      
      console.log('表格HTML生成完成')
    } else {
      console.log('警告：没有表格行数据')
    }
    
    // 添加表格后的内容
    if (tableParts[1]) {
      const postContent = tableParts[1].trim()
      let formatted = postContent.replace(/【([^】]+)】/g, '<div class="section-title"><span class="title-icon">📋</span>$1</div>')
      const lines = formatted.split('\n').filter(line => line.trim())
      for (let line of lines) {
        if (line.includes('<div class="section-title">')) {
          result.push(line)
        } else {
          result.push(`<p class="paragraph">${line.trim()}</p>`)
        }
      }
    }
  }
  
  return result.join('')
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    submitting.value = true
    
    const duration = Math.floor((Date.now() - startTime) / 1000)
    
    const answerMap = {}
    Object.keys(answers).forEach(key => {
      const questionId = parseInt(key.replace('q_', '').split('_')[0])
      if (!answerMap[questionId]) {
        answerMap[questionId] = answers[key]
      } else {
        if (typeof answerMap[questionId] === 'object') {
          answerMap[questionId][key.split('_')[2]] = answers[key]
        }
      }
    })
    
    const data = {
      questionnaireId: questionnaire.value.id,
      durationSeconds: duration,
      deviceType: getDeviceType(),
      answers: answerMap
    }
    
    console.log('提交数据:', data)
    
    const result = await submitResponse(data)
    console.log('提交结果:', result)
    
    submitted.value = true
    ElMessage.success('问卷提交成功，感谢您的参与！')
  } catch (error) {
    if (error !== 'validation failed') {
      console.error('提交问卷失败 - 详细错误:', error)
      console.error('错误信息:', error.message)
      console.error('错误响应:', error.response)
      
      let errorMsg = '提交失败，请稍后重试'
      if (error.response) {
        errorMsg = error.response.data?.message || error.response.data || errorMsg
      } else if (error.message) {
        errorMsg = error.message
      }
      ElMessage.error(errorMsg)
    }
  } finally {
    submitting.value = false
  }
}

const getDeviceType = () => {
  const ua = navigator.userAgent
  if (/mobile/i.test(ua)) return 'Mobile'
  if (/tablet/i.test(ua)) return 'Tablet'
  return 'Desktop'
}

const loadQuestionnaire = async () => {
  try {
    loading.value = true
    console.log('加载问卷ID:', route.params.id)
    const res = await getPublicQuestionnaire(route.params.id)
    console.log('问卷加载成功:', res.data)
    console.log('引言完整内容:', res.data.introduction)
    console.log('引言是否包含表格标记:', res.data.introduction?.includes('##TABLE_START##'))
    questionnaire.value = res.data
  } catch (error) {
    console.error('加载问卷失败 - 详细错误:', error)
    console.error('错误响应:', error.response)
    ElMessage.error('加载问卷失败，请检查链接是否正确')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadQuestionnaire()
})
</script>

<style scoped>
.survey-container {
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

.survey-container::before {
  content: '';
  position: absolute;
  width: 800px;
  height: 800px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 50%;
  top: -400px;
  right: -300px;
}

.survey-card {
  max-width: 900px;
  margin: 0 auto 80px auto;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: visible;
}

.loading-section,
.error-section,
.success-section {
  padding: 60px 20px;
  text-align: center;
}

.survey-content {
  padding: 28px 36px 60px 36px;
}

.survey-header {
  margin-bottom: 32px;
  text-align: center;
}

.survey-header h2 {
  font-size: 28px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.description {
  color: #666;
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 6px;
}

.introduction {
  background: linear-gradient(to bottom, #ffffff 0%, #f9fafb 100%);
  padding: 24px;
  border-radius: 10px;
  margin: 20px 0;
  border: 2px solid #e9ecef;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

/* 章节标题样式 */
:deep(.introduction .section-title) {
  font-size: 17px;
  font-weight: 700;
  color: #2c3e50;
  margin: 28px 0 16px 0;
  padding: 12px 16px;
  border-left: 4px solid #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-radius: 0 8px 8px 0;
  transition: all 0.3s;
}

:deep(.introduction .section-title:first-child) {
  margin-top: 0;
}

:deep(.introduction .title-icon) {
  margin-right: 8px;
  font-size: 15px;
}

/* 关键词样式 */
:deep(.introduction .keyword) {
  color: #764ba2;
  font-weight: 600;
  padding: 2px 7px;
  background: linear-gradient(135deg, rgba(118, 75, 162, 0.12) 0%, rgba(102, 126, 234, 0.08) 100%);
  border-radius: 4px;
  margin: 0 2px;
  border: 1px solid rgba(118, 75, 162, 0.2);
}

/* 段落样式 */
:deep(.introduction .paragraph) {
  margin: 14px 0;
  padding: 0 6px;
  text-align: justify;
  color: #4a5568;
  line-height: 1.9;
  font-size: 14px;
}

/* 列表容器 */
:deep(.introduction .list-container) {
  margin: 14px 0;
  padding: 10px;
  background: #ffffff;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

/* 列表项样式 */
:deep(.introduction .list-item) {
  display: flex;
  align-items: flex-start;
  margin: 10px 0;
  padding: 12px 14px;
  background: linear-gradient(135deg, #f8f9ff 0%, #ffffff 100%);
  border-radius: 6px;
  transition: all 0.3s;
}

:deep(.introduction .list-item:hover) {
  background: linear-gradient(135deg, #f0f2ff 0%, #fefeff 100%);
  transform: translateX(4px);
}

:deep(.introduction .list-number) {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 13px;
  margin-right: 14px;
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

:deep(.introduction .list-text) {
  flex: 1;
  color: #4a5568;
  line-height: 1.7;
  padding-top: 2px;
  font-size: 14px;
}

/* 治疗方案对比表格样式 */
:deep(.introduction .treatment-table-wrapper) {
  margin: 24px 0;
  overflow-x: auto;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

:deep(.introduction .treatment-table) {
  width: 100%;
  border-collapse: collapse;
  background: white;
  font-size: 14px;
  table-layout: auto;
}

:deep(.introduction .treatment-table thead th) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 16px 14px;
  text-align: center;
  font-weight: 700;
  font-size: 15px;
  border: 1px solid #5a67d8;
}

:deep(.introduction .treatment-table tbody td) {
  padding: 14px 16px;
  border: 1px solid #e9ecef;
  vertical-align: top;
  line-height: 1.8;
}

:deep(.introduction .treatment-table tbody tr) {
  transition: all 0.3s;
}

:deep(.introduction .treatment-table tbody tr:hover) {
  background: linear-gradient(135deg, #f8f9ff 0%, #fefeff 100%);
  transform: scale(1.01);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.15);
}

:deep(.introduction .treatment-table .category-cell) {
  background: linear-gradient(135deg, #f0f2ff 0%, #e6e9ff 100%);
  text-align: center;
  font-weight: 700;
  color: #667eea;
  min-width: 120px;
  font-size: 14px;
}

:deep(.introduction .treatment-table .content-cell) {
  line-height: 1.8;
  color: #2c3e50;
  text-align: justify;
  white-space: pre-wrap;
}

.instruction {
  margin: 20px 0;
}

:deep(.instruction .el-alert) {
  border-radius: 10px;
  padding: 14px 18px;
}

.survey-form {
  margin-top: 28px;
}

.question-item {
  background: #fafbfc;
  padding: 24px 28px;
  margin-bottom: 20px;
  border-radius: 12px;
  border: 2px solid #e9ecef;
  transition: all 0.3s;
}

.question-item:hover {
  border-color: #667eea;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.1);
}

.question-header {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.question-number {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-weight: 700;
  font-size: 15px;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.required-mark {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 700;
}

.question-text {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 10px;
  line-height: 1.6;
}

.question-desc {
  color: #666;
  font-size: 13px;
  margin-bottom: 20px;
  white-space: pre-wrap;
  line-height: 1.6;
  padding-left: 4px;
}

:deep(.el-radio),
:deep(.el-checkbox) {
  width: 100%;
  margin-right: 0;
  margin-bottom: 10px;
}

:deep(.el-radio.is-bordered),
:deep(.el-checkbox.is-bordered) {
  padding: 14px 18px;
  border-radius: 8px;
  border: 2px solid #e4e7ed;
  transition: all 0.3s;
}

:deep(.el-radio.is-bordered:hover),
:deep(.el-checkbox.is-bordered:hover) {
  border-color: #667eea;
  background: #f8f9ff;
}

:deep(.el-radio.is-checked.is-bordered),
:deep(.el-checkbox.is-checked.is-bordered) {
  border-color: #667eea;
  background: #f0f2ff;
}

.rating-group {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rating-item {
  display: flex;
  align-items: center;
  padding: 16px 20px 20px 20px;
  background: white;
  border-radius: 10px;
  border: 2px solid #e9ecef;
  transition: all 0.3s;
  position: relative;
}

.rating-item:hover {
  border-color: #667eea;
  box-shadow: 0 2px 12px rgba(102, 126, 234, 0.1);
}

.rating-item label {
  min-width: 160px;
  color: #2c3e50;
  font-weight: 600;
  font-size: 14px;
  margin-right: 20px;
}

/* 评分题的表单项样式 */
:deep(.rating-item .el-form-item) {
  margin-bottom: 0;
  width: 100%;
  display: flex;
  align-items: center;
}

:deep(.rating-item .el-form-item__content) {
  display: flex;
  align-items: center;
  width: 100%;
  margin-left: 0 !important;
}

:deep(.rating-item .el-form-item__error) {
  position: absolute;
  bottom: -22px;
  left: 0;
}

:deep(.el-textarea__inner) {
  border-radius: 10px;
  border: 2px solid #e4e7ed;
  padding: 14px;
  font-size: 14px;
  transition: all 0.3s;
}

:deep(.el-textarea__inner:hover) {
  border-color: #c0c4cc;
}

:deep(.el-textarea__inner:focus) {
  border-color: #667eea;
}

.submit-section {
  text-align: center;
  margin-top: 40px;
  padding-top: 30px;
  padding-bottom: 20px;
  border-top: 2px solid #dee2e6;
}

.submit-section .el-button {
  min-width: 180px;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 23px;
}

.submit-section .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s;
}

.submit-section .el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.4);
}

@media (max-width: 768px) {
  .survey-container {
    padding: 20px 16px;
  }
  
  .survey-card {
    max-width: 100%;
  }
  
  .survey-content {
    padding: 24px 20px 60px 20px;
  }
  
  .question-item {
    padding: 20px;
  }
  
  .rating-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .rating-item label {
    min-width: auto;
  }
}
</style>

