<script setup lang="ts">
import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { updateDeviceGroup } from '#/api/iot/device/device';
import { $t } from '#/locales';

import { useGroupFormSchema } from '../data';

defineOptions({ name: 'IoTDeviceGroupForm' });

const emit = defineEmits(['success']);
const deviceIds = ref<number[]>([]);
const getTitle = computed(() => '添加设备到分组');

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
  schema: useGroupFormSchema(),
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
    const data = await formApi.getValues();
    try {
      await updateDeviceGroup({
        ids: deviceIds.value,
        groupIds: data.groupIds as number[],
      });
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
      deviceIds.value = [];
      return;
    }
    // 加载数据
    const ids = modalApi.getData<number[]>();
    if (ids) {
      deviceIds.value = ids;
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-1/3">
    <Form class="mx-4" />
  </Modal>
</template>
