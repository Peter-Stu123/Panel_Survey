import request from '../utils/request'

export function generateReport(questionnaireId) {
  return request({
    url: `/api/report/generate/${questionnaireId}`,
    method: 'post'
  })
}

export function getReport(id) {
  return request({
    url: `/api/report/${id}`,
    method: 'get'
  })
}

export function getReportByQuestionnaire(questionnaireId) {
  return request({
    url: `/api/report/questionnaire/${questionnaireId}`,
    method: 'get'
  })
}

export function exportToPdf(reportId) {
  return request({
    url: `/api/report/export/pdf/${reportId}`,
    method: 'get'
  })
}

export function exportToExcel(reportId) {
  return request({
    url: `/api/report/export/excel/${reportId}`,
    method: 'get'
  })
}

export function getWenjuanReportUrl(questionnaireId) {
  return request({
    url: `/api/report/wenjuan-console-url/${questionnaireId}`,
    method: 'get'
  })
}

