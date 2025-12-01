import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/bpm',
    name: 'bpm',
    meta: {
      title: '工作流',
      hideInMenu: true,
    },
    children: [
      {
        path: 'task',
        name: 'BpmTask',
        meta: {
          title: '审批中心',
          icon: 'ant-design:history-outlined',
        },
        children: [
          {
            path: 'my',
            name: 'BpmTaskMy',
            component: () => import('#/views/bpm/processInstance/index.vue'),
            meta: {
              title: '我的流程',
            },
          },
        ],
      },
      {
        path: 'process-instance/detail',
        component: () => import('#/views/bpm/processInstance/detail/index.vue'),
        name: 'BpmProcessInstanceDetail',
        meta: {
          title: '流程详情',
          activePath: '/bpm/task/my',
          icon: 'ant-design:history-outlined',
          keepAlive: false,
          hideInMenu: true,
        },
        props: (route) => {
          return {
            id: route.query.id,
            taskId: route.query.taskId,
            activityId: route.query.activityId,
          };
        },
      },
      {
        path: '/bpm/manager/form/edit',
        name: 'BpmFormEditor',
        component: () => import('#/views/bpm/form/designer/index.vue'),
        meta: {
          title: '设计流程表单',
          activePath: '/bpm/manager/form',
        },
        props: (route) => {
          return {
            id: route.query.id,
            type: route.query.type,
            copyId: route.query.copyId,
          };
        },
      },
      {
        path: 'manager/model/create',
        component: () => import('#/views/bpm/model/form/index.vue'),
        name: 'BpmModelCreate',
        meta: {
          title: '创建流程',
          activePath: '/bpm/manager/model',
          icon: 'carbon:flow-connection',
          hideInMenu: true,
          keepAlive: true,
        },
      },
      {
        path: 'manager/model/:type/:id',
        component: () => import('#/views/bpm/model/form/index.vue'),
        name: 'BpmModelUpdate',
        meta: {
          title: '修改流程',
          activePath: '/bpm/manager/model',
          icon: 'carbon:flow-connection',
          hideInMenu: true,
          keepAlive: true,
        },
      },
      {
        path: 'manager/definition',
        component: () => import('#/views/bpm/model/definition/index.vue'),
        name: 'BpmProcessDefinition',
        meta: {
          title: '流程定义',
          activePath: '/bpm/manager/model',
          icon: 'carbon:flow-modeler',
          hideInMenu: true,
          keepAlive: true,
        },
      },
      {
        path: 'process-instance/report',
        component: () => import('#/views/bpm/processInstance/report/index.vue'),
        name: 'BpmProcessInstanceReport',
        meta: {
          title: '数据报表',
          activePath: '/bpm/manager/model',
          icon: 'carbon:data-2',
          hideInMenu: true,
          keepAlive: true,
        },
      },
    ],
  },
];

export default routes;
