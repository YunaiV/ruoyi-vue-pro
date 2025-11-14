---
outline: deep
---

# 路由和菜单

在项目中，框架提供了一套基础的路由系统，并**根据路由文件自动生成对应的菜单结构**。

## 路由类型

路由分为核心路由、静态路由和动态路由，核心路由是框架内置的路由，包含了根路由、登录路由、404路由等；静态路由是在项目启动时就已经确定的路由；动态路由一般是在用户登录后，根据用户的权限动态生成的路由。

静态路由和动态路由都会走权限控制，可以通过配置路由的 `meta` 属性中的 `authority` 字段来控制权限，可以参考[路由权限控制](https://github.com/vbenjs/vue-vben-admin/blob/main/playground/src/router/routes/modules/demos.ts)。

### 核心路由

核心路由是框架内置的路由，包含了根路由、登录路由、404路由等，核心路由的配置在应用下 `src/router/routes/core` 目录下

::: tip

核心路由主要用于框架的基础功能，因此不建议将业务相关的路由放在核心路由中，推荐将业务相关的路由放在静态路由或动态路由中。

:::

### 静态路由

静态路由的配置在应用下 `src/router/routes/index` 目录下，打开注释的文件内容:

::: tip

权限控制是通过路由的 `meta` 属性中的 `authority` 字段来控制的，如果你的页面项目不需要权限控制，可以不设置 `authority` 字段。

:::

```ts
// 有需要可以自行打开注释，并创建文件夹
// const externalRouteFiles = import.meta.glob('./external/**/*.ts', { eager: true }); // [!code --]
const staticRouteFiles = import.meta.glob('./static/**/*.ts', { eager: true }); // [!code ++]
/** 动态路由 */
const dynamicRoutes: RouteRecordRaw[] = mergeRouteModules(dynamicRouteFiles);

/** 外部路由列表，访问这些页面可以不需要Layout，可能用于内嵌在别的系统 */
// const externalRoutes: RouteRecordRaw[] = mergeRouteModules(externalRouteFiles) // [!code --]
const externalRoutes: RouteRecordRaw[] = []; // [!code --]
const externalRoutes: RouteRecordRaw[] = mergeRouteModules(externalRouteFiles); // [!code ++]
```

### 动态路由

动态路由的配置在对应应用 `src/router/routes/modules` 目录下，这个目录下存放了所有的路由文件。每个文件的内容格式如下，与 Vue Router 的路由配置格式一致，以下为二级路由和多级路由的配置。

## 路由定义

静态路由与动态路由的配置方式一致，以下为二级路由和多级路由的配置：

### 二级路由

::: details 二级路由示例代码

```ts
import type { RouteRecordRaw } from 'vue-router';

import { VBEN_LOGO_URL } from '@vben/constants';

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

### 多级路由

::: tip

- 如果没有特殊情况，父级路由的 `redirect` 属性，不需要指定，默认会指向第一个子路由。

:::

::: details 多级路由示例代码

```ts
import type { RouteRecordRaw } from 'vue-router';

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
      // 嵌套菜单
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

## 新增页面

新增一个页面，你只需要添加一个路由及对应的页面组件即可。

### 添加路由

在对应的路由文件中添加一个路由对象，如下：

```ts
import type { RouteRecordRaw } from 'vue-router';

import { VBEN_LOGO_URL } from '@vben/constants';

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

### 添加页面组件

在`#/views/home/`下，新增一个`index.vue`文件，如下：

```vue
<template>
  <div>
    <h1>home page</h1>
  </div>
</template>
```

### 验证

到这里页面已添加完成，访问 `http://localhost:5555/home/index` 出现对应的页面即可。

## 路由配置

路由配置项主要在对象路由的 `meta` 属性中，以下为常用的配置项：

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

::: details 路由Meta配置类型定义

```ts
interface RouteMeta {
  /**
   * 激活图标（菜单）
   */
  activeIcon?: string;
  /**
   * 当前激活的菜单，有时候不想激活现有菜单，需要激活父级菜单时使用
   */
  activePath?: string;
  /**
   * 是否固定标签页
   * @default false
   */
  affixTab?: boolean;
  /**
   * 固定标签页的顺序
   * @default 0
   */
  affixTabOrder?: number;
  /**
   * 需要特定的角色标识才可以访问
   * @default []
   */
  authority?: string[];
  /**
   * 徽标
   */
  badge?: string;
  /**
   * 徽标类型
   */
  badgeType?: 'dot' | 'normal';
  /**
   * 徽标颜色
   */
  badgeVariants?:
    | 'default'
    | 'destructive'
    | 'primary'
    | 'success'
    | 'warning'
    | string;
  /**
   * 路由的完整路径作为key（默认true）
   */
  fullPathKey?: boolean;
  /**
   * 当前路由的子级在菜单中不展现
   * @default false
   */
  hideChildrenInMenu?: boolean;
  /**
   * 当前路由在面包屑中不展现
   * @default false
   */
  hideInBreadcrumb?: boolean;
  /**
   * 当前路由在菜单中不展现
   * @default false
   */
  hideInMenu?: boolean;
  /**
   * 当前路由在标签页不展现
   * @default false
   */
  hideInTab?: boolean;
  /**
   * 图标（菜单/tab）
   */
  icon?: string;
  /**
   * iframe 地址
   */
  iframeSrc?: string;
  /**
   * 忽略权限，直接可以访问
   * @default false
   */
  ignoreAccess?: boolean;
  /**
   * 开启KeepAlive缓存
   */
  keepAlive?: boolean;
  /**
   * 外链-跳转路径
   */
  link?: string;
  /**
   * 路由是否已经加载过
   */
  loaded?: boolean;
  /**
   * 标签页最大打开数量
   * @default false
   */
  maxNumOfOpenTab?: number;
  /**
   * 菜单可以看到，但是访问会被重定向到403
   */
  menuVisibleWithForbidden?: boolean;
  /**
   * 当前路由不使用基础布局（仅在顶级生效）
   */
  noBasicLayout?: boolean;
  /**
   * 在新窗口打开
   */
  openInNewWindow?: boolean;
  /**
   * 用于路由->菜单排序
   */
  order?: number;
  /**
   * 菜单所携带的参数
   */
  query?: Recordable;
  /**
   * 标题名称
   */
  title: string;
}
```

:::

### title

- 类型：`string`
- 默认值：`''`

用于配置页面的标题，会在菜单和标签页中显示。一般会配合国际化使用。

### icon

- 类型：`string`
- 默认值：`''`

用于配置页面的图标，会在菜单和标签页中显示。一般会配合图标库使用，如果是`http`链接，会自动加载图片。

### activeIcon

- 类型：`string`
- 默认值：`''`

用于配置页面的激活图标，会在菜单中显示。一般会配合图标库使用，如果是`http`链接，会自动加载图片。

### keepAlive

- 类型：`boolean`
- 默认值：`false`

用于配置页面是否开启缓存，开启后页面会缓存，不会重新加载，仅在标签页启用时有效。

### hideInMenu

- 类型：`boolean`
- 默认值：`false`

用于配置页面是否在菜单中隐藏，隐藏后页面不会在菜单中显示。

### hideInTab

- 类型：`boolean`
- 默认值：`false`

用于配置页面是否在标签页中隐藏，隐藏后页面不会在标签页中显示。

### hideInBreadcrumb

- 类型：`boolean`
- 默认值：`false`

用于配置页面是否在面包屑中隐藏，隐藏后页面不会在面包屑中显示。

### hideChildrenInMenu

- 类型：`boolean`
- 默认值：`false`

用于配置页面的子页面是否在菜单中隐藏，隐藏后子页面不会在菜单中显示。

### authority

- 类型：`string[]`
- 默认值：`[]`

用于配置页面的权限，只有拥有对应权限的用户才能访问页面，不配置则不需要权限。

### badge

- 类型：`string`
- 默认值：`''`

用于配置页面的徽标，会在菜单显示。

### badgeType

- 类型：`'dot' | 'normal'`
- 默认值：`'normal'`

用于配置页面的徽标类型，`dot` 为小红点，`normal` 为文本。

### badgeVariants

- 类型：`'default' | 'destructive' | 'primary' | 'success' | 'warning' | string`
- 默认值：`'success'`

用于配置页面的徽标颜色。

### fullPathKey

- 类型：`boolean`
- 默认值：`true`

是否将路由的完整路径作为tab key（默认true）

### activePath

- 类型：`string`
- 默认值：`''`

用于配置当前激活的菜单，有时候页面没有显示在菜单内，需要激活父级菜单时使用。

### affixTab

- 类型：`boolean`
- 默认值：`false`

用于配置页面是否固定标签页，固定后页面不可关闭。

### affixTabOrder

- 类型：`number`
- 默认值：`0`

用于配置页面固定标签页的排序, 采用升序排序。

### iframeSrc

- 类型：`string`
- 默认值：`''`

用于配置内嵌页面的 `iframe` 地址，设置后会在当前页面内嵌对应的页面。

### ignoreAccess

- 类型：`boolean`
- 默认值：`false`

用于配置页面是否忽略权限，直接可以访问。

### link

- 类型：`string`
- 默认值：`''`

用于配置外链跳转路径，会在新窗口打开。

### maxNumOfOpenTab

- 类型：`number`
- 默认值：`-1`

用于配置标签页最大打开数量，设置后会在打开新标签页时自动关闭最早打开的标签页(仅在打开同名标签页时生效)。

### menuVisibleWithForbidden

- 类型：`boolean`
- 默认值：`false`

用于配置页面在菜单可以看到，但是访问会被重定向到403。

### openInNewWindow

- 类型：`boolean`
- 默认值：`false`

设置为 `true` 时，会在新窗口打开页面。

### order

- 类型：`number`
- 默认值：`0`

用于配置页面的排序，用于路由到菜单排序。

_注意:_ 排序仅针对一级菜单有效，二级菜单的排序需要在对应的一级菜单中按代码顺序设置。

### query

- 类型：`Recordable`
- 默认值：`{}`

用于配置页面的菜单参数，会在菜单中传递给页面。

### noBasicLayout

- 类型：`boolean`
- 默认值：`false`

用于配置当前路由不使用基础布局，仅在顶级时生效。默认情况下，所有的路由都会被包裹在基础布局中（包含顶部以及侧边等导航部件），如果你的页面不需要这些部件，可以设置 `noBasicLayout` 为 `true`。

## 路由刷新

路由刷新方式如下：

```vue
<script setup lang="ts">
import { useRefresh } from '@vben/hooks';

const { refresh } = useRefresh();

// 刷新当前路由
refresh();
</script>
```

## 标签页与路由控制

在某些场景下，需要单个路由打开多个标签页，或者修改路由的query不打开新的标签页

每个标签页Tab使用唯一的key标识，设置Tab key有三种方式，优先级由高到低：

- 使用路由query参数pageKey

```vue
<script setup lang="ts">
import { useRouter } from 'vue-router';
// 跳转路由
const router = useRouter();
router.push({
  path: 'path',
  query: {
    pageKey: 'key',
  },
});
```

- 路由的完整路径作为key

`meta` 属性中的 `fullPathKey`不为false，则使用路由`fullPath`作为key

- 路由的path作为key

`meta` 属性中的 `fullPathKey`为false，则使用路由`path`作为key
