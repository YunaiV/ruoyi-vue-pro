import type { RouteRecordRaw } from 'vue-router';

// TODO @chihuo：这个合并到 mall.ts 里
const routes: RouteRecordRaw[] = [
  {
    path: '/diy',
    name: 'DiyCenter',
    meta: {
      title: '营销中心',
      icon: 'lucide:shopping-bag',
      keepAlive: true,
      hideInMenu: true,
    },
    children: [
      {
        path: String.raw`template/decorate/:id(\d+)`,
        name: 'DiyTemplateDecorate',
        meta: {
          title: '模板装修',
          activePath: '/mall/promotion/diy-template/diy-template',
        },
        component: () =>
          import('#/views/mall/promotion/diy/template/modules/decorate.vue'),
      },
      {
        path: 'page/decorate/:id',
        name: 'DiyPageDecorate',
        meta: {
          title: '页面装修',
          noCache: false,
          hidden: true,
          activePath: '/mall/promotion/diy-template/diy-page',
        },
        component: () =>
          import('#/views/mall/promotion/diy/page/modules/decorate.vue'),
      },
    ],
  },
];

export default routes;
