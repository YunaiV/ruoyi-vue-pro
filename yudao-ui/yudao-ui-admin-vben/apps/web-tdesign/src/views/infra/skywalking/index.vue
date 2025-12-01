<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { DocAlert, IFrame, Page } from '@vben/common-ui';

import { getConfigKey } from '#/api/infra/config';

const loading = ref(true); // 是否加载中
const src = ref('http://skywalking.shop.iocoder.cn');

/** 初始化 */
onMounted(async () => {
  try {
    const data = await getConfigKey('url.skywalking');
    if (data && data.length > 0) {
      src.value = data;
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="服务监控" url="https://doc.iocoder.cn/server-monitor/" />
    </template>

    <IFrame v-if="!loading" v-loading="loading" :src="src" />
  </Page>
</template>
