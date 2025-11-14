<script lang="ts" setup>
import type { MallCombinationActivityApi } from '#/api/mall/promotion/combination/combinationActivity';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createCombinationActivity,
  getCombinationActivity,
  updateCombinationActivity,
} from '#/api/mall/promotion/combination/combinationActivity';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

defineOptions({ name: 'CombinationActivityForm' });

const emit = defineEmits(['success']);
const formData = ref<MallCombinationActivityApi.CombinationActivity>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['拼团活动'])
    : $t('ui.actionTitle.create', ['拼团活动']);
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
      (await formApi.getValues()) as MallCombinationActivityApi.CombinationActivity;
    try {
      await (formData.value?.id
        ? updateCombinationActivity(data)
        : createCombinationActivity(data));
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
    const data =
      modalApi.getData<MallCombinationActivityApi.CombinationActivity>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getCombinationActivity(data.id);
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
