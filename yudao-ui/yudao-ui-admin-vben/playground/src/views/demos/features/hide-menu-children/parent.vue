<script lang="ts" setup>
import { Fallback } from '@vben/common-ui';
</script>

<template>
  <Fallback
    :description="`当前路由：${String($route.name)}，子菜单不可见`"
    status="coming-soon"
    title="隐藏子菜单"
  >
    <template #action>
      <RouterLink to="/demos/features/hide-menu-children/children">
        打开子路由
      </RouterLink>
    </template>
  </Fallback>
</template>
