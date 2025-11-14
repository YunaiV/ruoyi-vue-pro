---
outline: deep
---

# Routes and Menus

::: info

This page is translated by machine translation and may not be very accurate.

:::

In the project, the framework provides a basic routing system and **automatically generates the corresponding menu structure based on the routing files**.

## Types of Routes

Routes are divided into core routes, static routes, and dynamic routes. Core routes are built-in routes of the framework, including root routes, login routes, 404 routes, etc.; static routes are routes that are determined when the project starts; dynamic routes are generally generated dynamically based on the user's permissions after the user logs in.

Both static and dynamic routes go through permission control, which can be controlled by configuring the `authority` field in the `meta` property of the route.

### Core Routes

Core routes are built-in routes of the framework, including root routes, login routes, 404 routes, etc. The configuration of core routes is in the `src/router/routes/core` directory under the application.

::: tip

Core routes are mainly used for the basic functions of the framework, so it is not recommended to put business-related routes in core routes. It is recommended to put business-related routes in static or dynamic routes.

:::

### Static Routes

The configuration of static routes is in the `src/router/routes/index` directory under the application. Open the commented file content:

::: tip

Permission control is controlled by the `authority` field in the `meta` property of the route. If your page project does not require permission control, you can omit the `authority` field.

:::

```ts
// Uncomment if needed and create the folder
// const externalRouteFiles = import.meta.glob('./external/**/*.ts', { eager: true }); // [!code --]
const staticRouteFiles = import.meta.glob('./static/**/*.ts', { eager: true }); // [!code ++]
/** Dynamic routes */
const dynamicRoutes: RouteRecordRaw[] = mergeRouteModules(dynamicRouteFiles);

/** External route list, these pages can be accessed without Layout, possibly used for embedding in other systems */
// const externalRoutes: RouteRecordRaw[] = mergeRouteModules(externalRouteFiles) // [!code --]
const externalRoutes: RouteRecordRaw[] = []; // [!code --]
const externalRoutes: RouteRecordRaw[] = mergeRouteModules(externalRouteFiles); // [!code ++]
```

### Dynamic Routes

The configuration of dynamic routes is in the `src/router/routes/modules` directory under the corresponding application. This directory contains all the route files. The content format of each file is consistent with the Vue Router route configuration format. Below is the configuration of secondary and multi-level routes.

## Route Definition

The configuration method of static routes and dynamic routes is the same. Below is the configuration of secondary and multi-level routes:

### Secondary Routes

::: details Secondary Route Example Code

```ts
import type { RouteRecordRaw } from 'vue-router';

import { VBEN_LOGO_URL } from '@vben/constants';

import { BasicLayout } from '#/layouts';
import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      badgeType: 'dot',
      badgeVariants: 'destructive',
      icon: VBEN_LOGO_URL,
      order: 9999,
      title: $t('page.vben.title'),
    },
    name: 'VbenProject',
    path: '/vben-admin',
    redirect: '/vben-admin/about',
    children: [
      {
        name: 'VbenAbout',
        path: '/vben-admin/about',
        component: () => import('#/views/_core/about/index.vue'),
        meta: {
          badgeType: 'dot',
          badgeVariants: 'destructive',
          icon: 'lucide:copyright',
          title: $t('page.vben.about'),
        },
      },
    ],
  },
];

export default routes;
```

:::

### Multi-level Routes

::: tip

- The parent route of multi-level routes does not need to set the `component` property, just set the `children` property. Unless you really need to display content nested under the parent route.
- In most cases, the `redirect` property of the parent route does not need to be specified, it will default to the first child route.

:::

::: details Multi-level Route Example Code

```ts
import type { RouteRecordRaw } from 'vue-router';

import { BasicLayout } from '#/layouts';
import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'ic:baseline-view-in-ar',
      keepAlive: true,
      order: 1000,
      title: $t('demos.title'),
    },
    name: 'Demos',
    path: '/demos',
    redirect: '/demos/access',
    children: [
      // Nested menu
      {
        meta: {
          icon: 'ic:round-menu',
          title: $t('demos.nested.title'),
        },
        name: 'NestedDemos',
        path: '/demos/nested',
        redirect: '/demos/nested/menu1',
        children: [
          {
            name: 'Menu1Demo',
            path: '/demos/nested/menu1',
            component: () => import('#/views/demos/nested/menu-1.vue'),
            meta: {
              icon: 'ic:round-menu',
              keepAlive: true,
              title: $t('demos.nested.menu1'),
            },
          },
          {
            name: 'Menu2Demo',
            path: '/demos/nested/menu2',
            meta: {
              icon: 'ic:round-menu',
              keepAlive: true,
              title: $t('demos.nested.menu2'),
            },
            redirect: '/demos/nested/menu2/menu2-1',
            children: [
              {
                name: 'Menu21Demo',
                path: '/demos/nested/menu2/menu2-1',
                component: () => import('#/views/demos/nested/menu-2-1.vue'),
                meta: {
                  icon: 'ic:round-menu',
                  keepAlive: true,
                  title: $t('demos.nested.menu2_1'),
                },
              },
            ],
          },
          {
            name: 'Menu3Demo',
            path: '/demos/nested/menu3',
            meta: {
              icon: 'ic:round-menu',
              title: $t('demos.nested.menu3'),
            },
            redirect: '/demos/nested/menu3/menu3-1',
            children: [
              {
                name: 'Menu31Demo',
                path: 'menu3-1',
                component: () => import('#/views/demos/nested/menu-3-1.vue'),
                meta: {
                  icon: 'ic:round-menu',
                  keepAlive: true,
                  title: $t('demos.nested.menu3_1'),
                },
              },
              {
                name: 'Menu32Demo',
                path: 'menu3-2',
                meta: {
                  icon: 'ic:round-menu',
                  title: $t('demos.nested.menu3_2'),
                },
                redirect: '/demos/nested/menu3/menu3-2/menu3-2-1',
                children: [
                  {
                    name: 'Menu321Demo',
                    path: '/demos/nested/menu3/menu3-2/menu3-2-1',
                    component: () =>
                      import('#/views/demos/nested/menu-3-2-1.vue'),
                    meta: {
                      icon: 'ic:round-menu',
                      keepAlive: true,
                      title: $t('demos.nested.menu3_2_1'),
                    },
                  },
                ],
              },
            ],
          },
        ],
      },
    ],
  },
];

export default routes;
```

:::

## Adding a New Page

To add a new page, you only need to add a route and the corresponding page component.

### Adding a Route

Add a route object in the corresponding route file, as follows:

```ts
import type { RouteRecordRaw } from 'vue-router';

import { VBEN_LOGO_URL } from '@vben/constants';

import { BasicLayout } from '#/layouts';
import { $t } from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'mdi:home',
      title: $t('page.home.title'),
    },
    name: 'Home',
    path: '/home',
    redirect: '/home/index',
    children: [
      {
        name: 'HomeIndex',
        path: '/home/index',
        component: () => import('#/views/home/index.vue'),
        meta: {
          icon: 'mdi:home',
          title: $t('page.home.index'),
        },
      },
    ],
  },
];

export default routes;
```

### Adding a Page Component

In `#/views/home/`, add a new `index.vue` file, as follows:

```vue
<template>
  <div>
    <h1>home page</h1>
  </div>
</template>
```

### Verification

At this point, the page has been added. Visit `http://localhost:5555/home/index` to see the corresponding page.

## Route Configuration

The route configuration items are mainly in the `meta` property of the route object. The following are common configuration items:

```ts {5-8}
const routes = [
  {
    name: 'HomeIndex',
    path: '/home/index',
    meta: {
      icon: 'mdi:home',
      title: $t('page.home.index'),
    },
  },
];
```

::: details Route Meta Configuration Type Definition

```ts
interface RouteMeta {
  /**
   * Active icon (menu)
   */
  activeIcon?: string;
  /**
   * The currently active menu, sometimes you don't want to activate the existing menu, use this to activate the parent menu
   */
  activePath?: string;
  /**
   * Whether to fix the tab
   * @default false
   */
  affixTab?: boolean;
  /**
   * The order of fixed tabs
   * @default 0
   */
  affixTabOrder?: number;
  /**
   * Specific roles required to access
   * @default []
   */
  authority?: string[];
  /**
   * Badge
   */
  badge?: string;
  /**
   * Badge type
   */
  badgeType?: 'dot' | 'normal';
  /**
   * Badge color
   */
  badgeVariants?:
    | 'default'
    | 'destructive'
    | 'primary'
    | 'success'
    | 'warning'
    | string;
  /**
   * The children of the current route are not displayed in the menu
   * @default false
   */
  hideChildrenInMenu?: boolean;
  /**
   * The current route is not displayed in the breadcrumb
   * @default false
   */
  hideInBreadcrumb?: boolean;
  /**
   * The current route is not displayed in the menu
   * @default false
   */
  hideInMenu?: boolean;
  /**
   * The current route is not displayed in the tab
   * @default false
   */
  hideInTab?: boolean;
  /**
   * Icon (menu/tab)
   */
  icon?: string;
  /**
   * iframe address
   */
  iframeSrc?: string;
  /**
   * Ignore permissions, can be accessed directly
   * @default false
   */
  ignoreAccess?: boolean;
  /**
   * Enable KeepAlive cache
   */
  keepAlive?: boolean;
  /**
   * External link - jump path
   */
  link?: string;
  /**
   * Whether the route has been loaded
   */
  loaded?: boolean;
  /**
   * Maximum number of open tabs
   * @default false
   */
  maxNumOfOpenTab?: number;
  /**
   * The menu can be seen, but access will be redirected to 403
   */
  menuVisibleWithForbidden?: boolean;
  /**
   * Open in a new window
   */
  openInNewWindow?: boolean;
  /**
   * Used for route -> menu sorting
   */
  order?: number;
  /**
   * Parameters carried by the menu
   */
  query?: Recordable;
  /**
   * Title name
   */
  title: string;
}
```

:::

### title

- Type: `string`
- Default: `''`

Used to configure the title of the page, which will be displayed in the menu and tab. Generally used with internationalization.

### icon

- Type: `string`
- Default: `''`

Used to configure the icon of the page, which will be displayed in the menu and tab. Generally used with an icon library, if it is an `http` link, the image will be loaded automatically.

### activeIcon

- Type: `string`
- Default: `''`

Used to configure the active icon of the page, which will be displayed in the menu. Generally used with an icon library, if it is an `http` link, the image will be loaded automatically.

### keepAlive

- Type: `boolean`
- Default: `false`

Used to configure whether the page cache is enabled. When enabled, the page will be cached and will not reload, only effective when the tab is enabled.

### hideInMenu

- Type: `boolean`
- Default: `false`

Used to configure whether the page is hidden in the menu. When hidden, the page will not be displayed in the menu.

### hideInTab

- Type: `boolean`
- Default: `false`

Used to configure whether the page is hidden in the tab. When hidden, the page will not be displayed in the tab.

### hideInBreadcrumb

- Type: `boolean`
- Default: `false`

Used to configure whether the page is hidden in the breadcrumb. When hidden, the page will not be displayed in the breadcrumb.

### hideChildrenInMenu

- Type: `boolean`
- Default: `false`

Used to configure whether the subpages of the page are hidden in the menu. When hidden, the subpages will not be displayed in the menu.

### authority

- Type: `string[]`
- Default: `[]`

Used to configure the permissions of the page. Only users with the corresponding permissions can access the page. If not configured, no permissions are required.

### badge

- Type: `string`
- Default: `''`

Used to configure the badge of the page, which will be displayed in the menu.

### badgeType

- Type: `'dot' | 'normal'`
- Default: `'normal'`

Used to configure the badge type of the page. `dot` is a small red dot, `normal` is text.

### badgeVariants

- Type: `'default' | 'destructive' | 'primary' | 'success' | 'warning' | string`
- Default: `'success'`

Used to configure the badge color of the page.

### activePath

- Type: `string`
- Default: `''`

Used to configure the currently active menu. Sometimes the page is not displayed in the menu, and this is used to activate the parent menu.

### affixTab

- Type: `boolean`
- Default: `false`

Used to configure whether the page is fixed in the tab. When fixed, the page cannot be closed.

### affixTabOrder

- Type: `number`
- Default: `0`

Used to configure the order of fixed tabs, sorted in ascending order.

### iframeSrc

- Type: `string`
- Default: `''`

Used to configure the `iframe` address of the embedded page. When set, the corresponding page will be embedded in the current page.

### ignoreAccess

- Type: `boolean`
- Default: `false`

Used to configure whether the page ignores permissions and can be accessed directly.

### link

- Type: `string`
- Default: `''`

Used to configure the external link jump path, which will open in a new window.

### maxNumOfOpenTab

- Type: `number`
- Default: `-1`

Used to configure the maximum number of open tabs. When set, the earliest opened tab will be automatically closed when opening a new tab (only effective when opening tabs with the same name).

### menuVisibleWithForbidden

- Type: `boolean`
- Default: `false`

Used to configure whether the page can be seen in the menu, but access will be redirected to 403.

### openInNewWindow

- Type: `boolean`
- Default: `false`

When set to `true`, the page will open in a new window.

### order

- Type: `number`
- Default: `0`

Used to configure the sorting of the page, used for route to menu sorting.

_Note:_ Sorting is only effective for first-level menus. The sorting of second-level menus needs to be set in the corresponding first-level menu in code order.

### query

- Type: `Recordable`
- Default: `{}`

Used to configure the menu parameters of the page, which will be passed to the page in the menu.

## Route Refresh

The route refresh method is as follows:

```vue
<script setup lang="ts">
import { useRefresh } from '@vben/hooks';

const { refresh } = useRefresh();

// Refresh the current route
refresh();
</script>
```
