<script lang="ts" setup>
import type { SystemSmsTemplateApi } from '#/api/system/sms/template';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { sendSms } from '#/api/system/sms/template';

import { useSendSmsFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemSmsTemplateApi.SmsTemplate>();

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
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
    const paramsObj: Record<string, string> = {};
    if (formData.value?.params) {
      formData.value.params.forEach((param) => {
        paramsObj[param] = values[`param_${param}`];
      });
    }
    const data: SystemSmsTemplateApi.SmsSendReqVO = {
      mobile: values.mobile,
      templateCode: formData.value?.code || '',
      templateParams: paramsObj,
    };

    // 提交表单
    try {
      await sendSms(data);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success('短信发送成功');
    } catch (error) {
      console.error('发送短信失败', error);
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
    const data = modalApi.getData<SystemSmsTemplateApi.SmsTemplate>();
    if (!data) {
      return;
    }
    formData.value = data;
    // 更新 form schema
    const schema = buildFormSchema();
    formApi.setState({ schema });
    // 设置到 values
    await formApi.setValues({
      content: data.content,
    });
  },
});

/** 动态构建表单 schema */
function buildFormSchema() {
  const schema = useSendSmsFormSchema();
  if (formData.value?.params) {
    formData.value.params.forEach((param) => {
      schema.push({
        fieldName: `param_${param}`,
        label: `参数 ${param}`,
        component: 'Input',
        componentProps: {
          placeholder: `请输入参数 ${param}`,
        },
        rules: 'required',
      });
    });
  }
  return schema;
}
</script>

<template>
  <Modal class="w-1/3" title="发送短信">
    <Form class="mx-4" />
  </Modal>
</template>
