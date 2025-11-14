<script lang="ts" setup>
import type { MallPropertyApi } from '#/api/mall/product/property';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createPropertyValue,
  getPropertyValue,
  updatePropertyValue,
} from '#/api/mall/product/property';
import { $t } from '#/locales';

import { useValueFormSchema } from '../data';

defineOptions({ name: 'MallPropertyValueForm' });

const emit = defineEmits(['success']);
const formData = ref<MallPropertyApi.PropertyValue>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['属性值'])
    : $t('ui.actionTitle.create', ['属性值']);
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
  schema: useValueFormSchema(),
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
    const data = (await formApi.getValues()) as MallPropertyApi.PropertyValue;
    try {
      await (formData.value?.id
        ? updatePropertyValue(data)
        : createPropertyValue(data));
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
    const data = modalApi.getData<MallPropertyApi.PropertyValue>();
    if (!data || !data.id) {
      // 设置 propertyId
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getPropertyValue(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
