<script lang="ts" setup>
import type { WalletRechargePackageApi } from '#/api/pay/wallet/rechargePackage';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { fenToYuan, yuanToFen } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createWalletRechargePackage,
  getWalletRechargePackage,
  updateWalletRechargePackage,
} from '#/api/pay/wallet/rechargePackage';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<WalletRechargePackageApi.WalletRechargePackage>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['充值套餐'])
    : $t('ui.actionTitle.create', ['充值套餐']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
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
      (await formApi.getValues()) as WalletRechargePackageApi.WalletRechargePackage;
    try {
      // 转换金额单位
      data.payPrice = yuanToFen(data.payPrice);
      data.bonusPrice = yuanToFen(data.bonusPrice);
      await (formData.value?.id
        ? updateWalletRechargePackage(data)
        : createWalletRechargePackage(data));
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
      modalApi.getData<WalletRechargePackageApi.WalletRechargePackage>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getWalletRechargePackage(data.id);
      // 转换金额单位
      formData.value.payPrice = Number.parseFloat(
        fenToYuan(formData.value.payPrice),
      );
      formData.value.bonusPrice = Number.parseFloat(
        fenToYuan(formData.value.bonusPrice),
      );
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-1/4">
    <Form class="mx-4" />
  </Modal>
</template>
