import type { PluginOption } from 'vite';

import { lazyImport, VxeResolver } from 'vite-plugin-lazy-import';

async function viteVxeTableImportsPlugin(): Promise<PluginOption> {
  return [
    lazyImport({
      resolvers: [
        VxeResolver({
          libraryName: 'vxe-table',
        }),
        VxeResolver({
          libraryName: 'vxe-pc-ui',
        }),
      ],
    }),
  ];
}

export { viteVxeTableImportsPlugin };
