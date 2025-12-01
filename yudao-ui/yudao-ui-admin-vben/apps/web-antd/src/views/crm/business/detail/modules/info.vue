<script lang="ts" setup>
import type { CrmBusinessApi } from '#/api/crm/business';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  business: CrmBusinessApi.Business; // 商机信息
}>();

const [BaseDescription] = useDescription({
  title: '基本信息',
  bordered: false,
  column: 4,
  class: 'mx-4',
  schema: useDetailBaseSchema(),
});

const [SystemDescription] = useDescription({
  title: '系统信息',
  bordered: false,
  column: 3,
  class: 'mx-4',
  schema: useFollowUpDetailSchema(),
});
</script>

<template>
  <div>
    <BaseDescription :data="business" />
    <Divider />
    <SystemDescription :data="business" />
  </div>
</template>
