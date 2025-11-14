# 单元测试

项目内置了 [Vitest](https://vitest.dev/) 作为单元测试工具。Vitest 是一个基于 Vite 的测试运行器，它提供了一套简单的 API 来编写测试用例。

## 编写测试用例

在项目中，我们约定将测试文件名以 `.test.ts` 结尾，或者存放到`__tests__`目录内。例如，创建一个 `utils.ts` 文件，然后同级目录`utils.test.ts` 文件，

```ts
// utils.test.ts
import { expect, test } from 'vitest';
import { sum } from './sum';

test('adds 1 + 2 to equal 3', () => {
  expect(sum(1, 2)).toBe(3);
});
```

## 运行测试

在大仓根目录下运行以下命令即可：

```bash
pnpm test:unit
```

## 现有单元测试

项目中已经有一些单元测试用例，可以搜索以`.test.ts`结尾的文件查看，在你更改到相关代码时，可以运行单元测试来保证代码的正确性，建议在开发过程中，保持单元测试的覆盖率，且同时在 CI/CD 流程中运行单元测试，保证测试通过在进行项目部署。

现有单元测试情况：

![](/guide/test.png)
