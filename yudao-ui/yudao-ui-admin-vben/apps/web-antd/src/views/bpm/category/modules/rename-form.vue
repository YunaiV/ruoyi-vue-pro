<script lang="ts" setup>
import type { BpmCategoryApi } from '#/api/bpm/category';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getCategory, updateCategory } from '#/api/bpm/category';
import { $t } from '#/locales';

const emit = defineEmits(['success']);
const formData = ref<BpmCategoryApi.Category>();

// 定义表单结构
const formSchema = [
  {
    fieldName: 'name',
    label: '分类名',
    component: 'Input',
    componentProps: {
      placeholder: '请输入分类名',
    },
    rules: 'required',
  },
];

// 创建表单
const [Form, formApi] = useVbenForm({
  layout: 'horizontal',
  schema: formSchema,
  showDefaultActions: false,
});

// 创建模态窗
const [Modal, modalApi] = useVbenModal({
  // 保存按钮回调
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();

    // 提交表单，只更新流程分类名
    const formValues = await formApi.getValues();
    const data = {
      id: formData.value?.id,
      name: formValues.name, // 只更新流程分类名
      code: formData.value?.code,
      status: formData.value?.status,
      description: formData.value?.description,
      sort: formData.value?.sort,
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

  // 打开/关闭弹窗回调
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
      // 获取流程分类数据
      formData.value = await getCategory(data.id);
      // 仅设置 name 字段
      await formApi.setValues({
        name: formData.value.name,
      });
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="重命名流程分类">
    <Form class="mx-4" />
  </Modal>
</template>
