# Styles

::: tip Preface

For Vue projects, the [official documentation](https://vuejs.org/api/sfc-css-features.html#deep-selectors) already provides a detailed introduction to the syntax. Here, we mainly introduce the structure and usage of style files in the project.

:::

## Project Structure

The style files in the project are stored in `@vben/styles`, which includes some global styles, such as reset styles, global variables, etc. It inherits the styles and capabilities of `@vben-core/design` and can be overridden according to project needs.

## Scss

The project uses `scss` as the style preprocessor, allowing the use of `scss` features such as variables, functions, mixins, etc., within the project.

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

If you're not accustomed to using `scss`, you can also use `postcss`, which is a more powerful style processor that supports a wider range of plugins. The project includes the [postcss-nested](https://github.com/postcss/postcss-nested) plugin and is configured with `Css Variables`, making it a complete substitute for `scss`.

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

The project integrates [Tailwind CSS](https://tailwindcss.com/), allowing the use of `tailwindcss` class names to quickly build pages.

```vue
<template>
  <div class="bg-white p-4">
    <p class="text-green">hello world</p>
  </div>
</template>
```

## BEM Standard

Another option to avoid style conflicts is to use the `BEM` standard. If you choose `scss`, it is recommended to use the `BEM` naming convention for better style management. The project provides a default `useNamespace` function to easily generate namespaces.

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
// If you use it within the application, this line of code can be omitted as it has already been globally introduced in all applications
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

Another solution to address style conflicts is to use the `CSS Modules` modular approach. The usage method is as follows.

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

For more usage, see the [CSS Modules official documentation](https://vuejs.org/api/sfc-css-features.html#css-modules).
