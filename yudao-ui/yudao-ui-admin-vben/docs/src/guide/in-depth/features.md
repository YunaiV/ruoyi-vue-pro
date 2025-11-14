# 常用功能

一些常用的功能合集。

## 登录认证过期

当接口返回`401`状态码时，框架会认为登录认证过期，登录超时会跳转到登录页或者打开登录弹窗。在应用目录下的`preferences.ts`可以配置：

### 跳转登录页面

登录超时会跳转到登录页

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    loginExpiredMode: 'page',
  },
});
```

### 打开登录弹窗

登录超时会打开登录弹窗

![](/guide/login-expired.png)

配置：

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    loginExpiredMode: 'modal',
  },
});
```

## 动态标题

- 默认值：`true`

开启后网页标题随着路由的`title`而变化。在应用目录下的`preferences.ts`，开启或者关闭即可。

```ts
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    dynamicTitle: true,
  },
});
```

## 页面水印

- 默认值：`false`

开启后网页会显示水印，在应用目录下的`preferences.ts`，开启或者关闭即可。

```ts
export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    watermark: true,
  },
});
```

如果你想更新水印的内容，可以这么做，参数可以参考 [watermark-js-plus](https://zhensherlock.github.io/watermark-js-plus/)：

```ts
import { useWatermark } from '@vben/hooks';

const { destroyWatermark, updateWatermark } = useWatermark();

await updateWatermark({
  // 水印内容
  content: 'hello my watermark',
});
```
