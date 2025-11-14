import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/pay/cashier',
    component: () => import('#/views/pay/cashier/index.vue'),
    name: 'PayCashier',
    meta: {
      title: '收银台',
      icon: 'lucide:badge-japanese-yen',
      hideInMenu: true,
    },
  },
];

export default routes;
