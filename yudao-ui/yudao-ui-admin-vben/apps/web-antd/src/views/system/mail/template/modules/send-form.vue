<script lang="ts" setup>
import type { SystemMailTemplateApi } from '#/api/system/mail/template';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { sendMail } from '#/api/system/mail/template';

import { useSendMailFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemMailTemplateApi.MailTemplate>();

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
      formData.value.params.forEach((param: string) => {
        paramsObj[param] = values[`param_${param}`];
      });
    }
    const data: SystemMailTemplateApi.MailSendReqVO = {
      toMails: values.toMails,
      ccMails: values.ccMails,
      bccMails: values.bccMails,
      templateCode: formData.value?.code || '',
      templateParams: paramsObj,
    };

    // 提交表单
    try {
      await sendMail(data);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success('邮件发送成功');
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
    const data = modalApi.getData<SystemMailTemplateApi.MailTemplate>();
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
  const schema = useSendMailFormSchema();
  if (formData.value?.params) {
    formData.value.params?.forEach((param: string) => {
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
  <Modal title="测试发送邮件" class="w-1/3">
    <Form class="mx-4" />
  </Modal>
</template>
