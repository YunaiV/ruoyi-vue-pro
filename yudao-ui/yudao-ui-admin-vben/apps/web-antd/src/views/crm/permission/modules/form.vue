<script lang="ts" setup>
import type { CrmPermissionApi } from '#/api/crm/permission';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createPermission, updatePermission } from '#/api/crm/permission';
import { $t } from '#/locales';

import { useFormSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<CrmPermissionApi.Permission>();
const getTitle = computed(() => {
  return formData.value?.ids
    ? $t('ui.actionTitle.edit', ['团队成员'])
    : $t('ui.actionTitle.create', ['团队成员']);
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
    const data = (await formApi.getValues()) as CrmPermissionApi.Permission;
    try {
      await (formData.value?.ids
        ? updatePermission(data)
        : createPermission(data));
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
    const data = modalApi.getData();
    if (!data || !data.bizType || !data.bizId) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = {
        ids: data.ids ?? (data.id ? [data.id] : undefined),
        userId: undefined,
        bizType: data.bizType,
        bizId: data.bizId,
        level: data.level,
      };
      // 设置到 values
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
