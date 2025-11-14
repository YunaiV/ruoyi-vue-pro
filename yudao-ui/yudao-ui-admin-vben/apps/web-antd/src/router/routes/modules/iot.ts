import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/iot',
    name: 'IoTCenter',
    meta: {
      title: 'IoT 物联网',
      icon: 'lucide:cpu',
      keepAlive: true,
      hideInMenu: true,
    },
    children: [
      {
        path: 'product/detail/:id',
        name: 'IoTProductDetail',
        meta: {
          title: '产品详情',
          activePath: '/iot/device/product',
        },
        component: () => import('#/views/iot/product/product/modules/detail/index.vue'),
      },
      {
        path: 'device/detail/:id',
        name: 'IoTDeviceDetail',
        meta: {
          title: '设备详情',
          activePath: '/iot/device/device',
        },
        component: () => import('#/views/iot/device/device/modules/detail/index.vue'),
      },
    ],
  },
];

export default routes;

