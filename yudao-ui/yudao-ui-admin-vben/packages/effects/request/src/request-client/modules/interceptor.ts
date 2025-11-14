import type { AxiosInstance, AxiosResponse } from 'axios';

import type {
  RequestInterceptorConfig,
  ResponseInterceptorConfig,
} from '../types';

const defaultRequestInterceptorConfig: RequestInterceptorConfig = {
  fulfilled: (response) => response,
  rejected: (error) => Promise.reject(error),
};

const defaultResponseInterceptorConfig: ResponseInterceptorConfig = {
  fulfilled: (response: AxiosResponse) => response,
  rejected: (error) => Promise.reject(error),
};

class InterceptorManager {
  private axiosInstance: AxiosInstance;

  constructor(instance: AxiosInstance) {
    this.axiosInstance = instance;
  }

  addRequestInterceptor({
    fulfilled,
    rejected,
  }: RequestInterceptorConfig = defaultRequestInterceptorConfig) {
    this.axiosInstance.interceptors.request.use(fulfilled, rejected);
  }

  addResponseInterceptor<T = any>({
    fulfilled,
    rejected,
  }: ResponseInterceptorConfig<T> = defaultResponseInterceptorConfig) {
    this.axiosInstance.interceptors.response.use(fulfilled, rejected);
  }
}

export { InterceptorManager };
