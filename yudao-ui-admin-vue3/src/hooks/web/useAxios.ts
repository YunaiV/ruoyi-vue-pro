import { service } from '@/config/axios'

import { config } from '@/config/axios/config'

const { default_headers } = config

const request = (option: AxiosConfig) => {
  const { url, method, params, data, headersType, responseType } = option
  return service({
    url: url,
    method,
    params,
    data,
    responseType: responseType,
    headers: {
      'Content-Type': headersType || default_headers
    }
  })
}

async function getFn<T = any>(option: AxiosConfig): Promise<T> {
  const res = await request({ method: 'GET', ...option })
  return res.data
}

async function postFn<T = any>(option: AxiosConfig): Promise<T> {
  const res = await request({ method: 'POST', ...option })
  return res.data
}

async function deleteFn<T = any>(option: AxiosConfig): Promise<T> {
  const res = await request({ method: 'DELETE', ...option })
  return res.data
}

async function putFn<T = any>(option: AxiosConfig): Promise<T> {
  const res = await request({ method: 'PUT', ...option })
  return res.data
}
async function downloadFn<T = any>(option: AxiosConfig): Promise<T> {
  const res = await request({ method: 'GET', responseType: 'blob', ...option })
  return res as unknown as Promise<T>
}

async function uploadFn<T = any>(option: AxiosConfig): Promise<T> {
  option.headersType = 'multipart/form-data'
  const res = await request({ method: 'PUT', ...option })
  return res as unknown as Promise<T>
}

export const useAxios = () => {
  return {
    get: getFn,
    post: postFn,
    delete: deleteFn,
    put: putFn,
    download: downloadFn,
    upload: uploadFn
  }
}
