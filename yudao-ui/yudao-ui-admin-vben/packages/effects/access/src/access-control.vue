<!--
 Access control component for fine-grained access control.
 TODO: 可以扩展更完善的功能：
 1. 支持多个权限码，只要有一个权限码满足即可 或者 多个权限码全部满足
 2. 支持多个角色，只要有一个角色满足即可 或者 多个角色全部满足
 3. 支持自定义权限码和角色的判断逻辑
-->
<script lang="ts" setup>
import { computed } from 'vue';

import { useAccess } from './use-access';

interface Props {
  /**
   * Specified codes is visible
   * @default []
   */
  codes?: string[];

  /**
   * 通过什么方式来控制组件，如果是 role，则传入角色，如果是 code，则传入权限码
   * @default 'role'
   */
  type?: 'code' | 'role';
}

defineOptions({
  name: 'AccessControl',
});

const props = withDefaults(defineProps<Props>(), {
  codes: () => [],
  type: 'role',
});

const { hasAccessByCodes, hasAccessByRoles } = useAccess();

const hasAuth = computed(() => {
  const { codes, type } = props;
  return type === 'role' ? hasAccessByRoles(codes) : hasAccessByCodes(codes);
});
</script>

<template>
  <slot v-if="!codes"></slot>
  <slot v-else-if="hasAuth"></slot>
</template>
