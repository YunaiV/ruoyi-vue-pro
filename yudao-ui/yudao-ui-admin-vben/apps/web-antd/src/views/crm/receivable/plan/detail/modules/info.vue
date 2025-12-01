<script lang="ts" setup>
import type { CrmReceivablePlanApi } from '#/api/crm/receivable/plan';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';

import { useDetailBaseSchema, useDetailSystemSchema } from '../data';

defineProps<{
  receivablePlan: CrmReceivablePlanApi.Plan; // 收款计划信息
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
    <BaseDescriptions :data="receivablePlan" />
    <Divider />
    <SystemDescriptions :data="receivablePlan" />
  </div>
</template>
