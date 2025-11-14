<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getAreaByIp } from '#/api/system/area';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const [Form, { setFieldValue, validate, getValues }] = useVbenForm({
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
    const { valid } = await validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = await getValues();
    try {
      const result = await getAreaByIp(data.ip);
      // 设置结果
      await setFieldValue('result', result);
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="IP 查询">
    <Form class="mx-4" />
  </Modal>
</template>
