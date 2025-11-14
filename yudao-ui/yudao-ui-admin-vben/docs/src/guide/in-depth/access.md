---
outline: deep
---

# 权限

框架内置了三种权限控制方式：

- 通过用户角色来判断菜单或者按钮是否可以访问
- 通过接口来判断菜单或者按钮是否可以访问
- 混合模式：同时使用前端和后端权限控制

## 前端访问控制

**实现原理**: 在前端固定写死路由的权限，指定路由有哪些权限可以查看。只初始化通用的路由，需要权限才能访问的路由没有被加入路由表内。在登录后或者其他方式获取用户角色后，通过角色去遍历路由表，获取该角色可以访问的路由表，生成路由表，再通过 `router.addRoute` 添加到路由实例，实现权限的过滤。

**缺点**: 权限相对不自由，如果后台改动角色，前台也需要跟着改动。适合角色较固定的系统

### 步骤

- 确保当前模式为前端访问控制模式

调整对应应用目录下的`preferences.ts`，确保`accessMode='frontend'`。

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    // 默认值，可不填
    accessMode: 'frontend',
  },
});
```

- 配置路由权限

**如果不配置，默认可见**

```ts {3}
 {
    meta: {
      authority: ['super'],
    },
},
```

- 确保接口返回的角色和路由表的权限匹配

可查看应用下的 `src/store/auth`,找到下面代码，

```ts
// 设置登录用户信息，需要确保 userInfo.roles 是一个数组，且包含路由表中的权限
// 例如：userInfo.roles=['super', 'admin']
authStore.setUserInfo(userInfo);
```

到这里，就已经配置完成，你需要确保登录后，接口返回的角色和路由表的权限匹配，否则无法访问。

### 菜单可见，但禁止访问

有时候，我们需要菜单可见，但是禁止访问，可以通过下面的方式实现，设置 `menuVisibleWithForbidden` 为 `true`，此时菜单可见，但是禁止访问，会跳转403页面。

```ts
{
    meta: {
      menuVisibleWithForbidden: true,
    },
},
```

## 后端访问控制

**实现原理**: 是通过接口动态生成路由表，且遵循一定的数据结构返回。前端根据需要处理该数据为可识别的结构，再通过 `router.addRoute` 添加到路由实例，实现权限的动态生成。

**缺点**: 后端需要提供符合规范的数据结构，前端需要处理数据结构，适合权限较为复杂的系统。

### 步骤

- 确保当前模式为后端访问控制模式

调整对应应用目录下的`preferences.ts`，确保`accessMode='backend'`。

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    accessMode: 'backend',
  },
});
```

- 确保接口返回的菜单数据结构正确

可查看应用下的 `src/router/access.ts`,找到下面代码，

```ts {5}
async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  return await generateAccessible(preferences.app.accessMode, {
    fetchMenuListAsync: async () => {
      // 这个接口为后端返回的菜单数据
      return await getAllMenus();
    },
  });
}
```

- 接口返回菜单数据，可看注释说明

::: details 接口返回菜单数据示例

```ts
const dashboardMenus = [
  {
    meta: {
      order: -1,
      title: 'page.dashboard.title',
    },
    name: 'Dashboard',
    path: '/',
    redirect: '/analytics',
    children: [
      {
        name: 'Analytics',
        path: '/analytics',
        // 这里为页面的路径，需要去掉 views/ 和 .vue
        component: '/dashboard/analytics/index',
        meta: {
          affixTab: true,
          title: 'page.dashboard.analytics',
        },
      },
      {
        name: 'Workspace',
        path: '/workspace',
        component: '/dashboard/workspace/index',
        meta: {
          title: 'page.dashboard.workspace',
        },
      },
    ],
  },
  {
    name: 'Test',
    path: '/test',
    component: '/test/index',
    meta: {
      title: 'page.test',
      // 部分特殊页面如果不需要基础布局（页面顶部和侧边栏），可将noBasicLayout设置为true
      noBasicLayout: true,
    },
  },
];
```

:::

到这里，就已经配置完成，你需要确保登录后，接口返回的菜单格式正确，否则无法访问。

## 混合访问控制

**实现原理**: 混合模式同时结合了前端访问控制和后端访问控制两种方式。系统会并行处理前端固定路由权限和后端动态菜单数据，最终将两部分路由合并，提供更灵活的权限控制方案。

**优点**: 兼具前端控制的性能优势和后端控制的灵活性，适合复杂业务场景下的权限管理。

### 步骤

- 确保当前模式为混合访问控制模式

调整对应应用目录下的`preferences.ts`，确保`accessMode='mixed'`。

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    accessMode: 'mixed',
  },
});
```

- 配置前端路由权限

同[前端访问控制](#前端访问控制)模式的路由权限配置方式。

- 配置后端菜单接口

同[后端访问控制](#后端访问控制)模式的接口配置方式。

- 确保角色和权限匹配

需要同时满足前端路由权限配置和后端菜单数据返回的要求，确保用户角色与两种模式的权限配置都匹配。

到这里，就已经配置完成，混合模式会自动合并前端和后端的路由，提供完整的权限控制功能。

## 按钮细粒度控制

在某些情况下，我们需要对按钮进行细粒度的控制，我们可以借助接口或者角色来控制按钮的显示。

### 权限码

权限码为接口返回的权限码，通过权限码来判断按钮是否显示，逻辑在`src/store/auth`下：

```ts
const [fetchUserInfoResult, accessCodes] = await Promise.all([
  fetchUserInfo(),
  getAccessCodes(),
]);

userInfo = fetchUserInfoResult;
authStore.setUserInfo(userInfo);
accessStore.setAccessCodes(accessCodes);
```

找到 `getAccessCodes` 对应的接口，可根据业务逻辑进行调整。

权限码返回的数据结构为字符串数组，例如：`['AC_100100', 'AC_100110', 'AC_100120', 'AC_100010']`

有了权限码，就可以使用 `@vben/access` 提供的`AccessControl`组件及API来进行按钮的显示与隐藏。

#### 组件方式

```vue
<script lang="ts" setup>
import { AccessControl, useAccess } from '@vben/access';

const { accessMode, hasAccessByCodes } = useAccess();
</script>

<template>
  <!-- 需要指明 type="code" -->
  <AccessControl :codes="['AC_100100']" type="code">
    <Button> Super 账号可见 ["AC_1000001"] </Button>
  </AccessControl>
  <AccessControl :codes="['AC_100030']" type="code">
    <Button> Admin 账号可见 ["AC_100010"] </Button>
  </AccessControl>
  <AccessControl :codes="['AC_1000001']" type="code">
    <Button> User 账号可见 ["AC_1000001"] </Button>
  </AccessControl>
  <AccessControl :codes="['AC_100100', 'AC_100010']" type="code">
    <Button> Super & Admin 账号可见 ["AC_100100","AC_1000001"] </Button>
  </AccessControl>
</template>
```

#### API方式

```vue
<script lang="ts" setup>
import { AccessControl, useAccess } from '@vben/access';

const { hasAccessByCodes } = useAccess();
</script>

<template>
  <Button v-if="hasAccessByCodes(['AC_100100'])">
    Super 账号可见 ["AC_1000001"]
  </Button>
  <Button v-if="hasAccessByCodes(['AC_100030'])">
    Admin 账号可见 ["AC_100010"]
  </Button>
  <Button v-if="hasAccessByCodes(['AC_1000001'])">
    User 账号可见 ["AC_1000001"]
  </Button>
  <Button v-if="hasAccessByCodes(['AC_100100', 'AC_1000001'])">
    Super & Admin 账号可见 ["AC_100100","AC_1000001"]
  </Button>
</template>
```

#### 指令方式

> 指令支持绑定单个或多个权限码。单个时可以直接传入字符串或数组中包含一个权限码，多个权限码则传入数组。

```vue
<template>
  <Button class="mr-4" v-access:code="'AC_100100'">
    Super 账号可见 'AC_100100'
  </Button>
  <Button class="mr-4" v-access:code="['AC_100030']">
    Admin 账号可见 ["AC_100010"]
  </Button>
  <Button class="mr-4" v-access:code="['AC_1000001']">
    User 账号可见 ["AC_1000001"]
  </Button>
  <Button class="mr-4" v-access:code="['AC_100100', 'AC_1000001']">
    Super & Admin 账号可见 ["AC_100100","AC_1000001"]
  </Button>
</template>
```

### 角色

角色判断方式不需要接口返回的权限码，直接通过角色来判断按钮是否显示。

#### 组件方式

```vue
<script lang="ts" setup>
import { AccessControl } from '@vben/access';
</script>

<template>
  <AccessControl :codes="['super']">
    <Button> Super 角色可见 </Button>
  </AccessControl>
  <AccessControl :codes="['admin']">
    <Button> Admin 角色可见 </Button>
  </AccessControl>
  <AccessControl :codes="['user']">
    <Button> User 角色可见 </Button>
  </AccessControl>
  <AccessControl :codes="['super', 'admin']">
    <Button> Super & Admin 角色可见 </Button>
  </AccessControl>
</template>
```

#### API方式

```vue
<script lang="ts" setup>
import { useAccess } from '@vben/access';

const { hasAccessByRoles } = useAccess();
</script>

<template>
  <Button v-if="hasAccessByRoles(['super'])"> Super 账号可见 </Button>
  <Button v-if="hasAccessByRoles(['admin'])"> Admin 账号可见 </Button>
  <Button v-if="hasAccessByRoles(['user'])"> User 账号可见 </Button>
  <Button v-if="hasAccessByRoles(['super', 'admin'])">
    Super & Admin 账号可见
  </Button>
</template>
```

#### 指令方式

> 指令支持绑定单个或多个角色。单个时可以直接传入字符串或数组中包含一个角色，多个角色均可访问则传入数组。

```vue
<template>
  <Button class="mr-4" v-access:role="'super'"> Super 角色可见 </Button>
  <Button class="mr-4" v-access:role="['super']"> Super 角色可见 </Button>
  <Button class="mr-4" v-access:role="['admin']"> Admin 角色可见 </Button>
  <Button class="mr-4" v-access:role="['user']"> User 角色可见 </Button>
  <Button class="mr-4" v-access:role="['super', 'admin']">
    Super & Admin 角色可见
  </Button>
</template>
```
