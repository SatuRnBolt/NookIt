import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '',
  timeout: 15000,
})

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('student_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body.code === 0) {
      return body.data
    }
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('student_token')
        window.location.hash = '#/login'
        ElMessage.error('登录已过期，请重新登录')
      } else {
        ElMessage.error(data?.message || `请求错误 ${status}`)
      }
    } else {
      ElMessage.error('网络错误，请检查后端服务是否启动')
    }
    return Promise.reject(error)
  }
)

export function get(url, params) {
  return http.get(url, { params })
}

export function post(url, data) {
  return http.post(url, data)
}

export function put(url, data) {
  return http.put(url, data)
}

export function patch(url, data) {
  return http.patch(url, data)
}

export function del(url) {
  return http.delete(url)
}

export default http
