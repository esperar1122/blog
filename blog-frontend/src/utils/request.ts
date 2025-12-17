import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const userStore = useUserStore();

    // 添加token到请求头
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`;
    }

    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;

    // 如果响应包含success字段且为false，则视为业务错误
    if (res.success === false) {
      ElMessage.error(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }

    // 返回响应数据
    return res.data || res;
  },
  async (error) => {
    console.error('Response error:', error);

    const userStore = useUserStore();

    if (error.response) {
      const { status, data } = error.response;

      switch (status) {
        case 401:
          // 未授权，清除token并跳转到登录页
          ElMessage.error('登录已过期，请重新登录');
          await userStore.doLogout();
          window.location.href = '/login';
          break;

        case 403:
          ElMessage.error('没有权限访问该资源');
          break;

        case 404:
          ElMessage.error('请求的资源不存在');
          break;

        case 500:
          ElMessage.error('服务器内部错误');
          break;

        default:
          ElMessage.error(data?.message || `请求失败 (${status})`);
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      ElMessage.error('网络错误，请检查网络连接');
    } else {
      // 请求配置出错
      ElMessage.error('请求配置错误');
    }

    return Promise.reject(error);
  }
);

/**
 * 封装的请求方法
 * @param config 请求配置
 * @returns Promise
 */
export function request<T = any>(config: AxiosRequestConfig): Promise<T> {
  return service.request(config);
}

/**
 * GET请求
 * @param url 请求地址
 * @param params 请求参数
 * @param config 额外配置
 * @returns Promise
 */
export function get<T = any>(
  url: string,
  params?: any,
  config?: AxiosRequestConfig
): Promise<T> {
  return request<T>({
    method: 'GET',
    url,
    params,
    ...config,
  });
}

/**
 * POST请求
 * @param url 请求地址
 * @param data 请求数据
 * @param config 额外配置
 * @returns Promise
 */
export function post<T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<T> {
  return request<T>({
    method: 'POST',
    url,
    data,
    ...config,
  });
}

/**
 * PUT请求
 * @param url 请求地址
 * @param data 请求数据
 * @param config 额外配置
 * @returns Promise
 */
export function put<T = any>(
  url: string,
  data?: any,
  config?: AxiosRequestConfig
): Promise<T> {
  return request<T>({
    method: 'PUT',
    url,
    data,
    ...config,
  });
}

/**
 * DELETE请求
 * @param url 请求地址
 * @param params 请求参数
 * @param config 额外配置
 * @returns Promise
 */
export function del<T = any>(
  url: string,
  params?: any,
  config?: AxiosRequestConfig
): Promise<T> {
  return request<T>({
    method: 'DELETE',
    url,
    params,
    ...config,
  });
}

// 导出axios实例
export default service;