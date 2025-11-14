<script lang="ts" setup>
import type { MallRewardActivityApi } from '#/api/mall/promotion/reward/rewardActivity';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import {
  createRewardActivity,
  getReward,
  updateRewardActivity,
} from '#/api/mall/promotion/reward/rewardActivity';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallRewardActivityApi.RewardActivity>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['满减送活动'])
    : $t('ui.actionTitle.create', ['满减送活动']);
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
      (await formApi.getValues()) as MallRewardActivityApi.RewardActivity;

    // 确保必要的默认值
    if (!data.rules) {
      data.rules = [];
    }

    try {
      await (formData.value?.id
        ? updateRewardActivity(data)
        : createRewardActivity(data));
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
    const data = modalApi.getData<MallRewardActivityApi.RewardActivity>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getReward(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-4/5" :title="getTitle">
    <Form />

    <!-- 简化说明 -->
    <div class="mt-4 rounded bg-blue-50 p-4">
      <p class="text-sm text-blue-600">
        <strong>说明：</strong> 当前为简化版本的满减送活动表单。
        复杂的商品选择、优惠规则配置等功能已简化，仅保留基础字段配置。
        如需完整功能，请参考原始 Element UI 版本的实现。
      </p>
    </div>
  </Modal>
</template>
