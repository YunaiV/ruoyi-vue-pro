<script lang="ts" setup>
import type { BpmCategoryApi } from '#/api/bpm/category';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getCategory, updateCategory } from '#/api/bpm/category';
import { $t } from '#/locales';

import { useRenameFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<BpmCategoryApi.Category>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useRenameFormSchema(),
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
    const data = {
      ...formData.value,
      ...(await formApi.getValues()),
    } as BpmCategoryApi.Category;
    try {
      await updateCategory(data);
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
    const data = modalApi.getData<BpmCategoryApi.Category>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getCategory(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="重命名流程分类" class="w-1/3">
    <Form class="mx-4" />
  </Modal>
</template>
