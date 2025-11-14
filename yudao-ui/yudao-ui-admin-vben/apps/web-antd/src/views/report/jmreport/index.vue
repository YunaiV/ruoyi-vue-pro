<script lang="ts" setup>
import { ref } from 'vue';

import { DocAlert, IFrame, Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

defineOptions({ name: 'JimuReport' });
const accessStore = useAccessStore();

const src = ref(
  `${import.meta.env.VITE_BASE_URL}/jmreport/list?token=${
    accessStore.refreshToken
  }`,
);
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="报表设计器" url="https://doc.iocoder.cn/report/" />
    </template>

    <IFrame :src="src" />
  </Page>
</template>
