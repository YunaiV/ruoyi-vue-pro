# Icons

::: tip About Icon Management

- The icons in the project are mainly provided by the `@vben/icons` package. It is recommended to manage them within this package for unified management and maintenance.
- If you are using `Vscode`, it is recommended to install the [Iconify IntelliSense](https://marketplace.visualstudio.com/items?itemName=antfu.iconify) plugin, which makes it easy to find and use icons.

:::

There are several ways to use icons in the project, you can choose according to the actual situation:

## Iconify Icons <Badge text="Recommended" type="tip"/>

Integrated with the [iconify](https://github.com/iconify/iconify) icon library

### Adding New Icons

You can add new icons in the `packages/icons/src/iconify` directory:

```ts
// packages/icons/src/iconify/index.ts
import { createIconifyIcon } from '@vben-core/icons';

export const MdiKeyboardEsc = createIconifyIcon('mdi:keyboard-esc');
```

### Usage

```vue
<script setup lang="ts">
import { MdiKeyboardEsc } from '@vben/icons';
</script>

<template>
  <!-- An icon with a width and height of 20px -->
  <MdiKeyboardEsc class="size-5" />
</template>
```

## SVG Icons <Badge text="Recommended" type="tip"/>

Instead of using Svg Sprite, SVG icons are directly imported,

### Adding New Icons

You can add new icon files `test.svg` in the `packages/icons/src/svg/icons` directory, and then import it in `packages/icons/src/svg/index.ts`:

```ts
// packages/icons/src/svg/index.ts
import { createIconifyIcon } from '@vben-core/icons';

const SvgTestIcon = createIconifyIcon('svg:test');

export { SvgTestIcon };
```

### Usage

```vue
<script setup lang="ts">
import { SvgTestIcon } from '@vben/icons';
</script>

<template>
  <!-- An icon with a width and height of 20px -->
  <SvgTestIcon class="size-5" />
</template>
```

## Tailwind CSS Icons <Badge text="Not Recommended" type="danger"/>

### Usage

You can use the icons by directly adding the Tailwind CSS icon class names, which can be found on [iconify](https://github.com/iconify/iconify) ：

```vue
<span class="icon-[mdi--ab-testing]"></span>
```
