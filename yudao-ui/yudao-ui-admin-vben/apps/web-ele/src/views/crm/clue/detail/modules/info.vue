<script lang="ts" setup>
import type { CrmClueApi } from '#/api/crm/clue';

import { ElDivider } from 'element-plus';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  clue: CrmClueApi.Clue;
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
    <BaseDescriptions :data="clue" />
    <ElDivider />
    <SystemDescriptions :data="clue" />
  </div>
</template>
