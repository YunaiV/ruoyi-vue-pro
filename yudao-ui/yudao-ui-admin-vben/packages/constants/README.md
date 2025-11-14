# @vben/constants

用于多个 `app` 公用的常量，继承了 `@vben-core/shared/constants` 的所有能力。业务上有通用常量可以放在这里。

## 用法

### 添加依赖

```bash
# 进入目标应用目录，例如 apps/xxxx-app
# cd apps/xxxx-app
pnpm add @vben/constants
```

### 使用

```ts
import { LOGIN_PATH } from '@vben/constants';
```
