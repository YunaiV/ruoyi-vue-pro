<script lang="ts" setup>
import type { FcDesigner } from '@form-create/antd-designer';

import type { BpmFormApi } from '#/api/bpm/form';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createForm, updateForm } from '#/api/bpm/form';
import { encodeConf, encodeFields } from '#/components/form-create';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);

const designerComponent = ref<InstanceType<typeof FcDesigner>>();
const formData = ref<BpmFormApi.Form>();
const editorAction = ref<string>();

const getTitle = computed(() => {
  if (!formData.value?.id) {
    return $t('ui.actionTitle.create', ['流程表单']);
  }
  return editorAction.value === 'copy'
    ? $t('ui.actionTitle.copy', ['流程表单'])
    : $t('ui.actionTitle.edit', ['流程表单']);
});

const [Form, formApi] = useVbenForm({
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
    try {
      // 获取表单数据
      const data = (await formApi.getValues()) as BpmFormApi.Form;
      // 编码表单配置和表单字段
      data.conf = encodeConf(designerComponent);
      data.fields = encodeFields(designerComponent);
      // 保存表单数据
      if (formData.value?.id) {
        await (editorAction.value === 'copy'
          ? createForm(data)
          : updateForm(data));
      } else {
        await createForm(data);
      }
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
      designerComponent.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<any>();
    if (!data) {
      return;
    }
    modalApi.lock();
    // 设置表单设计器组件
    designerComponent.value = data.designer;
    formData.value = data.formConfig;
    editorAction.value = data.action;
    // 如果是复制，表单名称后缀添加 _copy ，id 置空
    if (editorAction.value === 'copy' && formData.value) {
      formData.value = {
        ...formData.value,
        name: `${formData.value.name}_copy`,
        id: undefined,
      };
    }
    try {
      // 设置到 values
      if (formData.value) {
        await formApi.setValues(formData.value);
      }
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-4" />
  </Modal>
</template>
