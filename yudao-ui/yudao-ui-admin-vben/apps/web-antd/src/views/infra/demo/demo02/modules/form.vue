<script lang="ts" setup>
import type { Demo02CategoryApi } from '#/api/infra/demo/demo02';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDemo02Category,
  getDemo02Category,
  updateDemo02Category,
} from '#/api/infra/demo/demo02';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<Demo02CategoryApi.Demo02Category>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['示例分类'])
    : $t('ui.actionTitle.create', ['示例分类']);
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
    const data =
      (await formApi.getValues()) as Demo02CategoryApi.Demo02Category;
    try {
      await (formData.value?.id
        ? updateDemo02Category(data)
        : createDemo02Category(data));
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
    const data = modalApi.getData<Demo02CategoryApi.Demo02Category>();
    if (!data || !data.id) {
      // 设置上级
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDemo02Category(data.id);
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
