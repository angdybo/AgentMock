import axios from 'axios'

const request = axios.create({
  baseURL: '/anymockweb_api/v2',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端返回 resultCode: '000000' 表示成功
    if (res.resultCode && res.resultCode !== '000000') {
      return Promise.reject(new Error(res.resultMsg || '请求失败'))
    }
    if (res.code !== 0 && res.code !== '0' && res.resultCode === undefined) {
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    return Promise.reject(error)
  }
)

export default request
