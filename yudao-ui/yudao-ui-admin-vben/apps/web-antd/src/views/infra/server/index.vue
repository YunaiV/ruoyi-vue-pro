<script setup lang="ts">
import { onMounted, ref } from 'vue';

import { DocAlert, IFrame, Page } from '@vben/common-ui';

import { getConfigKey } from '#/api/infra/config';

const loading = ref(true); // 是否加载中
const src = ref(`${import.meta.env.VITE_BASE_URL}/admin/applications`);

/** 初始化 */
onMounted(async () => {
  try {
    // 友情提示：如果访问出现 404 问题：
    // 1）boot 参考 https://doc.iocoder.cn/server-monitor/ 解决；
    // 2）cloud 参考 https://cloud.iocoder.cn/server-monitor/ 解决
    const data = await getConfigKey('url.spring-boot-admin');
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
