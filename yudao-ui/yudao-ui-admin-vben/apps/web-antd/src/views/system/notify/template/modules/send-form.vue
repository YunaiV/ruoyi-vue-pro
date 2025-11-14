<script lang="ts" setup>
import type { SystemNotifyTemplateApi } from '#/api/system/notify/template';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { sendNotify } from '#/api/system/notify/template';
import { $t } from '#/locales';

import { useSendNotifyFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemNotifyTemplateApi.NotifyTemplate>();

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
    const paramsObj: Record<string, string> = {};
    if (formData.value?.params) {
      formData.value.params.forEach((param) => {
        paramsObj[param] = values[`param_${param}`];
      });
    }
    const data: SystemNotifyTemplateApi.NotifySendReqVO = {
      userId: values.userId,
      userType: values.userType,
      templateCode: formData.value?.code || '',
      templateParams: paramsObj,
    };

    // 提交表单
    try {
      await sendNotify(data);
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } catch (error) {
      console.error('发送站内信失败', error);
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
    const data = modalApi.getData<SystemNotifyTemplateApi.NotifyTemplate>();
    if (!data || !data.id) {
      return;
    }
    formData.value = data;
    // 更新 form schema
    const schema = buildFormSchema();
    formApi.setState({ schema });
    // 设置到 values
    await formApi.setValues({
      content: data.content,
      templateCode: data.code,
    });
  },
});

/** 动态构建表单 schema */
function buildFormSchema() {
  const schema = useSendNotifyFormSchema();
  if (formData.value?.params) {
    formData.value.params.forEach((param: string) => {
      schema.push({
        fieldName: `param_${param}`,
        label: `参数 ${param}`,
        component: 'Input',
        rules: 'required',
        componentProps: {
          placeholder: `请输入参数 ${param}`,
        },
      });
    });
  }
  return schema;
}
</script>

<template>
  <Modal title="测试发送站内信">
    <Form class="mx-4" />
  </Modal>
</template>
