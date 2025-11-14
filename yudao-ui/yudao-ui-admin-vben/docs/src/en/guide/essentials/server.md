# Server Interaction and Data Mocking

::: tip Note

This document explains how to use Mock data and interact with the server in a development environment, involving technologies such as:

- [Nitro](https://nitro.unjs.io/) A lightweight backend server that can be deployed anywhere, used as a Mock server in the project.
- [axios](https://axios-http.com/docs/intro) Used to send HTTP requests to interact with the server.

:::

## Interaction in Development Environment

If the frontend application and the backend API server are not running on the same host, you need to proxy the API requests to the API server in the development environment. If they are on the same host, you can directly request the specific API endpoint.

### Local Development CORS Configuration

::: tip Hint

The CORS configuration for local development has already been set up. If you have other requirements, you can add or adjust the configuration as needed.

:::

#### Configuring Local Development API Endpoint

Configure the API endpoint in the `.env.development` file at the project root directory, here it is set to `/api`:

```bash
VITE_GLOB_API_URL=/api
```

#### Configuring Development Server Proxy

In the development environment, if you need to handle CORS, configure the API endpoint in the `vite.config.mts` file under the corresponding application directory:

```ts{8-16}
// apps/web-antd/vite.config.mts
import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    vite: {
      server: {
        proxy: {// [!code focus:11]
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // mock proxy
            target: 'http://localhost:5320/api',
            ws: true,
          },
        },
      },
    },
  };
});
```

#### API Requests

Based on the above configuration, we can use `/api` as the prefix for API requests in our frontend project, for example:

```ts
import axios from 'axios';

axios.get('/api/user').then((res) => {
  console.log(res);
});
```

At this point, the request will be proxied to `http://localhost:5320/api/user`.

::: warning Note

From the browser's console Network tab, the request appears as `http://localhost:5555/api/user`. This is because the proxy configuration does not change the local request's URL.

:::

### Configuration Without CORS

If there is no CORS issue, you can directly ignore the [Configure Development Server Proxy](./server.md#configure-development-server-proxy) settings and set the API endpoint directly in `VITE_GLOB_API_URL`.

Configure the API endpoint in the `.env.development` file at the project root directory:

```bash
VITE_GLOB_API_URL=https://mock-napi.vben.pro/api
```

## Production Environment Interaction

### API Endpoint Configuration

Configure the API endpoint in the `.env.production` file at the project root directory:

```bash
VITE_GLOB_API_URL=https://mock-napi.vben.pro/api
```

::: tip How to Dynamically Modify API Endpoint in Production

Variables starting with `VITE_GLOB_*` in the `.env` file are injected into the `_app.config.js` file during packaging. After packaging, you can modify the corresponding API addresses in `dist/_app.config.js` and refresh the page to apply the changes. This eliminates the need to package multiple times for different environments, allowing a single package to be deployed across multiple API environments.

:::

### Cross-Origin Resource Sharing (CORS) Handling

In the production environment, if CORS issues arise, you can use `nginx` to proxy the API address or enable `cors` on the backend to handle it (refer to the mock service for examples).

## API Request Configuration

The project comes with a default basic request configuration based on `axios`, provided by the `@vben/request` package. The project does not overly complicate things but simply wraps some common configurations. If there are other requirements, you can add or adjust the configurations as needed. Depending on the app, different component libraries and `store` might be used, so under the `src/api/request.ts` folder in the application directory, there are corresponding request configuration files. For example, in the `web-antd` project, there's a `src/api/request.ts` file where you can configure according to your needs.

### Request Examples

#### GET Request

```ts
import { requestClient } from '#/api/request';

export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/user/info');
}
```

#### POST/PUT Request

```ts
import { requestClient } from '#/api/request';

export async function saveUserApi(user: UserInfo) {
  return requestClient.post<UserInfo>('/user', user);
}

export async function saveUserApi(user: UserInfo) {
  return requestClient.put<UserInfo>('/user', user);
}

export async function saveUserApi(user: UserInfo) {
  const url = user.id ? `/user/${user.id}` : '/user/';
  return requestClient.request<UserInfo>(url, {
    data: user,
    // OR PUT
    method: user.id ? 'PUT' : 'POST',
  });
}
```

#### DELETE Request

```ts
import { requestClient } from '#/api/request';

export async function deleteUserApi(userId: number) {
  return requestClient.delete<boolean>(`/user/${userId}`);
}
```

### Request Configuration

The `src/api/request.ts` within the application can be configured according to the needs of your application:

```ts
/**
 * This file can be adjusted according to business logic
 */
import type { HttpResponse } from '@vben/request';

import { useAppConfig } from '@vben/hooks';
import { preferences } from '@vben/preferences';
import {
  authenticateResponseInterceptor,
  errorMessageResponseInterceptor,
  RequestClient,
} from '@vben/request';
import { useAccessStore } from '@vben/stores';

import { message } from 'ant-design-vue';

import { useAuthStore } from '#/store';

import { refreshTokenApi } from './core';

const { apiURL } = useAppConfig(import.meta.env, import.meta.env.PROD);

function createRequestClient(baseURL: string) {
  const client = new RequestClient({
    baseURL,
  });

  /**
   * Re-authentication Logic
   */
  async function doReAuthenticate() {
    console.warn('Access token or refresh token is invalid or expired. ');
    const accessStore = useAccessStore();
    const authStore = useAuthStore();
    accessStore.setAccessToken(null);
    if (preferences.app.loginExpiredMode === 'modal') {
      accessStore.setLoginExpired(true);
    } else {
      await authStore.logout();
    }
  }

  /**
   * Refresh token Logic
   */
  async function doRefreshToken() {
    const accessStore = useAccessStore();
    const resp = await refreshTokenApi();
    const newToken = resp.data;
    accessStore.setAccessToken(newToken);
    return newToken;
  }

  function formatToken(token: null | string) {
    return token ? `Bearer ${token}` : null;
  }

  // Request Header Processing
  client.addRequestInterceptor({
    fulfilled: async (config) => {
      const accessStore = useAccessStore();

      config.headers.Authorization = formatToken(accessStore.accessToken);
      config.headers['Accept-Language'] = preferences.app.locale;
      return config;
    },
  });

  // Deal Response Data
  client.addResponseInterceptor<HttpResponse>({
    fulfilled: (response) => {
      const { data: responseData, status } = response;

      const { code, data } = responseData;

      if (status >= 200 && status < 400 && code === 0) {
        return data;
      }
      throw Object.assign({}, response, { response });
    },
  });

  // Handling Token Expiration
  client.addResponseInterceptor(
    authenticateResponseInterceptor({
      client,
      doReAuthenticate,
      doRefreshToken,
      enableRefreshToken: preferences.app.enableRefreshToken,
      formatToken,
    }),
  );

  // Generic error handling; if none of the above error handling logic is triggered, it will fall back to this.
  client.addResponseInterceptor(
    errorMessageResponseInterceptor((msg: string, error) => {
      // 这里可以根据业务进行定制,你可以拿到 error 内的信息进行定制化处理，根据不同的 code 做不同的提示，而不是直接使用 message.error 提示 msg
      // 当前mock接口返回的错误字段是 error 或者 message
      const responseData = error?.response?.data ?? {};
      const errorMessage = responseData?.error ?? responseData?.message ?? '';
      // 如果没有错误信息，则会根据状态码进行提示
      message.error(errorMessage || msg);
    }),
  );

  return client;
}

export const requestClient = createRequestClient(apiURL);

export const baseRequestClient = new RequestClient({ baseURL: apiURL });
```

### Multiple API Endpoints

To handle multiple API endpoints, simply create multiple `requestClient` instances, as follows:

```ts
const { apiURL, otherApiURL } = useAppConfig(
  import.meta.env,
  import.meta.env.PROD,
);

export const requestClient = createRequestClient(apiURL);

export const otherRequestClient = createRequestClient(otherApiURL);
```

## Refresh Token

The project provides a default logic for refreshing tokens. To enable it, follow the configuration below:

- Ensure the refresh token feature is enabled

Adjust the `preferences.ts` in the corresponding application directory to ensure `enableRefreshToken='true'`.

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    enableRefreshToken: true,
  },
});
```

Configure the `doRefreshToken` method in `src/api/request.ts` as follows:

```ts
// Adjust this to your token format
function formatToken(token: null | string) {
  return token ? `Bearer ${token}` : null;
}

/**
 * Refresh token logic
 */
async function doRefreshToken() {
  const accessStore = useAccessStore();
  // Adjust this to your refresh token API
  const resp = await refreshTokenApi();
  const newToken = resp.data;
  accessStore.setAccessToken(newToken);
  return newToken;
}
```

## Data Mocking

::: tip Production Environment Mock

The new version no longer supports mock in the production environment. Please use real interfaces.

:::

Mock data is an indispensable part of frontend development, serving as a key link in separating frontend and backend development. By agreeing on interfaces with the server side in advance and simulating request data and even logic, frontend development can proceed independently, without being blocked by the backend development process.

The project uses [Nitro](https://nitro.unjs.io/) for local mock data processing. The principle is to start an additional backend service locally, which is a real backend service that can handle requests and return data.

### Using Nitro

The mock service code is located in the `apps/backend-mock` directory. It does not need to be started manually and is already integrated into the project. You only need to run `pnpm dev` in the project root directory. After running successfully, the console will print `http://localhost:5320/api`, and you can access this address to view the mock service.

[Nitro](https://nitro.unjs.io/) syntax is simple, and you can configure and develop according to your needs. For specific configurations, you can refer to the [Nitro documentation](https://nitro.unjs.io/).

## Disabling Mock Service

Since mock is essentially a real backend service, if you do not need the mock service, you can configure `VITE_NITRO_MOCK=false` in the `.env.development` file in the project root directory to disable the mock service.

```bash
# .env.development
VITE_NITRO_MOCK=false
```
