import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/mall/product',
    name: 'ProductCenter',
    meta: {
      title: '商品中心',
      icon: 'lucide:shopping-bag',
      keepAlive: true,
      hideInMenu: true,
    },
    children: [
      {
        path: 'spu/add',
        name: 'ProductSpuAdd',
        meta: {
          title: '商品添加',
          activePath: '/mall/product/spu',
        },
        component: () => import('#/views/mall/product/spu/form/index.vue'),
      },
      {
        path: String.raw`spu/edit/:id(\d+)`,
        name: 'ProductSpuEdit',
        meta: {
          title: '商品编辑',
          activePath: '/mall/product/spu',
        },
        component: () => import('#/views/mall/product/spu/form/index.vue'),
      },
      {
        path: String.raw`spu/detail/:id(\d+)`,
        name: 'ProductSpuDetail',
        meta: {
          title: '商品详情',
          activePath: '/mall/product/spu',
        },
        component: () => import('#/views/mall/product/spu/form/index.vue'),
      },
    ],
  },
  {
    path: '/mall/trade',
    name: 'TradeCenter',
    meta: {
      title: '交易中心',
      icon: 'lucide:shopping-cart',
      keepAlive: true,
      hideInMenu: true,
    },
    children: [
      {
        path: String.raw`order/detail/:id(\d+)`,
        name: 'TradeOrderDetail',
        meta: {
          title: '订单详情',
          activePath: '/mall/trade/order',
        },
        component: () => import('#/views/mall/trade/order/detail/index.vue'),
      },
      {
        path: String.raw`after-sale/detail/:id(\d+)`,
        name: 'TradeAfterSaleDetail',
        meta: {
          title: '退款详情',
          activePath: '/mall/trade/after-sale',
        },
        component: () =>
          import('#/views/mall/trade/afterSale/detail/index.vue'),
      },
    ],
  },
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
          import('#/views/mall/promotion/diy/template/decorate/index.vue'),
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
          import('#/views/mall/promotion/diy/page/decorate/index.vue'),
      },
    ],
  },
];

export default routes;
