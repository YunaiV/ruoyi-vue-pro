import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    path: '/ai',
    name: 'Ai',
    meta: {
      title: 'Ai',
      hideInMenu: true,
    },
    children: [
      {
        path: 'image/square',
        component: () => import('#/views/ai/image/square/index.vue'),
        name: 'AiImageSquare',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '绘图作品',
          activePath: '/ai/image',
        },
      },
      {
        path: 'knowledge/document',
        component: () => import('#/views/ai/knowledge/document/index.vue'),
        name: 'AiKnowledgeDocument',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '知识库文档',
          activePath: '/ai/knowledge',
        },
      },
      {
        path: 'knowledge/document/create',
        component: () => import('#/views/ai/knowledge/document/form/index.vue'),
        name: 'AiKnowledgeDocumentCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '创建文档',
          activePath: '/ai/knowledge',
        },
      },
      {
        path: 'knowledge/document/update',
        component: () => import('#/views/ai/knowledge/document/form/index.vue'),
        name: 'AiKnowledgeDocumentUpdate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '修改文档',
          activePath: '/ai/knowledge',
        },
      },
      {
        path: 'knowledge/retrieval',
        component: () =>
          import('#/views/ai/knowledge/knowledge/retrieval/index.vue'),
        name: 'AiKnowledgeRetrieval',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '文档召回测试',
          activePath: '/ai/knowledge',
        },
      },
      {
        path: 'knowledge/segment',
        component: () => import('#/views/ai/knowledge/segment/index.vue'),
        name: 'AiKnowledgeSegment',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '知识库分段',
          activePath: '/ai/knowledge',
        },
      },
      {
        path: 'console/workflow/create',
        component: () => import('#/views/ai/workflow/form/index.vue'),
        name: 'AiWorkflowCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计 AI 工作流',
          activePath: '/ai/workflow',
        },
      },
      {
        path: 'console/workflow/:type/:id',
        component: () => import('#/views/ai/workflow/form/index.vue'),
        name: 'AiWorkflowUpdate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计 AI 工作流',
          activePath: '/ai/workflow',
        },
      },
    ],
  },
];

export default routes;
