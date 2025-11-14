# 外部模块

除了项目默认引入的外部模块，有时我们还需要引入其他外部模块。我们以 [ant-design-vue](https://antdv.com/components/overview) 为例：

## 安装依赖

::: tip 安装依赖到指定包

- 由于项目采用了 [pnpm](https://pnpm.io/) 作为包管理工具，所以我们需要使用 `pnpm` 命令来安装依赖。
- 通过采用了 Monorepo 模块来管理项目，所以我们需要在指定包下安装依赖。安装依赖前请确保已经进入到指定包目录下。

:::

```bash
# cd /path/to/your/package
pnpm add ant-design-vue
```

## 使用

### 全局引入

```ts
import { createApp } from 'vue';
import Antd from 'ant-design-vue';
import App from './App';
import 'ant-design-vue/dist/reset.css';

const app = createApp(App);

app.use(Antd).mount('#app');
```

#### 使用

```vue
<template>
  <a-button>text</a-button>
</template>
```

### 局部引入

```vue
<script setup lang="ts">
import { Button } from 'ant-design-vue';
</script>

<template>
  <Button>text</Button>
</template>
```

::: warning 注意

- 如果组件有依赖样式，则需要再引入样式文件

:::
