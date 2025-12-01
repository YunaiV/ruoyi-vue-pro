<script setup lang="ts">
import type { IotDeviceGroupApi } from '#/api/iot/device/group';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDeviceGroup,
  getDeviceGroup,
  updateDeviceGroup,
} from '#/api/iot/device/group';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'IoTDeviceGroupForm' });

// TODO @haohao：web-antd/src/views/iot/product/category/modules/product-category-form.vue 类似问题

const emit = defineEmits<{
  success: [];
}>();

const formData = ref<IotDeviceGroupApi.DeviceGroup>();

const modalTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['设备分组'])
    : $t('ui.actionTitle.create', ['设备分组']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  schema: useFormSchema(),
  showCollapseButton: false,
  showDefaultActions: false,
});

// TODO @haohao：参考别的 form；1）文件的命名可以简化；2）代码可以在简化下；
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();

    try {
      const values = await formApi.getValues();

      await (formData.value?.id
        ? updateDeviceGroup({
            ...values,
            id: formData.value.id,
          } as IotDeviceGroupApi.DeviceGroup)
        : createDeviceGroup(values as IotDeviceGroupApi.DeviceGroup));

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
      await formApi.resetForm();
      return;
    }

    // 重置表单
    await formApi.resetForm();

    const data = modalApi.getData<IotDeviceGroupApi.DeviceGroup>();
    // 如果没有数据或没有 id，表示是新增
    if (!data || !data.id) {
      formData.value = undefined;
      return;
    }

    // 编辑模式：加载数据
    modalApi.lock();
    try {
      formData.value = await getDeviceGroup(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="modalTitle" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
