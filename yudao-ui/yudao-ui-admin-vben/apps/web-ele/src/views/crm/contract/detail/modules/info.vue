<script lang="ts" setup>
import type { CrmContractApi } from '#/api/crm/contract';

import { ElDivider } from 'element-plus';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  contract: CrmContractApi.Contract; // 合同信息
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
    <BaseDescriptions :data="contract" />
    <ElDivider />
    <SystemDescriptions :data="contract" />
  </div>
</template>
