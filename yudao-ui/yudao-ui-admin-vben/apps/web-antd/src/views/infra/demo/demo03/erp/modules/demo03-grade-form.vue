<script lang="ts" setup>
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/erp';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDemo03Grade,
  getDemo03Grade,
  updateDemo03Grade,
} from '#/api/infra/demo/demo03/erp';
import { $t } from '#/locales';

import { useDemo03GradeFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<Demo03StudentApi.Demo03Grade>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['学生班级'])
    : $t('ui.actionTitle.create', ['学生班级']);
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
  schema: useDemo03GradeFormSchema(),
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
    const data = (await formApi.getValues()) as Demo03StudentApi.Demo03Grade;
    data.studentId = formData.value?.studentId;
    try {
      await (formData.value?.id
        ? updateDemo03Grade(data)
        : createDemo03Grade(data));
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
    let data = modalApi.getData<Demo03StudentApi.Demo03Grade>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getDemo03Grade(data.id);
      } finally {
        modalApi.unlock();
      }
    }
    // 设置到 values
    formData.value = data;
    await formApi.setValues(formData.value);
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
