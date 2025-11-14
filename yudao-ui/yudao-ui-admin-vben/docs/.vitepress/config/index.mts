import { withPwa } from '@vite-pwa/vitepress';
import { defineConfigWithTheme } from 'vitepress';

import { en } from './en.mts';
import { shared } from './shared.mts';
import { zh } from './zh.mts';

export default withPwa(
  defineConfigWithTheme({
    ...shared,
    locales: {
      en: {
        label: 'English',
        lang: 'en',
        link: '/en/',
        ...en,
      },
      root: {
        label: '简体中文',
        lang: 'zh-CN',
        ...zh,
      },
    },
  }),
);
