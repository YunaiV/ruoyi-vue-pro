<script lang="ts" setup>
import type { MallDiscountActivityApi } from '#/api/mall/promotion/discount/discountActivity';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createDiscountActivity,
  getDiscountActivity,
  updateDiscountActivity,
} from '#/api/mall/promotion/discount/discountActivity';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'DiscountActivityForm' });

const emit = defineEmits(['success']);
const formData = ref<MallDiscountActivityApi.DiscountActivity>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['限时折扣活动'])
    : $t('ui.actionTitle.create', ['限时折扣活动']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 100,
  },
  wrapperClass: 'grid-cols-2',
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
    const data =
      (await formApi.getValues()) as MallDiscountActivityApi.DiscountActivity;

    // 确保必要的默认值
    if (!data.products) {
      data.products = [];
    }

    try {
      await (formData.value?.id
        ? updateDiscountActivity(data)
        : createDiscountActivity(data));
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
    const data = modalApi.getData<MallDiscountActivityApi.DiscountActivity>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDiscountActivity(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-3/5" :title="getTitle">
    <Form />
  </Modal>
</template>
