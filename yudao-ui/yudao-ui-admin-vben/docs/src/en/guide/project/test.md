# Unit Testing

The project incorporates [Vitest](https://vitest.dev/) as the unit testing tool. Vitest is a test runner based on Vite, offering a simple API for writing test cases.

## Writing Test Cases

Within the project, we follow the convention of naming test files with a `.test.ts` suffix or placing them inside a `__tests__` directory. For example, if you create a `utils.ts` file, then you would create a corresponding `utils.spec.ts` file in the same directory,

```ts
// utils.test.ts
import { expect, test } from 'vitest';
import { sum } from './sum';

test('adds 1 + 2 to equal 3', () => {
  expect(sum(1, 2)).toBe(3);
});
```

## Running Tests

To run the tests, execute the following command at the root of the monorepo:

```bash
pnpm test:unit
```

## Existing Unit Tests

There are already some unit test cases in the project. You can search for files ending with .test.ts to view them. When you make changes to related code, you can run the unit tests to ensure the correctness of your code. It is recommended to maintain the coverage of unit tests during the development process and to run unit tests as part of the CI/CD process to ensure tests pass before deploying the project.

Existing unit test status:

![](/guide/test.png)
