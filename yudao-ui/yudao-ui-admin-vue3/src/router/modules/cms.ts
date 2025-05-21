import { getAccessToken } from '@/utils/auth' // Or your project's auth utils
import { CACHE_KEY, useCache } from '@/hooks/web/useCache' // Or your project's cache utils
import { t } from '@/hooks/web/useI18n'
// import { createUserRouter, createAdminRouter } from '@/utils/routerHelper' // Assuming this might not be used for static module routes
import type { AppRouteRecordRaw } from '@/router/types' // Correct type for routes

// Assuming Layout is the standard layout component
import Layout from '@/layout/Layout.vue' 

const cmsRouter: AppRouteRecordRaw[] = [
  {
    path: '/cms',
    component: Layout,
    redirect: '/cms/category',
    name: 'Cms',
    meta: {
      title: t('router.cmsManagement'), // Needs i18n key: '内容管理'
      icon: 'ep:document-copy', // Example icon for CMS module
      alwaysShow: true // Ensures the parent menu item is always visible
    },
    children: [
      {
        path: 'category',
        component: () => import('@/views/cms/category/index.vue'),
        name: 'CmsCategory',
        meta: {
          title: t('router.cmsCategory'), // Needs i18n key: '分类管理'
          noCache: true, // If the page should not be cached
          permissions: ['cms:category:query'] // Permission for accessing this route
        }
      }
      // Future CMS children routes (like 'article', 'tag', 'comment') can be added here
      // Example for article:
      // {
      //   path: 'article',
      //   component: () => import('@/views/cms/article/index.vue'), // Assuming you will create this
      //   name: 'CmsArticle',
      //   meta: {
      //     title: t('router.cmsArticle'), // Needs i18n key: '文章管理'
      //     noCache: true,
      //     permissions: ['cms:article:query'] 
      //   }
      // }
    ]
  }
]
export default cmsRouter
