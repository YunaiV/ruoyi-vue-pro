# 检查更新

## 介绍

当网站有更新时，您可能需要检查更新。框架提供了这一功能，通过定时检查更新，您可以在应用的 preferences.ts 文件中配置 `checkUpdatesInterval`和 `enableCheckUpdates` 字段，以开启和设置检查更新的时间间隔（单位：分钟）。

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    // 是否开启检查更新
    enableCheckUpdates: true,
    // 检查更新的时间间隔，单位为分钟
    checkUpdatesInterval: 1,
  },
});
```

## 效果

检测到更新时，会弹出提示框，询问用户是否刷新页面：

![check-updates](/guide/update-notice.png)

## 替换为其他检查更新方式

如果需要通过其他方式检查更新，例如通过接口来更灵活地控制更新逻辑（如强制刷新、显示更新内容等），你可以通过修改 `@vben/layouts` 下面的 `src/widgets/check-updates/check-updates.vue`文件来实现。

```ts
// 这里可以替换为你的检查更新逻辑
async function getVersionTag() {
  try {
    const response = await fetch('/', {
      cache: 'no-cache',
      method: 'HEAD',
    });

    return (
      response.headers.get('etag') || response.headers.get('last-modified')
    );
  } catch {
    console.error('Failed to fetch version tag');
    return null;
  }
}
```

## 替换为第三方库检查更新方式

如果需要通过其他方式检查更新，例如使用其他版本控制方式（chunkHash、version.json）、使用`Web Worker`在后台轮询更新、自定义检查更新时机（不使用轮询），你可以通过JS库`version-polling`来实现。

```bash
pnpm add version-polling
```

以`apps/web-antd`项目为例，在项目入口文件`main.ts`或者`app.vue`添加以下代码

```ts
import { h } from 'vue';

import { Button, notification } from 'ant-design-vue';
import { createVersionPolling } from 'version-polling';

createVersionPolling({
  silent: import.meta.env.MODE === 'development', // 开发环境下不检测
  onUpdate: (self) => {
    const key = `open${Date.now()}`;
    notification.info({
      message: '提示',
      description: '检测到网页有更新, 是否刷新页面加载最新版本？',
      btn: () =>
        h(
          Button,
          {
            type: 'primary',
            size: 'small',
            onClick: () => {
              notification.close(key);
              self.onRefresh();
            },
          },
          { default: () => '刷新' },
        ),
      key,
      duration: null,
      placement: 'bottomRight',
    });
  },
});
```
