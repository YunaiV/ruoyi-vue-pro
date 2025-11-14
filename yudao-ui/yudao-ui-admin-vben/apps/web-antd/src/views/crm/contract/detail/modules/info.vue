<script lang="ts" setup>
import type { CrmContractApi } from '#/api/crm/contract';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  contract: CrmContractApi.Contract; // 合同信息
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
  schema: useFollowUpDetailSchema(),
});
</script>

<template>
  <div class="p-4">
    <BaseDescriptions :data="contract" />
    <Divider />
    <SystemDescriptions :data="contract" />
  </div>
</template>
