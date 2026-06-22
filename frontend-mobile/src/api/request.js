import axios from 'axios'
import { showToast } from 'vant'

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
    showToast(body.message || '请求失败')
    return Promise.reject(new Error(body.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        localStorage.removeItem('student_token')
        window.location.hash = '#/login'
        showToast('登录已过期，请重新登录')
      } else {
        showToast(data?.message || `请求错误 ${status}`)
      }
    } else {
      showToast('网络错误，请检查服务是否启动')
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
