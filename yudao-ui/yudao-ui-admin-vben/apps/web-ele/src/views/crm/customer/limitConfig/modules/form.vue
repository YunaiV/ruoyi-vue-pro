<script lang="ts" setup>
import type { CrmCustomerLimitConfigApi } from '#/api/crm/customer/limitConfig';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createCustomerLimitConfig,
  getCustomerLimitConfig,
  LimitConfType,
  updateCustomerLimitConfig,
} from '#/api/crm/customer/limitConfig';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<CrmCustomerLimitConfigApi.CustomerLimitConfig>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['规则'])
    : $t('ui.actionTitle.create', ['规则']);
});

const confType = ref<LimitConfType>(LimitConfType.CUSTOMER_LOCK_LIMIT);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 200,
  },
  layout: 'horizontal',
  schema: useFormSchema(confType.value),
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
    const data =
      (await formApi.getValues()) as CrmCustomerLimitConfigApi.CustomerLimitConfig;
    try {
      await (formData.value?.id
        ? updateCustomerLimitConfig(data)
        : createCustomerLimitConfig(data));
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
    let data =
      modalApi.getData<CrmCustomerLimitConfigApi.CustomerLimitConfig>();
    if (!data) {
      return;
    }
    if (data.type) {
      confType.value = data.type as LimitConfType;
    }
    formApi.setState({ schema: useFormSchema(confType.value) });
    modalApi.lock();
    try {
      if (data.id) {
        data = await getCustomerLimitConfig(data.id);
      }
      formData.value = data;
      // 设置到 values
      await formApi.setValues(data);
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
