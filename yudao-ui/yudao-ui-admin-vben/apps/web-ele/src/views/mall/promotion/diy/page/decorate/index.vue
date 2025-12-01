<script setup lang="ts">
import type { MallDiyPageApi } from '#/api/mall/promotion/diy/page';

import { onMounted, ref, unref } from 'vue';
import { useRoute } from 'vue-router';

import { ElLoading, ElMessage } from 'element-plus';

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
  const loadingInstance = ElLoading.service({
    text: '加载中...',
  });
  try {
    formData.value = await getDiyPageProperty(id);
  } finally {
    loadingInstance.close();
  }
}

/** 提交表单 */
async function submitForm() {
  const loadingInstance = ElLoading.service({
    text: '保存中...',
  });
  try {
    await updateDiyPageProperty(unref(formData)!);
    ElMessage.success('保存成功');
  } finally {
    loadingInstance.close();
  }
}

/** 初始化 */
onMounted(() => {
  if (!route.params.id) {
    ElMessage.warning('参数错误，页面编号不能为空！');
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
