<script lang="ts" setup>
import type { CrmReceivableApi } from '#/api/crm/receivable';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';

import { useDetailBaseSchema, useDetailSystemSchema } from '../data';

defineProps<{
  receivable: CrmReceivableApi.Receivable; // 收款信息
}>();

const [BaseDescriptions] = useDescription({
  componentProps: {
    title: '基本信息',
    bordered: false,
    column: 4,
    class: 'mx-4',
  },
  schema: useDetailBaseSchema(),
});

const [SystemDescriptions] = useDescription({
  componentProps: {
    title: '系统信息',
    bordered: false,
    column: 3,
    class: 'mx-4',
  },
  schema: useDetailSystemSchema(),
});
</script>

<template>
  <div class="p-4">
    <BaseDescriptions :data="receivable" />
    <Divider />
    <SystemDescriptions :data="receivable" />
  </div>
</template>
