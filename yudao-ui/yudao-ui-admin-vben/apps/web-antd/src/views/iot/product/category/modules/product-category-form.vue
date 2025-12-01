<script lang="ts" setup>
import type { IotProductCategoryApi } from '#/api/iot/product/category';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createProductCategory,
  getProductCategory,
  updateProductCategory,
} from '#/api/iot/product/category';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

// TODO @haohao：应该是 form.vue，不用前缀；

const emit = defineEmits(['success']);
const formData = ref<IotProductCategoryApi.ProductCategory>();
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

// TODO @haohao：参考 apps/web-antd/src/views/system/dept/modules/form.vue 简化 useVbenModal 里的代码；
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as IotProductCategoryApi.ProductCategory;
    try {
      await (formData.value?.id
        ? updateProductCategory(data)
        : createProductCategory(data));
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
      formApi.resetForm();
      return;
    }

    // 重置表单
    await formApi.resetForm();

    const data = modalApi.getData<IotProductCategoryApi.ProductCategory>();
    // 如果没有数据或没有 id，表示是新增
    if (!data || !data.id) {
      formData.value = undefined;
      // 新增模式：设置默认值
      await formApi.setValues({
        sort: 0,
        status: 1,
      });
      return;
    }

    // 编辑模式：加载数据
    modalApi.lock();
    try {
      formData.value = await getProductCategory(data.id);
      await formApi.setValues(formData.value);
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
