<script setup lang="ts">
import type { IoTOtaFirmwareApi } from '#/api/iot/ota/firmware';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createOtaFirmware,
  getOtaFirmware,
  updateOtaFirmware,
} from '#/api/iot/ota/firmware';
import { $t } from '#/locales';

import { useFormSchema } from '../firmware/data';

defineOptions({ name: 'IoTOtaFirmwareForm' });

const emit = defineEmits<{
  success: [];
}>();

const formData = ref<IoTOtaFirmwareApi.Firmware>();

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['固件'])
    : $t('ui.actionTitle.create', ['固件']);
});

const [Form, formApi] = useVbenForm({
  schema: useFormSchema(),
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
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
    const data = (await formApi.getValues()) as IoTOtaFirmwareApi.Firmware;
    try {
      await (formData.value?.id
        ? updateOtaFirmware(data)
        : createOtaFirmware(data));
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
    const data = modalApi.getData<IoTOtaFirmwareApi.Firmware>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getOtaFirmware(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-1/3">
    <Form class="mx-4" />
  </Modal>
</template>
