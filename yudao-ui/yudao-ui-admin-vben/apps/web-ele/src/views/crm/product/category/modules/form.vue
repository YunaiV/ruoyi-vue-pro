<script lang="ts" setup>
import type { CrmProductCategoryApi } from '#/api/crm/product/category';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createProductCategory,
  getProductCategory,
  updateProductCategory,
} from '#/api/crm/product/category';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmProductCategoryApi.ProductCategory>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['产品分类'])
    : $t('ui.actionTitle.create', ['产品分类']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as CrmProductCategoryApi.ProductCategory;
    try {
      await (formData.value?.id
        ? updateProductCategory(data)
        : createProductCategory(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    let data = modalApi.getData<CrmProductCategoryApi.ProductCategory>();
    if (!data || !data.id) {
      // 设置上级
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      if (data.id) {
        data = await getProductCategory(data.id);
      }
      // 设置到 values
      formData.value = data;
      await formApi.setValues(data);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
