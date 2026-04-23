import request from '@/utils/request'

// ============ 接口管理 ============

/**
 * 创建接口
 * @param {Object} data - 接口配置数据
 */
export function insertInterface(data) {
  return request({
    url: '/interface_http/insert',
    method: 'post',
    data
  })
}

/**
 * 更新接口
 * @param {Object} data - 接口配置数据
 */
export function updateInterface(data) {
  return request({
    url: '/interface_http/update',
    method: 'post',
    data
  })
}

/**
 * 查询接口详情
 * @param {Object} params - { id }
 */
export function selectById(params) {
  return request({
    url: '/interface_http/selectById',
    method: 'post',
    data: params
  })
}

/**
 * 按空间查询接口列表（分页）
 * @param {Object} data - { criteria: { spaceId }, page, itemsPerPage }
 */
export function selectBySpaceId(data) {
  return request({
    url: '/interface_http/selectBySpaceId',
    method: 'post',
    data
  })
}

/**
 * 查询全部接口（分页）
 * @param {Object} data - { page, itemsPerPage }
 */
export function selectAll(data) {
  return request({
    url: '/interface_http/selectAll',
    method: 'post',
    data
  })
}

/**
 * 删除接口
 * @param {Object} data - { id }
 */
export function deleteInterface(data) {
  return request({
    url: '/interface_http/delete',
    method: 'post',
    data
  })
}

/**
 * 检测接口URL冲突（用于异步校验）
 * @param {Object} data - { interfaceUrl, requestMethod, id }
 */
export function conflictDetection(data) {
  return request({
    url: '/interface_http/conflictDetection',
    method: 'post',
    data
  })
}

/**
 * 查询接口调用记录
 * @param {Object} data - { interfaceId, page, itemsPerPage }
 */
export function getCallLogs(data) {
  return request({
    url: '/interface_http/call_logs/selectByInterfaceId',
    method: 'post',
    data
  })
}

// ============ 空间管理 ============

/**
 * 新增空间
 */
export function addSpace(data) {
  return request({
    url: '/space/insert',
    method: 'post',
    data
  })
}

/**
 * 更新空间
 */
export function updateSpace(data) {
  return request({
    url: '/space/update',
    method: 'post',
    data
  })
}

/**
 * 删除空间
 */
export function deleteSpace(data) {
  return request({
    url: '/space/delete',
    method: 'post',
    data
  })
}

/**
 * 获取空间树
 */
export function getSpaceTree() {
  return request({
    url: '/space/tree',
    method: 'post',
    data: {}
  })
}

// ============ 系统信息 ============

/**
 * 获取 Core 服务 URL 前缀
 */
export function getCoreHostInfo() {
  return request({
    url: '/host_info/core',
    method: 'post',
    data: {}
  })
}

// ============ API Tester（直调axios，不走拦截器）============

/**
 * API Tester 代理请求
 * 直接发送HTTP请求，不经过 anymock web api
 * @param {Object} config - { url, method, headers, body }
 */
export function apiProxySend(config) {
  const axios = window.__axios || import('axios')
  return axios.default({
    url: config.url,
    method: config.method || 'GET',
    headers: config.headers || {},
    data: config.body || undefined,
    timeout: 30000
  })
}
