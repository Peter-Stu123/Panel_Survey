import request from '../utils/request'

export function createProjectStep1(data) {
  return request({
    url: '/api/project/step1',
    method: 'post',
    data
  })
}

export function updateProjectStep2(data) {
  return request({
    url: '/api/project/step2',
    method: 'put',
    data
  })
}

export function getProject(id) {
  return request({
    url: `/api/project/${id}`,
    method: 'get'
  })
}

export function getUserProjects() {
  return request({
    url: '/api/project/list',
    method: 'get'
  })
}

export function deleteProject(id) {
  return request({
    url: `/api/project/${id}`,
    method: 'delete'
  })
}

// ==================== Redis草稿相关API ====================

/**
 * 保存Step1草稿到Redis
 */
export function saveStep1Draft(data) {
  return request({
    url: '/api/project/step1/draft',
    method: 'post',
    data
  })
}

/**
 * 获取Step1草稿
 */
export function getStep1Draft() {
  return request({
    url: '/api/project/step1/draft',
    method: 'get'
  })
}

/**
 * 保存Step2草稿到Redis
 */
export function saveStep2Draft(data) {
  return request({
    url: '/api/project/step2/draft',
    method: 'post',
    data
  })
}

/**
 * 获取Step2草稿
 */
export function getStep2Draft() {
  return request({
    url: '/api/project/step2/draft',
    method: 'get'
  })
}

/**
 * 保存通用草稿
 */
export function saveDraft(stepType, draftData) {
  return request({
    url: '/api/draft/save',
    method: 'post',
    data: {
      stepType,
      draftData
    }
  })
}

/**
 * 获取通用草稿
 */
export function getDraft(stepType) {
  return request({
    url: `/api/draft/get/${stepType}`,
    method: 'get'
  })
}

/**
 * 删除草稿
 */
export function deleteDraft(stepType) {
  return request({
    url: `/api/draft/delete/${stepType}`,
    method: 'delete'
  })
}

/**
 * 清除所有草稿
 */
export function clearAllDrafts() {
  return request({
    url: '/api/draft/clear',
    method: 'delete'
  })
}

