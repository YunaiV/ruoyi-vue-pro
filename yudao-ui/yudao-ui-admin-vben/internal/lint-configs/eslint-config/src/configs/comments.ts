import type { Linter } from 'eslint';

import { interopDefault } from '../util';

export async function comments(): Promise<Linter.Config[]> {
  const [pluginComments] = await Promise.all([
    // @ts-expect-error - no types
    interopDefault(import('eslint-plugin-eslint-comments')),
  ] as const);

  return [
    {
      plugins: {
        'eslint-comments': pluginComments,
      },
      rules: {
        'eslint-comments/no-aggregating-enable': 'error',
        'eslint-comments/no-duplicate-disable': 'error',
        'eslint-comments/no-unlimited-disable': 'error',
        'eslint-comments/no-unused-enable': 'error',
      },
    },
  ];
}
