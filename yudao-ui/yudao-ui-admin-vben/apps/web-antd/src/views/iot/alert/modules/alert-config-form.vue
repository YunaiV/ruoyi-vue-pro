<script setup lang="ts">
import type { AlertConfigApi } from '#/api/iot/alert/config';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createAlertConfig,
  getAlertConfig,
  updateAlertConfig,
} from '#/api/iot/alert/config';
import { $t } from '#/locales';

import { useFormSchema } from '../config/data';

defineOptions({ name: 'IoTAlertConfigForm' });

const emit = defineEmits(['success']);
const formData = ref<AlertConfigApi.AlertConfig>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['告警配置'])
    : $t('ui.actionTitle.create', ['告警配置']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  wrapperClass: 'grid-cols-2',
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
    const data = (await formApi.getValues()) as AlertConfigApi.AlertConfig;
    try {
      await (formData.value?.id
        ? updateAlertConfig(data)
        : createAlertConfig(data));
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
    const data = modalApi.getData<AlertConfigApi.AlertConfig>();
    if (!data || !data.id) {
      // 新增时设置默认值
      await formApi.setValues({
        status: 0,
        sceneRuleIds: [],
        receiveUserIds: [],
        receiveTypes: [],
      });
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getAlertConfig(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-3/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
