<script setup lang="ts">
import type { BpmOALeaveApi } from '#/api/bpm/oa/leave';

import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { ContentWrap } from '@vben/common-ui';

import { getLeave } from '#/api/bpm/oa/leave';
import { Description } from '#/components/description';

import { useDetailFormSchema } from './data';

const props = defineProps<{
  id: string;
}>();
const datailLoading = ref(false);
const detailData = ref<BpmOALeaveApi.Leave>();

const { query } = useRoute();
const queryId = computed(() => query.id as string);

async function getDetailData() {
  try {
    datailLoading.value = true;
    detailData.value = await getLeave(Number(props.id || queryId.value));
  } finally {
    datailLoading.value = false;
  }
}

onMounted(() => {
  getDetailData();
});
</script>

<template>
  <ContentWrap class="m-2">
    <Description
      :data="detailData"
      :schema="useDetailFormSchema()"
      :component-props="{
        column: 1,
        bordered: true,
        size: 'small',
      }"
    />
  </ContentWrap>
</template>

<style lang="scss" scoped>
:deep(.ant-descriptions-item-label) {
  width: 150px;
}
</style>
