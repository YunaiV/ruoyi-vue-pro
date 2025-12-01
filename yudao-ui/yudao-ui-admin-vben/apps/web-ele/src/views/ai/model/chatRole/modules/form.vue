<script lang="ts" setup>
import type { AiModelChatRoleApi } from '#/api/ai/model/chatRole';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createChatRole,
  getChatRole,
  updateChatRole,
} from '#/api/ai/model/chatRole';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<AiModelChatRoleApi.ChatRole>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['聊天角色'])
    : $t('ui.actionTitle.create', ['聊天角色']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
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
    const data = (await formApi.getValues()) as AiModelChatRoleApi.ChatRole;
    try {
      await (formData.value?.id ? updateChatRole(data) : createChatRole(data));
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
    const data = modalApi.getData<AiModelChatRoleApi.ChatRole>();
    if (!data || !data.id) {
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getChatRole(data.id);
      // 设置到 values
      await formApi.setValues({ ...data, ...formData.value });
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
