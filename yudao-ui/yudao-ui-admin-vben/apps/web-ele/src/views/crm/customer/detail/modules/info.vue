<script lang="ts" setup>
import type { CrmCustomerApi } from '#/api/crm/customer';

import { ElDivider } from 'element-plus';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  customer: CrmCustomerApi.Customer; // 客户信息
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
  schema: useFollowUpDetailSchema(),
});
</script>

<template>
  <div>
    <BaseDescriptions :data="customer" />
    <ElDivider />
    <SystemDescriptions :data="customer" />
  </div>
</template>
