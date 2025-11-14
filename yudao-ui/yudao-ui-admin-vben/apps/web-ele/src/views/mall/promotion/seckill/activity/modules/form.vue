<script lang="ts" setup>
import type { MallSeckillActivityApi } from '#/api/mall/promotion/seckill/seckillActivity';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createSeckillActivity,
  getSeckillActivity,
  updateSeckillActivity,
} from '#/api/mall/promotion/seckill/seckillActivity';
import { $t } from '#/locales';

const emit = defineEmits(['success']);
const formData = ref<MallSeckillActivityApi.SeckillActivity>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['秒杀活动'])
    : $t('ui.actionTitle.create', ['秒杀活动']);
});

// 简化的表单配置，实际项目中应该有完整的字段配置
const formSchema = [
  {
    fieldName: 'id',
    component: 'Input',
    dependencies: {
      triggerFields: [''],
      show: () => false,
    },
  },
  {
    fieldName: 'name',
    label: '活动名称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入活动名称',
    },
    rules: 'required',
  },
  {
    fieldName: 'status',
    label: '活动状态',
    component: 'Select',
    componentProps: {
      placeholder: '请选择活动状态',
      options: [
        { label: '开启', value: 0 },
        { label: '关闭', value: 1 },
      ],
    },
    rules: 'required',
  },
];

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: formSchema,
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
      (await formApi.getValues()) as MallSeckillActivityApi.SeckillActivity;
    try {
      await (formData.value?.id
        ? updateSeckillActivity(data)
        : createSeckillActivity(data));
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
    const data = modalApi.getData<MallSeckillActivityApi.SeckillActivity>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getSeckillActivity(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
