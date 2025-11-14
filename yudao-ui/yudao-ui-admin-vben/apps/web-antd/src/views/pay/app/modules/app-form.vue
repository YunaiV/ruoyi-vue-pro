<script lang="ts" setup>
import type { PayAppApi } from '#/api/pay/app';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createApp, getApp, updateApp } from '#/api/pay/app';

import { useAppFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<PayAppApi.App>();
const title = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['应用'])
    : $t('ui.actionTitle.create', ['应用']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 160,
  },
  layout: 'horizontal',
  schema: useAppFormSchema(),
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
    const data = (await formApi.getValues()) as PayAppApi.App;
    try {
      await (formData.value?.id ? updateApp(data) : createApp(data));
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
    const { id } = modalApi.getData() as {
      id?: number;
    };
    if (!id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getApp(id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>
<template>
  <Modal :title="title" class="w-2/5">
    <Form />
  </Modal>
</template>
