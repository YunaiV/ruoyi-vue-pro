import createCommand from 'eslint-plugin-command/config';

export async function command() {
  return [
    {
      // @ts-expect-error - no types
      ...createCommand(),
    },
  ];
}
