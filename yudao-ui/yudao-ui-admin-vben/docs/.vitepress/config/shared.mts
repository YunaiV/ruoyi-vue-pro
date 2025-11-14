import type { PwaOptions } from '@vite-pwa/vitepress';
import type { HeadConfig } from 'vitepress';

import { resolve } from 'node:path';

import {
  viteArchiverPlugin,
  viteVxeTableImportsPlugin,
} from '@vben/vite-config';

import {
  GitChangelog,
  GitChangelogMarkdownSection,
} from '@nolebase/vitepress-plugin-git-changelog/vite';
import tailwind from 'tailwindcss';
import { defineConfig, postcssIsolateStyles } from 'vitepress';
import {
  groupIconMdPlugin,
  groupIconVitePlugin,
} from 'vitepress-plugin-group-icons';

import { demoPreviewPlugin } from './plugins/demo-preview';
import { search as zhSearch } from './zh.mts';

export const shared = defineConfig({
  appearance: 'dark',
  head: head(),
  markdown: {
    preConfig(md) {
      md.use(demoPreviewPlugin);
      md.use(groupIconMdPlugin);
    },
  },
  pwa: pwa(),
  srcDir: 'src',
  themeConfig: {
    i18nRouting: true,
    logo: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/logo-v1.webp',
    search: {
      options: {
        locales: {
          ...zhSearch,
        },
      },
      provider: 'local',
    },
    siteTitle: 'Vben Admin',
    socialLinks: [
      { icon: 'github', link: 'https://github.com/vbenjs/vue-vben-admin' },
    ],
  },
  title: 'Vben Admin',
  vite: {
    build: {
      chunkSizeWarningLimit: Infinity,
      minify: 'terser',
    },
    css: {
      postcss: {
        plugins: [
          tailwind(),
          postcssIsolateStyles({ includeFiles: [/vp-doc\.css/] }),
        ],
      },
      preprocessorOptions: {
        scss: {
          api: 'modern',
        },
      },
    },
    json: {
      stringify: true,
    },
    plugins: [
      GitChangelog({
        mapAuthors: [
          {
            mapByNameAliases: ['Vben'],
            name: 'vben',
            username: 'anncwb',
          },
          {
            name: 'vince',
            username: 'vince292007',
          },
          {
            name: 'Li Kui',
            username: 'likui628',
          },
        ],
        repoURL: () => 'https://github.com/vbenjs/vue-vben-admin',
      }),
      GitChangelogMarkdownSection(),
      viteArchiverPlugin({ outputDir: '.vitepress' }),
      groupIconVitePlugin(),
      await viteVxeTableImportsPlugin(),
    ],
    server: {
      fs: {
        allow: ['../..'],
      },
      host: true,
      port: 6173,
    },

    ssr: {
      external: ['@vue/repl'],
    },
  },
});

function head(): HeadConfig[] {
  return [
    ['meta', { content: 'Vbenjs Team', name: 'author' }],
    [
      'meta',
      {
        content: 'vben, vitejs, vite, shacdn-ui, vue',
        name: 'keywords',
      },
    ],
    ['link', { href: '/favicon.ico', rel: 'icon', type: 'image/svg+xml' }],
    [
      'meta',
      {
        content:
          'width=device-width,initial-scale=1,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no',
        name: 'viewport',
      },
    ],
    ['meta', { content: 'vben admin docs', name: 'keywords' }],
    ['link', { href: '/favicon.ico', rel: 'icon' }],
    // [
    //   'script',
    //   {
    //     src: 'https://cdn.tailwindcss.com',
    //   },
    // ],
  ];
}

function pwa(): PwaOptions {
  return {
    includeManifestIcons: false,
    manifest: {
      description:
        'Vben Admin is a modern admin dashboard template based on Vue 3. ',
      icons: [
        {
          sizes: '192x192',
          src: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/pwa-icon-192.png',
          type: 'image/png',
        },
        {
          sizes: '512x512',
          src: 'https://unpkg.com/@vbenjs/static-source@0.1.7/source/pwa-icon-512.png',
          type: 'image/png',
        },
      ],
      id: '/',
      name: 'Vben Admin Doc',
      short_name: 'vben_admin_doc',
      theme_color: '#ffffff',
    },
    outDir: resolve(process.cwd(), '.vitepress/dist'),
    registerType: 'autoUpdate',
    workbox: {
      globPatterns: ['**/*.{css,js,html,svg,png,ico,txt,woff2}'],
      maximumFileSizeToCacheInBytes: 5 * 1024 * 1024,
    },
  };
}
