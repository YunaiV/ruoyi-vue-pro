<script lang="ts" setup>
import { onMounted, ref } from 'vue';

import { DocAlert, IFrame, Page } from '@vben/common-ui';

import { getConfigKey } from '#/api/infra/config';

const loading = ref(true); // 是否加载中
const src = ref(`${import.meta.env.VITE_BASE_URL}/doc.html`); // Knife4j UI
// const src = ref(import.meta.env.VITE_BASE_URL + '/swagger-ui') // Swagger UI

/** 初始化 */
onMounted(async () => {
  try {
    const data = await getConfigKey('url.swagger');
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
      <DocAlert title="接口文档" url="https://doc.iocoder.cn/api-doc/" />
    </template>

    <IFrame v-if="!loading" :src="src" />
  </Page>
</template>
