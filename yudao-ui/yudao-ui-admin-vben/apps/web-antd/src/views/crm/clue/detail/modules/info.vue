<script lang="ts" setup>
import type { CrmClueApi } from '#/api/crm/clue';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  clue: CrmClueApi.Clue;
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
  schema: useFollowUpDetailSchema(),
});
</script>

<template>
  <div>
    <BaseDescriptions :data="clue" />
    <Divider />
    <SystemDescriptions :data="clue" />
  </div>
</template>
