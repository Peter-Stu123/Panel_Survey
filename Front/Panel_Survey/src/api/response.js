import request from '../utils/request'

export function submitResponse(data) {
  return request({
    url: '/api/survey/public/submit',
    method: 'post',
    data
  })
}

export function getResponseCount(questionnaireId) {
  return request({
    url: `/api/survey/response/count/${questionnaireId}`,
    method: 'get'
  })
}

export function getCompletedCount(questionnaireId) {
  return request({
    url: `/api/survey/response/completed/${questionnaireId}`,
    method: 'get'
  })
}

