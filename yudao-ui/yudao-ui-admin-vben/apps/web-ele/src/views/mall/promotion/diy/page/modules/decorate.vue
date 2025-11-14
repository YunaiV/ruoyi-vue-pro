<script setup lang="ts">
import type { MallDiyPageApi } from '#/api/mall/promotion/diy/page';

import { onMounted, ref, unref } from 'vue';
import { useRoute } from 'vue-router';

import { ElMessage } from 'element-plus';

import * as DiyPageApi from '#/api/mall/promotion/diy/page';
import { PAGE_LIBS } from '#/components/diy-editor/util';

/** 装修页面表单 */
defineOptions({ name: 'DiyPageDecorate' });

const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const formData = ref<MallDiyPageApi.DiyPage>();
const formRef = ref(); // 表单 Ref

// 获取详情
const getPageDetail = async (id: any) => {
  formLoading.value = true;
  try {
    formData.value = await DiyPageApi.getDiyPageProperty(id);
  } finally {
    formLoading.value = false;
  }
};

// 提交表单
const submitForm = async () => {
  // 校验表单
  if (!formRef.value) return;
  // 提交请求
  formLoading.value = true;
  try {
    await DiyPageApi.updateDiyPageProperty(unref(formData)!);
    ElMessage.success('保存成功');
  } finally {
    formLoading.value = false;
  }
};

// 重置表单
const resetForm = () => {
  formData.value = {
    id: undefined,
    templateId: undefined,
    name: '',
    remark: '',
    previewPicUrls: [],
    property: '',
  } as MallDiyPageApi.DiyPage;
  formRef.value?.resetFields();
};

/** 初始化 */
const route = useRoute();
onMounted(() => {
  resetForm();
  if (!route.params.id) {
    ElMessage.warning('参数错误，页面编号不能为空！');
    return;
  }
  getPageDetail(route.params.id);
});
</script>
<template>
  <DiyEditor
    v-if="formData && !formLoading"
    v-model="formData.property"
    :title="formData.name"
    :libs="PAGE_LIBS"
    @save="submitForm"
  />
</template>
