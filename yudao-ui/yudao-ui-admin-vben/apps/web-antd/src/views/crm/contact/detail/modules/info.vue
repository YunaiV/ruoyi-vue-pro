<script lang="ts" setup>
import type { CrmContactApi } from '#/api/crm/contact';

import { Divider } from 'ant-design-vue';

import { useDescription } from '#/components/description';
import { useFollowUpDetailSchema } from '#/views/crm/followup/data';

import { useDetailBaseSchema } from '../data';

defineProps<{
  contact: CrmContactApi.Contact;
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
    <BaseDescriptions :data="contact" />
    <Divider />
    <SystemDescriptions :data="contact" />
  </div>
</template>
