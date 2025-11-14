import type { PluginOption } from 'vite';

import type {
  ApplicationPluginOptions,
  CommonPluginOptions,
  ConditionPlugin,
  LibraryPluginOptions,
} from '../typing';

import viteVueI18nPlugin from '@intlify/unplugin-vue-i18n/vite';
import viteVue from '@vitejs/plugin-vue';
import viteVueJsx from '@vitejs/plugin-vue-jsx';
import { visualizer as viteVisualizerPlugin } from 'rollup-plugin-visualizer';
import viteCompressPlugin from 'vite-plugin-compression';
import viteDtsPlugin from 'vite-plugin-dts';
import { createHtmlPlugin as viteHtmlPlugin } from 'vite-plugin-html';
import { VitePWA } from 'vite-plugin-pwa';
import viteVueDevTools from 'vite-plugin-vue-devtools';

import { viteArchiverPlugin } from './archiver';
import { viteExtraAppConfigPlugin } from './extra-app-config';
import { viteImportMapPlugin } from './importmap';
import { viteInjectAppLoadingPlugin } from './inject-app-loading';
import { viteMetadataPlugin } from './inject-metadata';
import { viteLicensePlugin } from './license';
import { viteNitroMockPlugin } from './nitro-mock';
import { vitePrintPlugin } from './print';
import { viteVxeTableImportsPlugin } from './vxe-table';

/**
 * 获取条件成立的 vite 插件
 * @param conditionPlugins
 */
async function loadConditionPlugins(conditionPlugins: ConditionPlugin[]) {
  const plugins: PluginOption[] = [];
  for (const conditionPlugin of conditionPlugins) {
    if (conditionPlugin.condition) {
      const realPlugins = await conditionPlugin.plugins();
      plugins.push(...realPlugins);
    }
  }
  return plugins.flat();
}

/**
 * 根据条件获取通用的vite插件
 */
async function loadCommonPlugins(
  options: CommonPluginOptions,
): Promise<ConditionPlugin[]> {
  const { devtools, injectMetadata, isBuild, visualizer } = options;
  return [
    {
      condition: true,
      plugins: () => [
        viteVue({
          script: {
            defineModel: true,
            // propsDestructure: true,
          },
        }),
        viteVueJsx(),
      ],
    },

    {
      condition: !isBuild && devtools,
      plugins: () => [viteVueDevTools()],
    },
    {
      condition: injectMetadata,
      plugins: async () => [await viteMetadataPlugin()],
    },
    {
      condition: isBuild && !!visualizer,
      plugins: () => [<PluginOption>viteVisualizerPlugin({
          filename: './node_modules/.cache/visualizer/stats.html',
          gzipSize: true,
          open: true,
        })],
    },
  ];
}

/**
 * 根据条件获取应用类型的vite插件
 */
async function loadApplicationPlugins(
  options: ApplicationPluginOptions,
): Promise<PluginOption[]> {
  // 单独取，否则commonOptions拿不到
  const isBuild = options.isBuild;
  const env = options.env;

  const {
    archiver,
    archiverPluginOptions,
    compress,
    compressTypes,
    extraAppConfig,
    html,
    i18n,
    importmap,
    importmapOptions,
    injectAppLoading,
    license,
    nitroMock,
    nitroMockOptions,
    print,
    printInfoMap,
    pwa,
    pwaOptions,
    vxeTableLazyImport,
    ...commonOptions
  } = options;

  const commonPlugins = await loadCommonPlugins(commonOptions);

  return await loadConditionPlugins([
    ...commonPlugins,
    {
      condition: i18n,
      plugins: async () => {
        return [
          viteVueI18nPlugin({
            compositionOnly: true,
            fullInstall: true,
            runtimeOnly: true,
          }),
        ];
      },
    },
    {
      condition: print,
      plugins: async () => {
        return [await vitePrintPlugin({ infoMap: printInfoMap })];
      },
    },
    {
      condition: vxeTableLazyImport,
      plugins: async () => {
        return [await viteVxeTableImportsPlugin()];
      },
    },
    {
      condition: nitroMock,
      plugins: async () => {
        return [await viteNitroMockPlugin(nitroMockOptions)];
      },
    },

    {
      condition: injectAppLoading,
      plugins: async () => [await viteInjectAppLoadingPlugin(!!isBuild, env)],
    },
    {
      condition: license,
      plugins: async () => [await viteLicensePlugin()],
    },
    {
      condition: pwa,
      plugins: () =>
        VitePWA({
          injectRegister: false,
          workbox: {
            globPatterns: [],
          },
          ...pwaOptions,
          manifest: {
            display: 'standalone',
            start_url: '/',
            theme_color: '#ffffff',
            ...pwaOptions?.manifest,
          },
        }),
    },
    {
      condition: isBuild && !!compress,
      plugins: () => {
        const compressPlugins: PluginOption[] = [];
        if (compressTypes?.includes('brotli')) {
          compressPlugins.push(
            viteCompressPlugin({ deleteOriginFile: false, ext: '.br' }),
          );
        }
        if (compressTypes?.includes('gzip')) {
          compressPlugins.push(
            viteCompressPlugin({ deleteOriginFile: false, ext: '.gz' }),
          );
        }
        return compressPlugins;
      },
    },
    {
      condition: !!html,
      plugins: () => [viteHtmlPlugin({ minify: true })],
    },
    {
      condition: isBuild && importmap,
      plugins: () => {
        return [viteImportMapPlugin(importmapOptions)];
      },
    },
    {
      condition: isBuild && extraAppConfig,
      plugins: async () => [
        await viteExtraAppConfigPlugin({ isBuild: true, root: process.cwd() }),
      ],
    },
    {
      condition: archiver,
      plugins: async () => {
        return [await viteArchiverPlugin(archiverPluginOptions)];
      },
    },
  ]);
}

/**
 * 根据条件获取库类型的vite插件
 */
async function loadLibraryPlugins(
  options: LibraryPluginOptions,
): Promise<PluginOption[]> {
  // 单独取，否则commonOptions拿不到
  const isBuild = options.isBuild;
  const { dts, ...commonOptions } = options;
  const commonPlugins = await loadCommonPlugins(commonOptions);
  return await loadConditionPlugins([
    ...commonPlugins,
    {
      condition: isBuild && !!dts,
      plugins: () => [viteDtsPlugin({ logLevel: 'error' })],
    },
  ]);
}

export {
  loadApplicationPlugins,
  loadLibraryPlugins,
  viteArchiverPlugin,
  viteCompressPlugin,
  viteDtsPlugin,
  viteHtmlPlugin,
  viteVisualizerPlugin,
  viteVxeTableImportsPlugin,
};
