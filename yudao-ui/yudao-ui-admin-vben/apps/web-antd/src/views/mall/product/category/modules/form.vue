<script lang="ts" setup>
import type { MallCategoryApi } from '#/api/mall/product/category';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createCategory,
  getCategory,
  updateCategory,
} from '#/api/mall/product/category';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallCategoryApi.Category>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['商品分类'])
    : $t('ui.actionTitle.create', ['商品分类']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
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
    const data = (await formApi.getValues()) as MallCategoryApi.Category;
    try {
      await (formData.value?.id ? updateCategory(data) : createCategory(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
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
    let data = modalApi.getData<MallCategoryApi.Category>();
    if (!data) {
      return;
    }
    modalApi.lock();
    try {
      if (data.id) {
        data = await getCategory(data.id);
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
  <Modal :title="getTitle" class="w-1/4">
    <Form class="mx-4" />
  </Modal>
</template>
