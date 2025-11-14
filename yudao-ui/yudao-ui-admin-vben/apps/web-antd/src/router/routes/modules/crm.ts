import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/crm',
    name: 'CrmCenter',
    meta: {
      title: '客户管理',
      icon: 'simple-icons:civicrm',
      keepAlive: true,
      hideInMenu: true,
    },
    children: [
      {
        path: 'clue/detail/:id',
        name: 'CrmClueDetail',
        meta: {
          title: '线索详情',
          activePath: '/crm/clue',
        },
        component: () => import('#/views/crm/clue/detail/index.vue'),
      },
      {
        path: 'customer/detail/:id',
        name: 'CrmCustomerDetail',
        meta: {
          title: '客户详情',
          activePath: '/crm/customer',
        },
        component: () => import('#/views/crm/customer/detail/index.vue'),
      },
      {
        path: 'business/detail/:id',
        name: 'CrmBusinessDetail',
        meta: {
          title: '商机详情',
          activePath: '/crm/business',
        },
        component: () => import('#/views/crm/business/detail/index.vue'),
      },
      {
        path: 'contract/detail/:id',
        name: 'CrmContractDetail',
        meta: {
          title: '合同详情',
          activePath: '/crm/contract',
        },
        component: () => import('#/views/crm/contract/detail/index.vue'),
      },
      {
        path: 'receivable-plan/detail/:id',
        name: 'CrmReceivablePlanDetail',
        meta: {
          title: '回款计划详情',
          activePath: '/crm/receivable-plan',
        },
        component: () => import('#/views/crm/receivable/plan/detail/index.vue'),
      },
      {
        path: 'receivable/detail/:id',
        name: 'CrmReceivableDetail',
        meta: {
          title: '回款详情',
          activePath: '/crm/receivable',
        },
        component: () => import('#/views/crm/receivable/detail/index.vue'),
      },
      {
        path: 'contact/detail/:id',
        name: 'CrmContactDetail',
        meta: {
          title: '联系人详情',
          activePath: '/crm/contact',
        },
        component: () => import('#/views/crm/contact/detail/index.vue'),
      },
      {
        path: 'product/detail/:id',
        name: 'CrmProductDetail',
        meta: {
          title: '产品详情',
          activePath: '/crm/product',
        },
        component: () => import('#/views/crm/product/detail/index.vue'),
      },
    ],
  },
];

export default routes;
