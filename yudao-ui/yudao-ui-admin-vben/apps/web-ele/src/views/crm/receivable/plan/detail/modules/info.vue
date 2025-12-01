<script lang="ts" setup>
import type { CrmReceivablePlanApi } from '#/api/crm/receivable/plan';

import { ElDivider } from 'element-plus';

import { useDescription } from '#/components/description';

import { useDetailBaseSchema, useDetailSystemSchema } from '../data';

defineProps<{
  receivablePlan: CrmReceivablePlanApi.Plan; // 收款计划信息
}>();

const [BaseDescriptions] = useDescription({
  title: '基本信息',
  border: false,
  column: 4,
  schema: useDetailBaseSchema(),
});

const [SystemDescriptions] = useDescription({
  title: '系统信息',
  border: false,
  column: 3,
  schema: useDetailSystemSchema(),
});
</script>

<template>
  <div>
    <BaseDescriptions :data="receivablePlan" />
    <ElDivider />
    <SystemDescriptions :data="receivablePlan" />
  </div>
</template>
