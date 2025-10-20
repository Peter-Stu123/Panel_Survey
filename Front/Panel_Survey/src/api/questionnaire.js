import request from '../utils/request'

export function generateQuestionnaire(projectId) {
  return request({
    url: `/api/questionnaire/generate/${projectId}`,
    method: 'post'
  })
}

export function reviewByAI(questionnaireId) {
  return request({
    url: `/api/questionnaire/review/${questionnaireId}`,
    method: 'post'
  })
}

export function polishReportBackground(projectId) {
  return request({
    url: `/api/questionnaire/polish-background/${projectId}`,
    method: 'post'
  })
}

export function getQuestionnaire(id) {
  return request({
    url: `/api/questionnaire/${id}`,
    method: 'get'
  })
}

export function getQuestionnaireByProject(projectId) {
  return request({
    url: `/api/questionnaire/project/${projectId}`,
    method: 'get'
  })
}

export function updateQuestion(questionId, questionText) {
  return request({
    url: `/api/questionnaire/question/${questionId}`,
    method: 'put',
    data: { questionText }
  })
}

export function publishQuestionnaire(questionnaireId, surveyUrl) {
  return request({
    url: `/api/questionnaire/publish/${questionnaireId}`,
    method: 'post',
    params: { surveyUrl }
  })
}

/**
 * 发布问卷到问卷网
 * 自动创建问卷网问卷并获取公网访问链接
 * @param {number} questionnaireId 问卷ID
 * @returns {Promise} 返回问卷网创建结果（包含公网链接）
 */
export function publishToWenjuan(questionnaireId) {
  return request({
    url: `/api/questionnaire/publish-to-wenjuan/${questionnaireId}`,
    method: 'post'
  })
}

// 公开访问问卷（不需要认证）
export function getPublicQuestionnaire(id) {
  return request({
    url: `/api/survey/public/${id}`,
    method: 'get'
  })
}

/**
 * 从问卷网同步答卷数据
 * @param {number} questionnaireId 问卷ID
 * @returns {Promise} 返回问卷网答卷数量
 */
export function syncWenjuanData(questionnaireId) {
  return request({
    url: `/api/questionnaire/sync-wenjuan/${questionnaireId}`,
    method: 'post'
  })
}

/**
 * 导出问卷内容为格式化文本
 * @param {number} questionnaireId 问卷ID
 * @returns {Promise} 返回格式化的问卷内容
 */
export function exportQuestionnaireContent(questionnaireId) {
  return request({
    url: `/api/questionnaire/export-content/${questionnaireId}`,
    method: 'get'
  })
}

