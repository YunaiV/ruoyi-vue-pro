# Vite Config

项目封装了一层vite配置，并集成了一些插件，方便在多个包以及应用内复用，使用方式如下：

## 应用

```ts
// vite.config.mts
import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {},
    // vite配置，参考vite官方文档进行覆盖
    vite: {},
  };
});
```

## 包

```ts
// vite.config.mts
import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    library: {},
    // vite配置，参考vite官方文档进行覆盖
    vite: {},
  };
});
```
