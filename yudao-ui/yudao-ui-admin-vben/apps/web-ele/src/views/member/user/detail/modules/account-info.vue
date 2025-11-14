<script setup lang="ts">
import type { MemberUserApi } from '#/api/member/user';
import type { PayWalletApi } from '#/api/pay/wallet/balance';

import { fenToYuan } from '@vben/utils';

import { ElCard } from 'element-plus';

import { useDescription } from '#/components/description';

withDefaults(
  defineProps<{
    mode?: 'kefu' | 'member';
    user: MemberUserApi.User;
    wallet: PayWalletApi.Wallet;
  }>(),
  {
    mode: 'member',
  },
);

const [Descriptions] = useDescription({
  componentProps: {
    border: false,
    column: 2,
    direction: 'horizontal',
    labelWidth: 140,
    title: '',
    extra: '',
  },
  schema: [
    {
      field: 'levelName',
      label: '等级',
      content: (data) => data.levelName || '-',
    },
    {
      field: 'experience',
      label: '成长值',
      content: (data) => data.experience || 0,
    },
    {
      field: 'point',
      label: '当前积分',
      content: (data) => data.point || 0,
    },
    {
      field: 'totalPoint',
      label: '总积分',
      content: (data) => data.totalPoint || 0,
    },
    {
      field: 'balance',
      label: '当前余额',
      content: (data) => fenToYuan(data.balance || 0),
    },
    {
      field: 'totalExpense',
      label: '支出金额',
      content: (data) => fenToYuan(data.totalExpense || 0),
    },
    {
      field: 'totalRecharge',
      label: '充值金额',
      content: (data) => fenToYuan(data.totalRecharge || 0),
    },
  ],
});
</script>

<template>
  <ElCard>
    <template #title>
      <slot name="title"></slot>
    </template>
    <template #extra>
      <slot name="extra"></slot>
    </template>
    <Descriptions
      :column="mode === 'member' ? 2 : 1"
      :data="{
        ...user,
        ...wallet,
      }"
    />
  </ElCard>
</template>
