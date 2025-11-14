<script lang="ts" setup>
import type { PayChannelApi } from '#/api/pay/channel';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { CommonStatusEnum } from '@vben/constants';
import { $t } from '@vben/locales';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createChannel, getChannel, updateChannel } from '#/api/pay/channel';

import { useChannelFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<any>();
const title = computed(() => {
  return formData.value?.id === 0
    ? $t('ui.actionTitle.create', ['渠道'])
    : $t('ui.actionTitle.edit', ['渠道']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 160,
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
    const data = (await formApi.getValues()) as PayChannelApi.Channel;
    data.config = JSON.stringify(data.config);
    try {
      await (data.id ? updateChannel(data) : createChannel(data));
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
    const { appId, code } = modalApi.getData() as {
      appId?: number;
      code?: string;
    };
    if (!appId || !code) {
      return;
    }
    modalApi.lock();
    formData.value = {
      appId,
      code,
      status: CommonStatusEnum.ENABLE,
      remark: '',
      feeRate: 0,
      config: {},
    };
    if (code.includes('alipay_')) {
      formData.value.config = {
        appId: undefined,
        serverUrl: undefined,
        signType: 'RSA2',
        mode: undefined,
        privateKey: undefined,
        alipayPublicKey: undefined,
        appCertContent: undefined,
        alipayPublicCertContent: undefined,
        rootCertContent: undefined,
        encryptType: undefined,
        encryptKey: undefined,
      };
    } else if (code.includes('mock')) {
      formData.value.config = {
        name: 'mock-conf',
      };
    } else if (code.includes('wallet')) {
      formData.value.config = {
        config: {
          name: 'wallet-conf',
        },
      };
    } else if (code.includes('wx')) {
      formData.value.config = {
        appId: undefined,
        mchId: undefined,
        apiVersion: undefined,
        mchKey: undefined,
        keyContent: undefined,
        privateKeyContent: undefined,
        certSerialNo: undefined,
        apiV3Key: undefined,
        publicKeyContent: undefined,
        publicKeyId: undefined,
      };
    }

    try {
      const res = await getChannel(appId, code);
      if (res) {
        formData.value = {
          ...res,
          config: JSON.parse(res.config),
        };
      }
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="title" class="w-2/5">
    <Form :schema="useChannelFormSchema(formData?.code)" />
  </Modal>
</template>
