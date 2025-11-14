# 服务端交互与数据Mock

::: tip 说明

本文档介绍如何在开发环境下使用 Mock 数据和与服务端进行交互，涉及到的技术有：

- [Nitro](https://nitro.unjs.io/) 轻量级后端服务器，可部署在任何地方，项目用作于 Mock 服务器。
- [axios](https://axios-http.com/docs/intro) 用于发送 HTTP 请求与服务端进行交互。

:::

## 开发环境交互

如果前端应用和后端接口服务器没有运行在同一个主机上，你需要在开发环境下将接口请求代理到接口服务器。如果是同一个主机，可以直接请求具体的接口地址。

### 本地开发跨域配置

::: tip 提示

本地开发跨域配置项目已经配置好了，如有其他需求，可以自行增加或者调整配置。

:::

#### 配置本地开发接口地址

在项目根目录下的 `.env.development` 文件中配置接口地址，这里配置为 `/api`：

```bash
VITE_GLOB_API_URL=/api
```

#### 配置开发服务器代理

开发环境时候，如果需要处理跨域，接口地址在对应的应用目录下的 `vite.config.mts` 文件中配置：

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
            // mock代理目标地址
            target: 'http://localhost:5320/api',
            ws: true,
          },
        },
      },
    },
  };
});
```

#### 接口请求

根据上面的配置，我们可以在前端项目中使用 `/api` 作为接口请求的前缀，例如：

```ts
import axios from 'axios';

axios.get('/api/user').then((res) => {
  console.log(res);
});
```

此时，请求会被代理到 `http://localhost:5320/api/user`。

::: warning 注意

从浏览器控制台的 Network 看，请求是 `http://localhost:5555/api/user`, 这是因为 proxy 配置不会改变本地请求的 url。

:::

### 没有跨域时的配置

如果没有跨域问题，可以直接忽略 [配置开发服务器代理](./server.md#配置开发服务器代理) 配置，直接将接口地址设置在 `VITE_GLOB_API_URL`

在项目根目录下的 `.env.development` 文件中配置接口地址：

```bash
VITE_GLOB_API_URL=https://mock-napi.vben.pro/api
```

## 生产环境交互

### 接口地址配置

在项目根目录下的 `.env.production` 文件中配置接口地址：

```bash
VITE_GLOB_API_URL=https://mock-napi.vben.pro/api
```

::: tip 打包如何动态修改接口地址

`.env` 文件内的 `VITE_GLOB_*` 开头的变量会在打包的时候注入 `_app.config.js` 文件内。在 `dist/_app.config.js` 修改相应的接口地址后刷新页面即可，不需要在根据不同环境打包多次，一次打包可以用于多个不同接口环境的部署。

:::

### 跨域处理

生产环境如果出现跨域问题，可以使用 `nginx` 代理接口地址 或者后台开启 `cors` 进行处理即可（可参考mock服务）。

## 接口请求配置

项目中默认自带了基于 `axios` 封装的基础的请求配置，核心由 `@vben/request` 包提供。项目没有过多的封装，只是简单的封装了一些常用的配置，如有其他需求，可以自行增加或者调整配置。针对不同的app，可能是用到了不同的组件库以及`store`,所以在应用目录下的`src/api/request.ts`文件夹下，有对应的请求配置文件,如`web-antd`项目下的`src/api/request.ts`文件,可以根据自己的需求进行配置。

### 扩展的配置

除了基础的Axios配置外，扩展了部分配置。

```ts
type ExtendOptions<T = any> = {
  /**
   * 参数序列化方式。预置了几种针对数组的序列化类型
   * - brackets: ids[]=1&ids[]=2&ids[]=3
   * - comma: ids=1,2,3
   * - indices: ids[0]=1&ids[1]=2&ids[2]=3
   * - repeat: ids=1&ids=2&ids=3
   * @default 'brackets'
   */
  paramsSerializer?:
    | 'brackets'
    | 'comma'
    | 'indices'
    | 'repeat'
    | AxiosRequestConfig<T>['paramsSerializer'];
  /**
   * 响应数据的返回方式。
   * - raw: 原始的AxiosResponse，包括headers、status等，不做是否成功请求的检查。
   * - body: 返回响应数据的BODY部分（只会根据status检查请求是否成功，忽略对code的判断，这种情况下应由调用方检查请求是否成功）。
   * - data: 解构响应的BODY数据，只返回其中的data节点数据（会检查status和code是否为成功状态）。
   */
  responseReturn?: 'body' | 'data' | 'raw';
};
```

### 请求示例

#### GET 请求

```ts
import { requestClient } from '#/api/request';

export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/user/info');
}
```

#### POST/PUT 请求

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
    // 或者 PUT
    method: user.id ? 'PUT' : 'POST',
  });
}
```

#### DELETE 请求

```ts
import { requestClient } from '#/api/request';

export async function deleteUserApi(userId: number) {
  return requestClient.delete<boolean>(`/user/${userId}`);
}
```

### 请求配置

应用内的`src/api/request.ts`可以根据自己应用的情况的需求进行配置：

```ts
/**
 * 该文件可自行根据业务逻辑进行调整
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
    const resp = await refreshTokenApi();
    const newToken = resp.data;
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
      return config;
    },
  });

  // 处理返回的响应数据格式。会根据responseReturn指定的类型返回对应的数据
  client.addResponseInterceptor(
    defaultResponseInterceptor({
      // 指定接口返回的数据中的 code 字段名
      codeField: 'code',
      // 指定接口返回的数据中装载了主要数据的字段名
      dataField: 'data',
      // 请求成功的 code 值，如果接口返回的 code 等于 successCode 则会认为是成功的请求
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

### 多个接口地址

只需要创建多个 `requestClient` 即可，如：

```ts
const { apiURL, otherApiURL } = useAppConfig(
  import.meta.env,
  import.meta.env.PROD,
);

export const requestClient = createRequestClient(apiURL);

export const otherRequestClient = createRequestClient(otherApiURL);
```

## 刷新Token

项目中默认提供了刷新 Token 的逻辑，只需要按照下面的配置即可开启：

- 确保当前启用了刷新 Token 的配置

调整对应应用目录下的`preferences.ts`，确保`enableRefreshToken='true'`。

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    enableRefreshToken: true,
  },
});
```

在 `src/api/request.ts` 中配置 `doRefreshToken` 方法即可:

```ts
// 这里调整为你的token格式
function formatToken(token: null | string) {
  return token ? `Bearer ${token}` : null;
}

/**
 * 刷新token逻辑
 */
async function doRefreshToken() {
  const accessStore = useAccessStore();
  // 这里调整为你的刷新token接口
  const resp = await refreshTokenApi();
  const newToken = resp.data;
  accessStore.setAccessToken(newToken);
  return newToken;
}
```

## 数据 Mock

::: tip 生产环境 Mock

新版本不再支持生产环境 mock，请使用真实接口。

:::

Mock 数据是前端开发过程中必不可少的一环，是分离前后端开发的关键链路。通过预先跟服务器端约定好的接口，模拟请求数据甚至逻辑，能够让前端开发独立自主，不会被服务端的开发进程所阻塞。

项目使用 [Nitro](https://nitro.unjs.io/) 来进行本地 mock 数据处理。其原理是本地额外启动一个后端服务，是一个真实的后端服务，可以处理请求，返回数据。

### Nitro 使用

Mock 服务代码位于`apps/backend-mock`目录下，无需手动启动，已经集成在项目中，只需要在项目根目录下运行`pnpm dev`即可，运行成功之后，控制台会打印 `http://localhost:5320/api`, 访问该地址即可查看 mock 服务。

[Nitro](https://nitro.unjs.io/) 语法简单，可以根据自己的需求进行配置及开发，具体配置可以查看 [Nitro 文档](https://nitro.unjs.io/)。

## 关闭 Mock 服务

mock的本质是一个真实的后端服务，如果不需要 mock 服务，可以在项目根目录下的 `.env.development` 文件中配置 `VITE_NITRO_MOCK=false` 即可关闭 mock 服务。

```bash
# .env.development
VITE_NITRO_MOCK=false
```
