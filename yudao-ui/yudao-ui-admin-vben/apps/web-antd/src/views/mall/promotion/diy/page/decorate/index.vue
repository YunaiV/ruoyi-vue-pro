<script setup lang="ts">
import type { MallDiyPageApi } from '#/api/mall/promotion/diy/page';

import { onMounted, ref, unref } from 'vue';
import { useRoute } from 'vue-router';

import { message } from 'ant-design-vue';

import {
  getDiyPageProperty,
  updateDiyPageProperty,
} from '#/api/mall/promotion/diy/page';
import { DiyEditor, PAGE_LIBS } from '#/views/mall/promotion/components';

/** 装修页面表单 */
defineOptions({ name: 'DiyPageDecorate' });

const route = useRoute();

const formData = ref<MallDiyPageApi.DiyPage>();

/** 获取详情 */
async function getPageDetail(id: any) {
  const hideLoading = message.loading({
    content: '加载中...',
    duration: 0,
  });
  try {
    formData.value = await getDiyPageProperty(id);
  } finally {
    hideLoading();
  }
}

/** 提交表单 */
async function submitForm() {
  const hideLoading = message.loading({
    content: '保存中...',
    duration: 0,
  });
  try {
    await updateDiyPageProperty(unref(formData)!);
    message.success('保存成功');
  } finally {
    hideLoading();
  }
}

/** 初始化 */
onMounted(() => {
  if (!route.params.id) {
    message.warning('参数错误，页面编号不能为空！');
    return;
  }
  formData.value = {} as MallDiyPageApi.DiyPage;
  getPageDetail(route.params.id);
});
</script>
<template>
  <DiyEditor
    v-if="formData?.id"
    v-model="formData!.property"
    :title="formData!.name"
    :libs="PAGE_LIBS"
    @save="submitForm"
  />
</template>
