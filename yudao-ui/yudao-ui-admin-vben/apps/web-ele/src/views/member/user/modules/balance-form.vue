<script lang="ts" setup>
import type { MemberUserApi } from '#/api/member/user';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { fenToYuan, yuanToFen } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { getUser } from '#/api/member/user';
import { getWallet, updateWalletBalance } from '#/api/pay/wallet/balance';
import { $t } from '#/locales';

import { useBalanceFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref({
  id: 0,
  nickname: '',
  balance: '0',
  changeBalance: 0,
  changeType: 1,
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
  schema: useBalanceFormSchema(),
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
    const data = await formApi.getValues();
    try {
      await updateWalletBalance({
        userId: data.id,
        balance: yuanToFen(data.changeBalance) * data.changeType,
      });
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
      return;
    }
    // 加载数据
    const data = modalApi.getData<MemberUserApi.User>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      const user = await getUser(data.id);
      if (!user || !user.id) {
        return;
      }
      const wallet = await getWallet({ userId: user.id });
      formData.value.id = user.id;
      formData.value.nickname = user.nickname || '';
      formData.value.balance = fenToYuan(wallet.balance);
      formData.value.changeType = 1; // 默认增加余额
      formData.value.changeBalance = 0; // 变动余额默认 0
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-1/3" :title="$t('ui.actionTitle.edit', ['用户余额'])">
    <Form class="mx-4" />
  </Modal>
</template>
