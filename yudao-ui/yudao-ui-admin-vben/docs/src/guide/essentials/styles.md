# 样式

::: tip 前言

对于 vue 项目，[官方文档](https://vuejs.org/api/sfc-css-features.html#deep-selectors) 对语法已经有比较详细的介绍，这里主要是介绍项目中的样式文件结构和使用。

:::

## 项目结构

项目中的样式文件存放在 `@vben/styles`，包含一些全局样式，如重置样式、全局变量等，它继承了 `@vben-core/design` 的样式和能力，可以根据项目需求进行覆盖。

## Scss

项目中使用 `scss` 作为样式预处理器，可以在项目中使用 `scss` 的特性，如变量、函数、混合等。

```vue
<style lang="scss" scoped>
$font-size: 30px;

.box {
  .title {
    color: green;
    font-size: $font-size;
  }
}
</style>
```

## Postcss

如果你不习惯使用 `scss`，也可以使用 `postcss`，它是一个更加强大的样式处理器，可以使用更多的插件，项目内置了 [postcss-nested](https://github.com/postcss/postcss-nested) 插件，配置 `Css Variables`，完全可以取代 `scss`。

```vue
<style scoped>
.box {
  --font-size: 30px;
  .title {
    color: green;
    font-size: var(--font-size);
  }
}
</style>
```

## Tailwind CSS

项目中集成了 [Tailwind CSS](https://tailwindcss.com/)，可以在项目中使用 `tailwindcss` 的类名，快速构建页面。

```vue
<template>
  <div class="bg-white p-4">
    <p class="text-green">hello world</p>
  </div>
</template>
```

## BEM 规范

样式冲突的另一种选择，是使用 `BEM` 规范。如果选择 `scss` ，建议使用 `BEM` 命名规范，可以更好的管理样式。项目默认提供了`useNamespace`函数，可以方便的生成命名空间。

```vue
<script lang="ts" setup>
import { useNamespace } from '@vben/hooks';

const { b, e, is } = useNamespace('menu');
</script>
<template>
  <div :class="[b()]">
    <div :class="[e('item'), is('active', true)]">item1</div>
  </div>
</template>
<style lang="scss" scoped>
// 如果你在应用内使用，这行代码可以省略，已经在所有的应用内全局引入了
@use '@vben/styles/global' as *;
@include b('menu') {
  color: black;

  @include e('item') {
    background-color: black;

    @include is('active') {
      background-color: red;
    }
  }
}
</style>
```

## CSS Modules

针对样式冲突问题，还有一种方案是使用 `CSS Modules` 模块化方案。使用方式如下。

```vue
<template>
  <p :class="$style.red">This should be red</p>
</template>

<style module>
.red {
  color: red;
}
</style>
```

更多用法可以见 [CSS Modules 官方文档](https://vuejs.org/api/sfc-css-features.html#css-modules)。
