<script lang="ts" setup>
import type { MallPointActivityApi } from '#/api/mall/promotion/point';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import {
  createPointActivity,
  getPointActivity,
  updatePointActivity,
} from '#/api/mall/promotion/point';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallPointActivityApi.PointActivity>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['积分活动'])
    : $t('ui.actionTitle.create', ['积分活动']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 120,
  },
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
      (await formApi.getValues()) as MallPointActivityApi.PointActivity;

    // 确保必要的默认值
    if (!data.products) {
      data.products = [];
    }
    if (!data.sort) {
      data.sort = 0;
    }

    try {
      await (formData.value?.id
        ? updatePointActivity(data)
        : createPointActivity(data));
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
    const data = modalApi.getData<MallPointActivityApi.PointActivity>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getPointActivity(data.id);
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
    <div class="p-4">
      <div class="mb-4 rounded border border-yellow-200 bg-yellow-50 p-4">
        <p class="text-yellow-800">
          <strong>注意：</strong>
          积分活动涉及复杂的商品选择和SKU配置，当前为简化版本。
          完整的商品选择和积分配置功能需要在后续版本中完善。
        </p>
      </div>
      <Form />
    </div>
  </Modal>
</template>
