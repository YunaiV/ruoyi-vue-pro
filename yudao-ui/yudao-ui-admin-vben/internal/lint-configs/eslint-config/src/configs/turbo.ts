import type { Linter } from 'eslint';

import { interopDefault } from '../util';

export async function turbo(): Promise<Linter.Config[]> {
  const [pluginTurbo] = await Promise.all([
    interopDefault(import('eslint-config-turbo')),
  ] as const);

  return [
    {
      plugins: {
        turbo: pluginTurbo,
      },
    },
  ];
}
