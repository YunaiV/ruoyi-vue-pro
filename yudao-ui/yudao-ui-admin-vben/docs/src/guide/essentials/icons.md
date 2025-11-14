# 图标

::: tip 关于图标的管理

- 项目的图标主要由`@vben/icons`包提供，建议统一在该包内部管理，以便于统一管理和维护。
- 如果你使用的是 `Vscode`，推荐安装 [Iconify IntelliSense](https://marketplace.visualstudio.com/items?itemName=antfu.iconify) 插件，可以方便的查找和使用图标。

:::

项目中有以下多种图标使用方式，可以根据实际情况选择使用：

## Iconify 图标 <Badge text="推荐" type="tip"/>

集成了 [iconify](https://github.com/iconify/iconify) 图标库

### 新增

可在 `packages/icons/src/iconify` 目录下新增图标：

```ts
// packages/icons/src/iconify/index.ts
import { createIconifyIcon } from '@vben-core/icons';

export const MdiKeyboardEsc = createIconifyIcon('mdi:keyboard-esc');
```

### 使用

```vue
<script setup lang="ts">
import { MdiKeyboardEsc } from '@vben/icons';
</script>

<template>
  <!-- 一个宽高为20px的图标 -->
  <MdiKeyboardEsc class="size-5" />
</template>
```

## Svg 图标 <Badge text="推荐" type="tip"/>

没有采用 Svg Sprite 的方式，而是直接引入 Svg 图标，

### 新增

可以在 `packages/icons/src/svg/icons` 目录下新增图标文件`test.svg`, 然后在 `packages/icons/src/svg/index.ts` 中引入：

```ts
// packages/icons/src/svg/index.ts
import { createIconifyIcon } from '@vben-core/icons';

const SvgTestIcon = createIconifyIcon('svg:test');

export { SvgTestIcon };
```

### 使用

```vue
<script setup lang="ts">
import { SvgTestIcon } from '@vben/icons';
</script>

<template>
  <!-- 一个宽高为20px的图标 -->
  <SvgTestIcon class="size-5" />
</template>
```

## Tailwind CSS 图标

### 使用

直接添加 Tailwind CSS 的图标类名即可使用，图标类名可查看 [iconify](https://github.com/iconify/iconify) ：

```vue
<span class="icon-[mdi--ab-testing]"></span>
```
