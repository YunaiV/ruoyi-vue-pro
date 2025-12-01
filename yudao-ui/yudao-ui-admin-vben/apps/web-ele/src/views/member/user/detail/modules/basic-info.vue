<script setup lang="ts">
import type { MemberUserApi } from '#/api/member/user';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDate } from '@vben/utils';

import { ElAvatar, ElCard, ElCol, ElRow } from 'element-plus';

import { useDescription } from '#/components/description';
import { DictTag } from '#/components/dict-tag';

const props = withDefaults(
  defineProps<{ mode?: 'kefu' | 'member'; user: MemberUserApi.User }>(),
  {
    mode: 'member',
  },
);

const [Descriptions] = useDescription({
  border: false,
  column: props.mode === 'member' ? 2 : 1,
  schema: [
    {
      field: 'name',
      label: '用户名',
    },
    {
      field: 'nickname',
      label: '昵称',
    },
    {
      field: 'mobile',
      label: '手机号',
    },
    {
      field: 'sex',
      label: '性别',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.SYSTEM_USER_SEX,
          value: val,
        }),
    },
    {
      field: 'areaName',
      label: '所在地',
    },
    {
      field: 'registerIp',
      label: '注册 IP',
    },
    {
      field: 'birthday',
      label: '生日',
      render: (val) => formatDate(val)?.toString() || '-',
    },
    {
      field: 'createTime',
      label: '注册时间',
      render: (val) => formatDate(val)?.toString() || '-',
    },
    {
      field: 'loginDate',
      label: '最后登录时间',
      render: (val) => formatDate(val)?.toString() || '-',
    },
  ],
});
</script>

<template>
  <ElCard>
    <template #header>
      <div class="flex justify-between">
        <span class="font-medium">
          <slot name="title"></slot>
        </span>
        <div class="h-[10px]">
          <slot name="extra"></slot>
        </div>
      </div>
    </template>
    <ElRow v-if="mode === 'member'" :gutter="24">
      <ElCol :span="6">
        <ElAvatar :size="180" shape="square" :src="user.avatar" />
      </ElCol>
      <ElCol :span="18">
        <Descriptions :data="user" />
      </ElCol>
    </ElRow>
    <template v-else-if="mode === 'kefu'">
      <ElAvatar :size="140" shape="square" :src="user.avatar" />
      <Descriptions :data="user" />
    </template>
  </ElCard>
</template>
