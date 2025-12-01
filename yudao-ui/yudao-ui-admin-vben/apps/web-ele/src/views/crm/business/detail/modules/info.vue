<script lang="ts" setup>
import type { CrmBusinessApi } from '#/api/crm/business';

import { ElDivider } from 'element-plus';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  business: CrmBusinessApi.Business; // 商机信息
}>();

const [BaseDescription] = useDescription({
  title: '基本信息',
  border: false,
  column: 4,
  schema: useDetailBaseSchema(),
});

const [SystemDescription] = useDescription({
  title: '系统信息',
  border: false,
  column: 3,
  schema: useFollowUpDetailSchema(),
});
</script>

<template>
  <div>
    <BaseDescription :data="business" />
    <ElDivider />
    <SystemDescription :data="business" />
  </div>
</template>
