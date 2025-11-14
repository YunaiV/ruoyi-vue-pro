---
outline: deep
---

# Access Control

The framework has built-in three types of access control methods:

- Determining whether a menu or button can be accessed based on user roles
- Determining whether a menu or button can be accessed through an API
- Mixed mode: Using both frontend and backend access control simultaneously

## Frontend Access Control

**Implementation Principle**: The permissions for routes are hardcoded on the frontend, specifying which permissions are required to view certain routes. Only general routes are initialized, and routes that require permissions are not added to the route table. After logging in or obtaining user roles through other means, the roles are used to traverse the route table to generate a route table that the role can access. This table is then added to the router instance using `router.addRoute`, achieving permission filtering.

**Disadvantage**: The permissions are relatively inflexible; if the backend changes roles, the frontend needs to be adjusted accordingly. This is suitable for systems with relatively fixed roles.

### Steps

- Ensure the current mode is set to frontend access control

Adjust `preferences.ts` in the corresponding application directory to ensure `accessMode='frontend'`.

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    // Default value, optional
    accessMode: 'frontend',
  },
});
```

- Configure route permissions

#### If not configured, it is visible by default

```ts {3}
 {
    meta: {
      authority: ['super'],
    },
},
```

- Ensure the roles returned by the interface match the permissions in the route table

You can look under `src/store/auth` in the application to find the following code:

```ts
// Set the login user information, ensuring that userInfo.roles is an array and contains permissions from the route table
// For example: userInfo.roles=['super', 'admin']
authStore.setUserInfo(userInfo);
```

At this point, the configuration is complete. You need to ensure that the roles returned by the interface after login match the permissions in the route table; otherwise, access will not be possible.

### Menu Visible but Access Forbidden

Sometimes, we need the menu to be visible but access to it forbidden. This can be achieved by setting `menuVisibleWithForbidden` to `true`. In this case, the menu will be visible, but access will be forbidden, redirecting to a 403 page.

```ts
{
    meta: {
      menuVisibleWithForbidden: true,
    },
},
```

## Backend Access Control

**Implementation Principle**: It is achieved by dynamically generating a routing table through an API, which returns data following a certain structure. The frontend processes this data into a recognizable structure, then adds it to the routing instance using `router.addRoute`, realizing the dynamic generation of permissions.

**Disadvantage**: The backend needs to provide a data structure that meets the standards, and the frontend needs to process this structure. This is suitable for systems with more complex permissions.

### Steps

- Ensure the current mode is set to backend access control

Adjust `preferences.ts` in the corresponding application directory to ensure `accessMode='backend'`.

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    accessMode: 'backend',
  },
});
```

- Ensure the structure of the menu data returned by the interface is correct

You can look under `src/router/access.ts` in the application to find the following code:

```ts
async function generateAccess(options: GenerateMenuAndRoutesOptions) {
  return await generateAccessible(preferences.app.accessMode, {
    fetchMenuListAsync: async () => {
      // This interface is for the menu data returned by the backend
      return await getAllMenus();
    },
  });
}
```

- Interface returns menu data, see comments for explanation

::: details Example of Interface Returning Menu Data

```ts
const dashboardMenus = [
  {
    // Here, 'BasicLayout' is hardcoded and cannot be changed
    component: 'BasicLayout',
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
        // Here is the path of the page, need to remove 'views/' and '.vue'
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
];
```

:::

At this point, the configuration is complete. You need to ensure that after logging in, the format of the menu returned by the interface is correct; otherwise, access will not be possible.

## Mixed Access Control

**Implementation Principle**: Mixed mode combines both frontend access control and backend access control methods. The system processes frontend fixed route permissions and backend dynamic menu data in parallel, ultimately merging both parts of routes to provide a more flexible access control solution.

**Advantages**: Combines the performance advantages of frontend control with the flexibility of backend control, suitable for complex business scenarios requiring permission management.

### Steps

- Ensure the current mode is set to mixed access control

Adjust `preferences.ts` in the corresponding application directory to ensure `accessMode='mixed'`.

```ts
import { defineOverridesPreferences } from '@vben/preferences';

export const overridesPreferences = defineOverridesPreferences({
  // overrides
  app: {
    accessMode: 'mixed',
  },
});
```

- Configure frontend route permissions

Same as the route permission configuration method in [Frontend Access Control](#frontend-access-control) mode.

- Configure backend menu interface

Same as the interface configuration method in [Backend Access Control](#backend-access-control) mode.

- Ensure roles and permissions match

Must satisfy both frontend route permission configuration and backend menu data return requirements, ensuring user roles match the permission configurations of both modes.

At this point, the configuration is complete. Mixed mode will automatically merge frontend and backend routes, providing complete access control functionality.

## Fine-grained Control of Buttons

In some cases, we need to control the display of buttons with fine granularity. We can control the display of buttons through interfaces or roles.

### Permission Code

The permission code is the code returned by the interface. The logic to determine whether a button is displayed is located under `src/store/auth`:

```ts
const [fetchUserInfoResult, accessCodes] = await Promise.all([
  fetchUserInfo(),
  getAccessCodes(),
]);

userInfo = fetchUserInfoResult;
authStore.setUserInfo(userInfo);
accessStore.setAccessCodes(accessCodes);
```

Locate the `getAccessCodes` corresponding interface, which can be adjusted according to business logic.

The data structure returned by the permission code is an array of strings, for example: `['AC_100100', 'AC_100110', 'AC_100120', 'AC_100010']`

With the permission codes, you can use the `AccessControl` component and API provided by `@vben/access` to show and hide buttons.

#### Component Method

```vue
<script lang="ts" setup>
import { AccessControl, useAccess } from '@vben/access';

const { accessMode, hasAccessByCodes } = useAccess();
</script>

<template>
  <!-- You need to specify type="code" -->
  <AccessControl :codes="['AC_100100']" type="code">
    <Button> Visible to Super account ["AC_1000001"] </Button>
  </AccessControl>
  <AccessControl :codes="['AC_100030']" type="code">
    <Button> Visible to Admin account ["AC_100010"] </Button>
  </AccessControl>
  <AccessControl :codes="['AC_1000001']" type="code">
    <Button> Visible to User account ["AC_1000001"] </Button>
  </AccessControl>
  <AccessControl :codes="['AC_100100', 'AC_100010']" type="code">
    <Button>
      Visible to Super & Admin account ["AC_100100","AC_1000001"]
    </Button>
  </AccessControl>
</template>
```

#### API Method

```vue
<script lang="ts" setup>
import { AccessControl, useAccess } from '@vben/access';

const { hasAccessByCodes } = useAccess();
</script>

<template>
  <Button v-if="hasAccessByCodes(['AC_100100'])">
    Visible to Super account ["AC_1000001"]
  </Button>
  <Button v-if="hasAccessByCodes(['AC_100030'])">
    Visible to Admin account ["AC_100010"]
  </Button>
  <Button v-if="hasAccessByCodes(['AC_1000001'])">
    Visible to User account ["AC_1000001"]
  </Button>
  <Button v-if="hasAccessByCodes(['AC_100100', 'AC_1000001'])">
    Visible to Super & Admin account ["AC_100100","AC_1000001"]
  </Button>
</template>
```

#### Directive Method

> The directive supports binding single or multiple permission codes. For a single one, you can pass a string or an array containing one permission code, and for multiple permission codes, you can pass an array.

```vue
<template>
  <Button class="mr-4" v-access:code="'AC_100100'">
    Visible to Super account 'AC_100100'
  </Button>
  <Button class="mr-4" v-access:code="['AC_100030']">
    Visible to Admin account ["AC_100010"]
  </Button>
  <Button class="mr-4" v-access:code="['AC_1000001']">
    Visible to User account ["AC_1000001"]
  </Button>
  <Button class="mr-4" v-access:code="['AC_100100', 'AC_1000001']">
    Visible to Super & Admin account ["AC_100100","AC_1000001"]
  </Button>
</template>
```

### Roles

The method of determining roles does not require permission codes returned by the interface; it directly determines whether buttons are displayed based on roles.

#### Component Method

```vue
<script lang="ts" setup>
import { AccessControl } from '@vben/access';
</script>

<template>
  <AccessControl :codes="['super']">
    <Button> Visible to Super account </Button>
  </AccessControl>
  <AccessControl :codes="['admin']">
    <Button> Visible to Admin account </Button>
  </AccessControl>
  <AccessControl :codes="['user']">
    <Button> Visible to User account </Button>
  </AccessControl>
  <AccessControl :codes="['super', 'admin']">
    <Button> Super & Visible to Admin account </Button>
  </AccessControl>
</template>
```

#### API Method

```vue
<script lang="ts" setup>
import { useAccess } from '@vben/access';

const { hasAccessByRoles } = useAccess();
</script>

<template>
  <Button v-if="hasAccessByRoles(['super'])"> Visible to Super account </Button>
  <Button v-if="hasAccessByRoles(['admin'])"> Visible to Admin account </Button>
  <Button v-if="hasAccessByRoles(['user'])"> Visible to User account </Button>
  <Button v-if="hasAccessByRoles(['super', 'admin'])">
    Super & Visible to Admin account
  </Button>
</template>
```

#### Directive Method

> The directive supports binding single or multiple permission codes. For a single one, you can pass a string or an array containing one permission code, and for multiple permission codes, you can pass an array.

```vue
<template>
  <Button class="mr-4" v-access:role="'super'">
    Visible to Super account
  </Button>
  <Button class="mr-4" v-access:role="['admin']">
    Visible to Admin account
  </Button>
  <Button class="mr-4" v-access:role="['user']">
    Visible to User account
  </Button>
  <Button class="mr-4" v-access:role="['super', 'admin']">
    Super & Visible to Admin account
  </Button>
</template>
```
