<script lang="ts" setup>
import type { CrmReceivableApi } from '#/api/crm/receivable';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';

import { useDetailBaseSchema, useDetailSystemSchema } from '../data';

defineProps<{
  receivable: CrmReceivableApi.Receivable; // 收款信息
}>();

const [BaseDescriptions] = useDescription({
  title: '基本信息',
  bordered: false,
  column: 4,
  class: 'mx-4',
  schema: useDetailBaseSchema(),
});

const [SystemDescriptions] = useDescription({
  title: '系统信息',
  bordered: false,
  column: 3,
  class: 'mx-4',
  schema: useDetailSystemSchema(),
});
</script>

<template>
  <div>
    <BaseDescriptions :data="receivable" />
    <Divider />
    <SystemDescriptions :data="receivable" />
  </div>
</template>
