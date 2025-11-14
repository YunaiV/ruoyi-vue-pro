/**
 * 该文件可自行根据业务逻辑进行调整
 */
import type { RequestClientOptions } from '@vben/request';

import { isTenantEnable, useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import {
  authenticateResponseInterceptor,
  defaultResponseInterceptor,
  errorMessageResponseInterceptor,
  RequestClient,
} from '@vben/request';
import { useAccessStore } from '@vben/stores';
import { createApiEncrypt } from '@vben/utils';

import { message } from 'ant-design-vue';

import { useAuthStore } from '#/store';

import { refreshTokenApi } from './core';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);
const tenantEnable = isTenantEnable();
const apiEncrypt = createApiEncrypt(import.meta.env);

function createRequestClient(baseURL: string, options?: RequestClientOptions) {
  const client = new RequestClient({
    ...options,
    baseURL,
  });

  /**
   * 重新认证逻辑
   */
  async function doReAuthenticate() {
    console.warn('Access token or refresh token is invalid or expired. ');
    const accessStore = useAccessStore();
    const authStore = useAuthStore();
    accessStore.setAccessToken(null);
    if (
      preferences.app.loginExpiredMode === 'modal' &&
      accessStore.isAccessChecked
    ) {
      accessStore.setLoginExpired(true);
    } else {
      await authStore.logout();
    }
  }

  /**
   * 刷新token逻辑
   */
  async function doRefreshToken() {
    const accessStore = useAccessStore();
    const refreshToken = accessStore.refreshToken as string;
    if (!refreshToken) {
      throw new Error('Refresh token is null!');
    }
    const resp = await refreshTokenApi(refreshToken);
    const newToken = resp?.data?.data?.accessToken;
    // add by 芋艿：这里一定要抛出 resp.data，从而触发 authenticateResponseInterceptor 中，刷新令牌失败！！！
    if (!newToken) {
      throw resp.data;
    }
    accessStore.setAccessToken(newToken);
    return newToken;
  }

  function formatToken(token: null | string) {
    return token ? `Bearer ${token}` : null;
  }

  // 请求头处理
  client.addRequestInterceptor({
    fulfilled: async (config) => {
      const accessStore = useAccessStore();

      config.headers.Authorization = formatToken(accessStore.accessToken);
      config.headers['Accept-Language'] = preferences.app.locale;
      // 添加租户编号
      config.headers['tenant-id'] = tenantEnable
        ? accessStore.tenantId
        : undefined;
      // 只有登录时，才设置 visit-tenant-id 访问租户
      config.headers['visit-tenant-id'] = tenantEnable
        ? accessStore.visitTenantId
        : undefined;

      // 是否 API 加密
      if ((config.headers || {}).isEncrypt) {
        try {
          // 加密请求数据
          if (config.data) {
            config.data = apiEncrypt.encryptRequest(config.data);
            // 设置加密标识头
            config.headers[apiEncrypt.getEncryptHeader()] = 'true';
          }
        } catch (error) {
          console.error('请求数据加密失败:', error);
          throw error;
        }
      }
      return config;
    },
  });

  // API 解密响应拦截器
  client.addResponseInterceptor({
    fulfilled: (response) => {
      // 检查是否需要解密响应数据
      const encryptHeader = apiEncrypt.getEncryptHeader();
      const isEncryptResponse =
        response.headers[encryptHeader] === 'true' ||
        response.headers[encryptHeader.toLowerCase()] === 'true';
      if (isEncryptResponse && typeof response.data === 'string') {
        try {
          // 解密响应数据
          response.data = apiEncrypt.decryptResponse(response.data);
        } catch (error) {
          console.error('响应数据解密失败:', error);
          throw new Error(`响应数据解密失败: ${(error as Error).message}`);
        }
      }
      return response;
    },
  });

  // 处理返回的响应数据格式
  client.addResponseInterceptor(
    defaultResponseInterceptor({
      codeField: 'code',
      dataField: 'data',
      successCode: 0,
    }),
  );

  // token过期的处理
  client.addResponseInterceptor(
    authenticateResponseInterceptor({
      client,
      doReAuthenticate,
      doRefreshToken,
      enableRefreshToken: preferences.app.enableRefreshToken,
      formatToken,
    }),
  );

  // 通用的错误处理,如果没有进入上面的错误处理逻辑，就会进入这里
  client.addResponseInterceptor(
    errorMessageResponseInterceptor((msg: string, error) => {
      // 这里可以根据业务进行定制,你可以拿到 error 内的信息进行定制化处理，根据不同的 code 做不同的提示，而不是直接使用 message.error 提示 msg
      // 当前mock接口返回的错误字段是 error 或者 message
      const responseData = error?.response?.data ?? {};
      const errorMessage =
        responseData?.error ?? responseData?.message ?? responseData.msg ?? '';
      // add by 芋艿：特殊：避免 401 “账号未登录”，重复提示。因为，此时会跳转到登录界面，只需提示一次！！！
      if (error?.data?.code === 401) {
        return;
      }
      // 如果没有错误信息，则会根据状态码进行提示
      message.error(errorMessage || msg);
    }),
  );

  return client;
}

export const requestClient = createRequestClient(apiURL, {
  responseReturn: 'data',
});

export const baseRequestClient = new RequestClient({ baseURL: apiURL });
baseRequestClient.addRequestInterceptor({
  fulfilled: (config) => {
    const accessStore = useAccessStore();
    // 添加租户编号
    config.headers['tenant-id'] = tenantEnable
      ? accessStore.tenantId
      : undefined;
    // 只有登录时，才设置 visit-tenant-id 访问租户
    config.headers['visit-tenant-id'] = tenantEnable
      ? accessStore.visitTenantId
      : undefined;
    return config;
  },
});
