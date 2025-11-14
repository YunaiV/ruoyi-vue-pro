# 国际化

项目已经集成了 [Vue i18n](https://kazupon.github.io/vue-i18n/)，并且已经配置好了中文和英文的语言包。

## IDE 插件

如果你使用的 vscode 开发工具，则推荐安装 [i18n Ally](https://marketplace.visualstudio.com/items?itemName=Lokalise.i18n-ally) 这个插件。它可以帮助你更方便的管理国际化的文案，安装了该插件后，你的代码内可以实时看到对应的语言内容：

![](/public/guide/locale.png)

## 配置默认语言

只需要覆盖默认的偏好设置即可，在对应的应用内，找到 `src/preferences.ts` 文件，修改 `locale` 的值即可：

```ts {3}
export const overridesPreferences = defineOverridesPreferences({
  app: {
    locale: 'en-US',
  },
});
```

## 动态切换语言

切换语言有两部分组成:

- 更新偏好设置
- 加载对应的语言包

```ts
import type { SupportedLanguagesType } from '@vben/locales';
import { loadLocaleMessages } from '@vben/locales';
import { updatePreferences } from '@vben/preferences';

async function updateLocale(value: string) {
  // 1. 更新偏好设置
  const locale = value as SupportedLanguagesType;
  updatePreferences({
    app: {
      locale,
    },
  });
  // 2. 加载对应的语言包
  await loadLocaleMessages(locale);
}

updateLocale('en-US');
```

## 新增翻译文本

::: warning 注意

- 请不要将业务翻译文本放到 `@vben/locales` 内，这样可以更好的管理业务和通用的翻译文本。
- 有多个语言包的情况下，新增翻译文本时，需要在所有语言包内新增对应的文本。

:::

新增翻译文本，只需要在对应的应用内，找到 `src/locales/langs/`，新增对应的文本即可，例:

**src/locales/langs/zh-CN/\*.json**

````ts
```json
{
  "about": {
    "desc": "Vben Admin 是一个现代的管理模版。"
  }
}
````

**src/locales/langs/en-US.ts**

````ts
```json
{
  "about": {
    "desc": "Vben Admin is a modern management template."
  }
}
````

## 使用翻译文本

通过 `@vben/locales`，你可以很方便的使用翻译文本：

### 在代码中使用

```vue
<script setup lang="ts">
import { computed } from 'vue';
import { $t } from '@vben/locales';

const items = computed(() => [{ title: $t('about.desc') }]);
</script>
<template>
  <div>{{ $t('about.desc') }}</div>
  <template v-for="item in items.value">
    <div>{{ item.title }}</div>
  </template>
</template>
```

## 新增语言包

如果你需要新增语言包，需要按照以下步骤进行：

- 在 `packages/locales/langs` 目录下新增对应的语言包文件，例：`zh-TW.json`，并翻译对应的文本。
- 在对应的应用内，找到 `src/locales/langs` 文件，新增对应的语言包 `zh-TW.json`
- 在 `packages/constants/src/core.ts`内，新增对应的语言：

  ```ts
  export interface LanguageOption {
    label: string;
    value: 'en-US' | 'zh-CN'; // [!code --]
    value: 'en-US' | 'zh-CN' | 'zh-TW'; // [!code ++]
  }
  export const SUPPORT_LANGUAGES: LanguageOption[] = [
    {
      label: '简体中文',
      value: 'zh-CN',
    },
    {
      label: 'English',
      value: 'en-US',
    },
    {
      label: '繁体中文', // [!code ++]
      value: 'zh-TW', // [!code ++]
    },
  ];
  ```

- 在 `packages/locales/typing.ts`内，新增 Typescript 类型：

  ```ts
  export type SupportedLanguagesType = 'en-US' | 'zh-CN'; // [!code --]
  export type SupportedLanguagesType = 'en-US' | 'zh-CN' | 'zh-TW'; // [!code ++]
  ```

到这里，你就可以在项目内使用新增的语言包了。

## 界面切换语言功能

如果你想关闭界面上的语言切换显示按钮，在对应的应用内，找到 `src/preferences.ts` 文件，修改 `locale` 的值即可：

```ts {3}
export const overridesPreferences = defineOverridesPreferences({
  widget: {
    languageToggle: false,
  },
});
```

## 远程加载语言包

::: tip 提示

通过项目自带的`request`工具进行接口请求时，默认请求头里会带上 [Accept-Language](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Accept-Language) ，服务端可根据请求头进行动态数据国际化处理。

:::

每个应用都有一个独立的语言包，它可以覆盖通用的语言配置，你可以通过远程加载的方式来获取对应的语言包，只需要在对应的应用内，找到 `src/locales/index.ts` 文件，修改 `loadMessages` 方法即可：

```ts {3-4}
async function loadMessages(lang: SupportedLanguagesType) {
  const [appLocaleMessages] = await Promise.all([
    // 这里修改为远程接口加载数据即可
    localesMap[lang](),
    loadThirdPartyMessage(lang),
  ]);
  return appLocaleMessages.default;
}
```

## 第三方语言包

不同应用内使用的第三方组件库或者插件国际化方式可能不一致，所以需要差别处理。 如果你需要引入第三方的语言包，你可以在对应的应用内，找到 `src/locales/index.ts` 文件，修改 `loadThirdPartyMessage` 方法即可：

```ts
/**
 * 加载dayjs的语言包
 * @param lang
 */
async function loadDayjsLocale(lang: SupportedLanguagesType) {
  let locale;
  switch (lang) {
    case 'zh-CN': {
      locale = await import('dayjs/locale/zh-cn');
      break;
    }
    case 'en-US': {
      locale = await import('dayjs/locale/en');
      break;
    }
    // 默认使用英语
    default: {
      locale = await import('dayjs/locale/en');
    }
  }
  if (locale) {
    dayjs.locale(locale);
  } else {
    console.error(`Failed to load dayjs locale for ${lang}`);
  }
}
```

## 移除国际化

首先，不是很建议移除国际化，因为国际化是一个很好的开发习惯，但是如果你真的需要移除国际化，你可以直接使用中文文案，然后保留项目自带的语言包即可，整体开发体验不会影响。移除国际化的步骤如下：

- 隐藏界面上的语言切换按钮，见：[界面切换语言功能](#界面切换语言功能)
- 修改默认语言，见：[配置默认语言](#配置默认语言)
- 关闭 `vue-i18n`的警告提示，在`src/locales/index.ts`文件内，修改`missingWarn`为`false`即可：

  ```ts
  async function setupI18n(app: App, options: LocaleSetupOptions = {}) {
    await coreSetup(app, {
      defaultLocale: preferences.app.locale,
      loadMessages,
      missingWarn: !import.meta.env.PROD, // [!code --]
      missingWarn: false, // [!code ++]
      ...options,
    });
  }
  ```
