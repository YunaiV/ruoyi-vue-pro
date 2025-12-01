<script lang="ts" setup>
import type { MpMessageTemplateApi } from '#/api/mp/messageTemplate';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { sendMessageTemplate } from '#/api/mp/messageTemplate';

import { useSendFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MpMessageTemplateApi.MessageTemplate>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
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
    // 构建发送请求
    const values = await formApi.getValues();
    const sendData: MpMessageTemplateApi.MessageTemplateSendVO = {
      id: formData.value?.id || 0,
      userId: values.userId,
      data: values.data || undefined,
      url: values.url || undefined,
      miniProgramAppId: values.miniProgramAppId || undefined,
      miniProgramPagePath: values.miniProgramPagePath || undefined,
    };
    // 如果填写了小程序信息，需要拼接成 miniprogram 字段
    if (sendData.miniProgramAppId && sendData.miniProgramPagePath) {
      sendData.miniprogram = JSON.stringify({
        appid: sendData.miniProgramAppId,
        pagepath: sendData.miniProgramPagePath,
      });
    }
    // 如果填写了 data 字段
    if (sendData.data && typeof sendData.data === 'string') {
      try {
        sendData.data = JSON.parse(sendData.data);
      } catch {
        ElMessage.error('模板数据格式不正确，请输入有效的 JSON 格式');
        modalApi.unlock();
        return;
      }
    }

    // 提交表单
    try {
      await sendMessageTemplate(sendData);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success('发送成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 获取数据
    const data = modalApi.getData<MpMessageTemplateApi.MessageTemplate>();
    if (!data) {
      return;
    }
    formData.value = data;
    // 更新 form schema
    const schema = useSendFormSchema(data.accountId);
    formApi.setState({ schema });
    // 设置到 values
    await formApi.setValues({
      id: data.id,
      title: data.title,
    });
  },
});
</script>

<template>
  <Modal class="w-[600px]" title="发送消息模板">
    <Form class="mx-4" />
  </Modal>
</template>
