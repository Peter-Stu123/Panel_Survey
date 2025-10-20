<template>
  <div class="wenjuan-export-container">
    <!-- 顶部标题栏 -->
    <div class="export-header">
      <h2>📋 问卷内容导出 - 复制到问卷网</h2>
      <p class="subtitle">已创建空白项目，请按照以下步骤将问卷内容复制到问卷网</p>
    </div>

    <!-- 进度指示器 -->
    <div class="progress-steps">
      <div class="step completed">
        <div class="step-icon">✓</div>
        <div class="step-label">创建空白项目</div>
      </div>
      <div class="step" :class="{ active: currentStep >= 2 }">
        <div class="step-icon">2</div>
        <div class="step-label">复制问卷内容</div>
      </div>
      <div class="step" :class="{ active: currentStep >= 3 }">
        <div class="step-icon">3</div>
        <div class="step-label">打开编辑器</div>
      </div>
      <div class="step" :class="{ active: currentStep >= 4 }">
        <div class="step-icon">4</div>
        <div class="step-label">手动粘贴</div>
      </div>
      <div class="step" :class="{ active: currentStep >= 5 }">
        <div class="step-icon">5</div>
        <div class="step-label">保存发布</div>
      </div>
    </div>

    <!-- 项目信息卡片 -->
    <div class="info-card">
      <h3>📍 问卷网项目信息</h3>
      <div class="info-row">
        <span class="label">项目ID：</span>
        <span class="value">{{ wenjuanResult.surveyId }}</span>
        <button @click="copyToClipboard(wenjuanResult.surveyId)" class="btn-copy-inline">复制</button>
      </div>
      <div class="info-row">
        <span class="label">答题链接：</span>
        <span class="value">{{ wenjuanResult.surveyUrl }}</span>
        <button @click="copyToClipboard(wenjuanResult.surveyUrl)" class="btn-copy-inline">复制</button>
      </div>
      <div class="info-tip">
        💡 提示：此链接为公共链接，支持多人多次填写。添加题目并发布后即可使用。
      </div>
      <div class="info-row" v-if="wenjuanResult.manageUrl">
        <span class="label">编辑链接：</span>
        <span class="value">{{ wenjuanResult.manageUrl }}</span>
        <button @click="openEditUrl" class="btn-open">打开编辑器</button>
      </div>
    </div>

    <!-- 问卷标题区 -->
    <div class="content-section">
      <div class="section-header">
        <h3>📝 问卷标题</h3>
        <button @click="copyTitle" class="btn-copy-section" :class="{ copied: copiedSections.title }">
          <span v-if="!copiedSections.title">📋 复制标题</span>
          <span v-else>✓ 已复制</span>
        </button>
      </div>
      <div class="content-box small">
        <textarea 
          v-model="questionnaireTitle" 
          class="editable-content"
          placeholder="问卷标题"
          rows="2"
        ></textarea>
      </div>
    </div>

    <!-- 问卷背景/说明区 -->
    <div class="content-section">
      <div class="section-header">
        <h3>📄 问卷背景与说明</h3>
        <button @click="copyBackground" class="btn-copy-section" :class="{ copied: copiedSections.background }">
          <span v-if="!copiedSections.background">📋 复制背景</span>
          <span v-else>✓ 已复制</span>
        </button>
      </div>
      <div class="content-box medium">
        <textarea 
          v-model="questionnaireBackground" 
          class="editable-content"
          placeholder="问卷背景与说明"
          rows="8"
        ></textarea>
      </div>
    </div>

    <!-- 问卷题目内容区 -->
    <div class="content-section">
      <div class="section-header">
        <h3>📋 问卷题目内容（问卷网标准格式）</h3>
        <button @click="copyQuestions" class="btn-copy-section" :class="{ copied: copiedSections.questions }">
          <span v-if="!copiedSections.questions">📋 复制题目</span>
          <span v-else>✓ 已复制</span>
        </button>
      </div>
      <div class="content-box large">
        <textarea 
          v-model="questionnaireQuestions" 
          class="editable-content"
          placeholder="问卷题目内容"
          rows="20"
        ></textarea>
      </div>
    </div>

    <!-- 快速操作按钮 -->
    <div class="action-buttons">
      <button @click="copyAllContent" class="btn-primary btn-large">
        📋 一键复制全部内容
      </button>
      <button @click="openEditUrl" class="btn-secondary btn-large" v-if="wenjuanResult.manageUrl">
        🌐 打开问卷网编辑器
      </button>
    </div>

    <!-- 使用说明 -->
    <div class="instructions">
      <h3>💡 详细操作步骤</h3>
      <div class="format-tip">
        ✨ <strong>格式优化提示：</strong>导出内容已按照问卷网标准格式生成，可直接复制粘贴！
      </div>
      <ol>
        <li>
          <strong>第一步：复制内容</strong>
          <p>点击上方"一键复制全部内容"按钮，将问卷内容复制到剪贴板</p>
        </li>
        <li>
          <strong>第二步：打开编辑器</strong>
          <p>点击"打开问卷网编辑器"按钮，在新窗口中打开问卷网编辑页面</p>
        </li>
        <li>
          <strong>第三步：添加问卷内容</strong>
          <p>在问卷网编辑器中：</p>
          <ul>
            <li><strong>修改标题和说明：</strong>复制粘贴"问卷标题"和"问卷说明"部分</li>
            <li><strong>逐题添加：</strong>点击"添加题目"按钮，选择题型（单选题/多选题/填空题）</li>
            <li><strong>粘贴题目：</strong>题目格式为"题目内容【题型】"，直接粘贴到题目输入框</li>
            <li><strong>添加选项：</strong>选项已列出（无A、B、C编号），直接复制粘贴每个选项即可</li>
            <li><strong>重复操作：</strong>依次添加所有题目</li>
          </ul>
        </li>
        <li>
          <strong>第四步：预览检查</strong>
          <p>所有题目添加完成后，点击"预览"按钮查看效果，确认内容正确</p>
        </li>
        <li>
          <strong>第五步：保存发布</strong>
          <p>点击"保存"保存问卷，然后点击"发布"按钮，问卷即可发布到公网收集答卷</p>
        </li>
      </ol>
      <div class="time-estimate">
        <span class="icon">⏱️</span>
        <span>预计完成时间：3-5分钟（根据题目数量而定）</span>
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="footer-buttons">
      <button @click="$emit('close')" class="btn-secondary">关闭</button>
      <button @click="openEditUrl" class="btn-primary" v-if="wenjuanResult.manageUrl">
        继续到问卷网编辑器 →
      </button>
    </div>
  </div>
</template>

<script>
import { exportQuestionnaireContent } from '@/api/questionnaire'

export default {
  name: 'WenjuanExport',
  props: {
    questionnaireId: {
      type: Number,
      required: true
    },
    wenjuanResult: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      formattedContent: '',
      questionnaireTitle: '',
      questionnaireBackground: '',
      questionnaireQuestions: '',
      loading: false,
      copiedSections: {
        title: false,
        background: false,
        questions: false
      },
      currentStep: 2
    }
  },
  mounted() {
    this.loadContent()
  },
  methods: {
    async loadContent() {
      this.loading = true
      try {
        const res = await exportQuestionnaireContent(this.questionnaireId)
        this.formattedContent = res.data
        
        // 解析内容，分为标题、背景和题目三部分
        this.parseContent(res.data)
      } catch (error) {
        console.error('加载问卷内容失败:', error)
        this.$message.error('加载问卷内容失败')
      } finally {
        this.loading = false
      }
    },
    parseContent(content) {
      // 按照固定格式解析内容
      const lines = content.split('\n')
      let titleEnd = -1
      let backgroundEnd = -1
      
      // 找到"问卷标题："和"问卷说明："的位置
      for (let i = 0; i < lines.length; i++) {
        if (lines[i].includes('问卷标题：')) {
          titleEnd = i
        } else if (lines[i].includes('问卷说明：') || lines[i].includes('问卷背景：')) {
          backgroundEnd = i
        } else if (lines[i].includes('━━━') || lines[i].includes('===') || lines[i].includes('---')) {
          if (backgroundEnd > 0 && titleEnd > 0) {
            break
          }
        }
      }
      
      // 提取标题
      if (titleEnd >= 0) {
        this.questionnaireTitle = lines[titleEnd].replace('问卷标题：', '').trim()
      }
      
      // 提取背景说明
      if (backgroundEnd >= 0) {
        let bgStart = backgroundEnd
        let bgEnd = backgroundEnd + 1
        for (let i = backgroundEnd + 1; i < lines.length; i++) {
          if (lines[i].includes('━━━') || lines[i].includes('===') || lines[i].includes('【')) {
            bgEnd = i
            break
          }
        }
        this.questionnaireBackground = lines.slice(bgStart, bgEnd)
          .join('\n')
          .replace('问卷说明：', '')
          .replace('问卷背景：', '')
          .trim()
      }
      
      // 提取题目内容 - 从第一个【开始
      let questionsStart = -1
      for (let i = 0; i < lines.length; i++) {
        if (lines[i].includes('【') && (lines[i].includes('题】') || lines[i].includes('题型】'))) {
          questionsStart = i
          break
        }
      }
      
      if (questionsStart >= 0) {
        this.questionnaireQuestions = lines.slice(questionsStart).join('\n').trim()
      }
    },
    async copyTitle() {
      try {
        await navigator.clipboard.writeText(this.questionnaireTitle)
        this.copiedSections.title = true
        this.$message.success('标题已复制到剪贴板！')
        setTimeout(() => {
          this.copiedSections.title = false
        }, 2000)
      } catch (error) {
        this.fallbackCopyText(this.questionnaireTitle, 'title')
      }
    },
    async copyBackground() {
      try {
        await navigator.clipboard.writeText(this.questionnaireBackground)
        this.copiedSections.background = true
        this.$message.success('背景说明已复制到剪贴板！')
        setTimeout(() => {
          this.copiedSections.background = false
        }, 2000)
      } catch (error) {
        this.fallbackCopyText(this.questionnaireBackground, 'background')
      }
    },
    async copyQuestions() {
      try {
        await navigator.clipboard.writeText(this.questionnaireQuestions)
        this.copiedSections.questions = true
        this.$message.success('题目内容已复制到剪贴板！')
        setTimeout(() => {
          this.copiedSections.questions = false
        }, 2000)
      } catch (error) {
        this.fallbackCopyText(this.questionnaireQuestions, 'questions')
      }
    },
    async copyAllContent() {
      try {
        await navigator.clipboard.writeText(this.formattedContent)
        this.copied = true
        this.currentStep = 3
        this.$message.success('问卷内容已复制到剪贴板！')
        
        // 3秒后重置复制状态
        setTimeout(() => {
          this.copied = false
        }, 3000)
      } catch (error) {
        console.error('复制失败:', error)
        // 降级方案：使用传统方法复制
        this.fallbackCopy()
      }
    },
    fallbackCopy() {
      const textarea = document.createElement('textarea')
      textarea.value = this.formattedContent
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.currentStep = 3
        this.$message.success('问卷内容已复制到剪贴板！')
      } catch (error) {
        this.$message.error('复制失败，请手动选择并复制')
      }
      document.body.removeChild(textarea)
    },
    fallbackCopyText(text, section) {
      const textarea = document.createElement('textarea')
      textarea.value = text
      textarea.style.position = 'fixed'
      textarea.style.opacity = '0'
      document.body.appendChild(textarea)
      textarea.select()
      try {
        document.execCommand('copy')
        this.copiedSections[section] = true
        this.$message.success('内容已复制到剪贴板！')
        setTimeout(() => {
          this.copiedSections[section] = false
        }, 2000)
      } catch (error) {
        this.$message.error('复制失败，请手动选择并复制')
      }
      document.body.removeChild(textarea)
    },
    async copyToClipboard(text) {
      try {
        await navigator.clipboard.writeText(text)
        this.$message.success('已复制到剪贴板')
      } catch (error) {
        this.$message.error('复制失败')
      }
    },
    openEditUrl() {
      if (this.wenjuanResult.manageUrl) {
        window.open(this.wenjuanResult.manageUrl, '_blank')
        this.currentStep = 4
        this.$message.info('已在新窗口打开问卷网编辑器，请按照说明添加题目')
      } else {
        this.$message.warning('编辑链接不可用')
      }
    }
  }
}
</script>

<style scoped>
.wenjuan-export-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 40px;
  background: #f5f7fa;
  min-height: 100%;
}

.export-header {
  text-align: center;
  margin-bottom: 30px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.export-header h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  font-size: 16px;
  opacity: 0.95;
}

/* 进度步骤 */
.progress-steps {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.step {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  opacity: 0.4;
}

.step.completed,
.step.active {
  opacity: 1;
}

.step:not(:last-child)::after {
  content: '';
  position: absolute;
  top: 20px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #e0e0e0;
  z-index: 0;
}

.step.completed:not(:last-child)::after {
  background: #67c23a;
}

.step-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #e0e0e0;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  margin-bottom: 8px;
  z-index: 1;
  position: relative;
}

.step.completed .step-icon {
  background: #67c23a;
}

.step.active .step-icon {
  background: #409eff;
}

.step-label {
  font-size: 14px;
  color: #606266;
  text-align: center;
}

/* 信息卡片 */
.info-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.info-card h3 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 18px;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-row:last-child {
  border-bottom: none;
}

.info-tip {
  padding: 12px 15px;
  margin-top: 12px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border-left: 4px solid #1890ff;
  border-radius: 6px;
  color: #0050b3;
  font-size: 14px;
  line-height: 1.6;
}

.info-row .label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
}

.info-row .value {
  flex: 1;
  color: #303133;
  word-break: break-all;
  margin-right: 10px;
}

.btn-copy-inline,
.btn-open {
  padding: 4px 12px;
  font-size: 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-copy-inline {
  background: #e8f4fd;
  color: #409eff;
}

.btn-copy-inline:hover {
  background: #409eff;
  color: white;
}

.btn-open {
  background: #f0f9ff;
  color: #409eff;
}

.btn-open:hover {
  background: #409eff;
  color: white;
}

/* 操作按钮区 */
.action-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
}

.btn-large {
  flex: 1;
  padding: 15px 30px;
  font-size: 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  background: white;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.btn-secondary:hover {
  background: #f5f7fa;
  border-color: #c0c4cc;
}

/* 内容分区展示 */
.content-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.content-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 700;
}

.btn-copy-section {
  padding: 8px 20px;
  font-size: 14px;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.btn-copy-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-copy-section.copied {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);
}

.content-box {
  background: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 20px;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 小号内容框 - 标题 */
.content-box.small {
  height: auto;
  min-height: 60px;
}

.content-box.small .editable-content {
  min-height: 60px;
}

/* 中号内容框 - 背景说明 */
.content-box.medium {
  height: auto;
  min-height: 150px;
}

.content-box.medium .editable-content {
  min-height: 150px;
}

/* 大号内容框 - 题目列表 */
.content-box.large {
  height: auto;
  min-height: 400px;
}

.content-box.large .editable-content {
  min-height: 400px;
}

.content-box::-webkit-scrollbar {
  width: 8px;
}

.content-box::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.content-box::-webkit-scrollbar-thumb {
  background: #667eea;
  border-radius: 4px;
}

.content-box::-webkit-scrollbar-thumb:hover {
  background: #764ba2;
}

.content-box pre {
  margin: 0;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* 可编辑内容区域 */
.editable-content {
  width: 100%;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  resize: vertical;
  background: white;
  transition: all 0.3s;
  box-sizing: border-box;
}

.editable-content:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.editable-content::placeholder {
  color: #c0c4cc;
}

/* 使用说明 */
.instructions {
  background: #fff7e6;
  border: 2px solid #ffd666;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
}

.instructions h3 {
  margin: 0 0 15px 0;
  color: #d48806;
  font-size: 18px;
}

.format-tip {
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  border-left: 4px solid #1890ff;
  padding: 12px 15px;
  margin-bottom: 15px;
  border-radius: 6px;
  color: #0050b3;
  font-size: 14px;
  line-height: 1.6;
}

.instructions ol {
  margin: 0;
  padding-left: 20px;
}

.instructions li {
  margin-bottom: 15px;
  color: #595959;
}

.instructions strong {
  color: #303133;
  font-size: 15px;
}

.instructions p {
  margin: 5px 0;
  font-size: 14px;
}

.instructions ul {
  margin: 8px 0;
  padding-left: 20px;
}

.instructions ul li {
  margin-bottom: 5px;
  font-size: 14px;
}

.time-estimate {
  margin-top: 15px;
  padding: 10px;
  background: white;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #d48806;
  font-weight: 600;
}

.time-estimate .icon {
  font-size: 20px;
}

/* 底部按钮 */
.footer-buttons {
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.footer-buttons button {
  padding: 12px 30px;
  font-size: 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.3s;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .progress-steps {
    flex-direction: column;
    gap: 10px;
  }

  .step:not(:last-child)::after {
    display: none;
  }

  .action-buttons {
    flex-direction: column;
  }

  .footer-buttons {
    flex-direction: column;
  }
}
</style>

