---
outline: deep
---

# 登录

本文介绍如何去改造自己的应用程序登录页以及如何快速的对接登录页面接口。

## 登录页面调整

如果你想调整登录页面的标题、描述和图标以及工具栏，你可以通过配置 `AuthPageLayout` 组件的参数来实现。

![login](/guide/login.png)

只需要在应用下的 `src/layouts/auth.vue` 内，配置`AuthPageLayout`的 `props`参数即可：

```vue {2-7}
<AuthPageLayout
  :copyright="true"
  :toolbar="true"
  :toolbarList="['color', 'language', 'layout', 'theme']"
  :app-name="appName"
  :logo="logo"
  :page-description="$t('authentication.pageDesc')"
  :page-title="$t('authentication.pageTitle')"
>
</AuthPageLayout>
```

## 登录表单调整

如果你想调整登录表单的相关内容，你可以在应用下的 `src/views/_core/authentication/login.vue` 内，配置`AuthenticationLogin` 组件参数即可：

```vue
<AuthenticationLogin
  :loading="authStore.loginLoading"
  @submit="authStore.authLogin"
/>
```

::: details AuthenticationLogin 组件参数

```ts
{
  /**
   * @zh_CN 验证码登录路径
   */
  codeLoginPath?: string;
  /**
   * @zh_CN 忘记密码路径
   */
  forgetPasswordPath?: string;

  /**
   * @zh_CN 是否处于加载处理状态
   */
  loading?: boolean;

  /**
   * @zh_CN 二维码登录路径
   */
  qrCodeLoginPath?: string;

  /**
   * @zh_CN 注册路径
   */
  registerPath?: string;

  /**
   * @zh_CN 是否显示验证码登录
   */
  showCodeLogin?: boolean;
  /**
   * @zh_CN 是否显示忘记密码
   */
  showForgetPassword?: boolean;

  /**
   * @zh_CN 是否显示二维码登录
   */
  showQrcodeLogin?: boolean;

  /**
   * @zh_CN 是否显示注册按钮
   */
  showRegister?: boolean;

  /**
   * @zh_CN 是否显示记住账号
   */
  showRememberMe?: boolean;

  /**
   * @zh_CN 是否显示第三方登录
   */
  showThirdPartyLogin?: boolean;

  /**
   * @zh_CN 登录框子标题
   */
  subTitle?: string;

  /**
   * @zh_CN 登录框标题
   */
  title?: string;

}
```

:::

::: tip Note

如果这些配置不能满足你的需求，你可以自行实现登录表单及相关登录逻辑或者给我们提交 `PR`。

:::

## 接口对接流程

这里将会快速的介绍如何快速对接自己的后端。

### 前置条件

- 首先文档用的后端服务，接口返回的格式统一如下：

```ts
interface HttpResponse<T = any> {
  /**
   * 0 表示成功 其他表示失败
   * 0 means success, others means fail
   */
  code: number;
  data: T;
  message: string;
}
```

如果你不符合这个格式，你需要先阅读 [服务端交互](../essentials/server.md) 文档，改造你的`request.ts`配置。

- 其次你需要在先将本地代理地址改为你的真实后端地址，你可以在应用下的 `vite.config.mts` 内配置：

```ts
import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    vite: {
      server: {
        proxy: {
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // 这里改为你的真实接口地址
            target: 'http://localhost:5320/api',
            ws: true,
          },
        },
      },
    },
  };
});
```

### 登录接口

为了能正常登录，你的后端最少需要提供 `2-3` 个接口：

- 登录接口

接口地址可在应用下的 `src/api/core/auth` 内修改，以下为默认接口地址：

```ts
/**
 * 登录
 */
export async function loginApi(data: AuthApi.LoginParams) {
  return requestClient.post<AuthApi.LoginResult>('/auth/login', data);
}

/** 只需要保证登录接口返回值有 `accessToken` 字段即可 */
export interface LoginResult {
  accessToken: string;
}
```

- 获取用户信息接口

接口地址可在应用下的 `src/api/core/user` 内修改，以下为默认接口地址：

```ts
export async function getUserInfoApi() {
  return requestClient.get<UserInfo>('/user/info');
}

/** 只需要保证登录接口返回值有以下字段即可，多的字段可以自行使用 */
export interface UserInfo {
  roles: string[];
  realName: string;
}
```

- 获取权限码 (可选)

这个接口用于获取用户的权限码，权限码是用于控制用户的权限的，接口地址可在应用下的 `src/api/core/auth` 内修改，以下为默认接口地址：

```ts
export async function getAccessCodesApi() {
  return requestClient.get<string[]>('/auth/codes');
}
```

如果你不需要这个权限，你只需要把代码改为返回一个空数组即可。

```ts {2}
export async function getAccessCodesApi() {
  // 这里返回一个空数组即可
  return [];
}
```
