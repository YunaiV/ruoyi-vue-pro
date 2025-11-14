import type { RouteRecordRaw } from 'vue-router';

import {
  VBEN_ANT_PREVIEW_URL,
  VBEN_DOC_URL,
  VBEN_ELE_PREVIEW_URL,
  VBEN_GITHUB_URL,
  VBEN_LOGO_URL,
  VBEN_NAIVE_PREVIEW_URL,
} from '@vben/constants';
import { SvgAntdvLogoIcon } from '@vben/icons';

import { IFrameView } from '#/layouts';
import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      badgeType: 'dot',
      icon: VBEN_LOGO_URL,
      order: 9998,
      title: $t('demos.vben.title'),
    },
    name: 'VbenProject',
    path: '/vben-admin',
    children: [
      {
        name: 'VbenDocument',
        path: '/vben-admin/document',
        component: IFrameView,
        meta: {
          icon: 'lucide:book-open-text',
          link: VBEN_DOC_URL,
          title: $t('demos.vben.document'),
        },
      },
      {
        name: 'VbenGithub',
        path: '/vben-admin/github',
        component: IFrameView,
        meta: {
          icon: 'mdi:github',
          link: VBEN_GITHUB_URL,
          title: 'Github',
        },
      },
      {
        name: 'VbenAntdv',
        path: '/vben-admin/antdv',
        component: IFrameView,
        meta: {
          badgeType: 'dot',
          icon: SvgAntdvLogoIcon,
          link: VBEN_ANT_PREVIEW_URL,
          title: $t('demos.vben.antdv'),
        },
      },
      {
        name: 'VbenNaive',
        path: '/vben-admin/naive',
        component: IFrameView,
        meta: {
          badgeType: 'dot',
          icon: 'logos:naiveui',
          link: VBEN_NAIVE_PREVIEW_URL,
          title: $t('demos.vben.naive-ui'),
        },
      },
      {
        name: 'VbenElementPlus',
        path: '/vben-admin/ele',
        component: IFrameView,
        meta: {
          badgeType: 'dot',
          icon: 'logos:element',
          link: VBEN_ELE_PREVIEW_URL,
          title: $t('demos.vben.element-plus'),
        },
      },
    ],
  },
  {
    component: () => import('#/views/_core/about/index.vue'),
    meta: {
      icon: 'lucide:copyright',
      order: 9999,
      title: $t('demos.vben.about'),
    },
    name: 'VbenAbout',
    path: '/vben-admin/about',
  },
];

export default routes;
