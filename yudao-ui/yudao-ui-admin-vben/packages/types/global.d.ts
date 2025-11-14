import type { RouteMeta as IRouteMeta } from '@vben-core/typings';

import 'vue-router';

declare module 'vue-router' {
  // eslint-disable-next-line @typescript-eslint/no-empty-object-type
  interface RouteMeta extends IRouteMeta {}
}

export interface VbenAdminProAppConfigRaw {
  VITE_GLOB_API_URL: string;
  VITE_GLOB_AUTH_DINGDING_CLIENT_ID: string;
  VITE_GLOB_AUTH_DINGDING_CORP_ID: string;
  // API 加解密相关配置
  VITE_APP_API_ENCRYPT_ENABLE: string;
  VITE_APP_API_ENCRYPT_HEADER: string;
  VITE_APP_API_ENCRYPT_ALGORITHM: string;
  VITE_APP_API_ENCRYPT_REQUEST_KEY: string;
  VITE_APP_API_ENCRYPT_RESPONSE_KEY: string;
}

interface AuthConfig {
  dingding?: {
    clientId: string;
    corpId: string;
  };
}

export interface ApplicationConfig {
  apiURL: string;
  auth: AuthConfig;
}

declare global {
  interface Window {
    _VBEN_ADMIN_PRO_APP_CONF_: VbenAdminProAppConfigRaw;
  }
}
