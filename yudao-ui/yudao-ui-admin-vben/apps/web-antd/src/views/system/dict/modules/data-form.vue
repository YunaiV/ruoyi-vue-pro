<script lang="ts" setup>
import type { SystemDictDataApi } from '#/api/system/dict/data';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDictData,
  getDictData,
  updateDictData,
} from '#/api/system/dict/data';
import { $t } from '#/locales';

import { useDataFormSchema } from '../data';

defineOptions({ name: 'SystemDictDataForm' });

const emit = defineEmits(['success']);
const formData = ref<SystemDictDataApi.DictData>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['字典数据'])
    : $t('ui.actionTitle.create', ['字典数据']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useDataFormSchema(),
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
    const data = (await formApi.getValues()) as SystemDictDataApi.DictData;
    try {
      await (formData.value?.id ? updateDictData(data) : createDictData(data));
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
    const data = modalApi.getData<SystemDictDataApi.DictData>();
    if (!data || !data.id) {
      // 设置 dictType
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDictData(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
