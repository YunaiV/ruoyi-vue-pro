<script setup lang="ts">
import type { MemberUserApi } from '#/api/member/user';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { formatDate } from '@vben/utils';

import { Avatar, Card, Col, Row } from 'ant-design-vue';

import { useDescription } from '#/components/description';
import { DictTag } from '#/components/dict-tag';

const props = withDefaults(
  defineProps<{ mode?: 'kefu' | 'member'; user: MemberUserApi.User }>(),
  {
    mode: 'member',
  },
);

const [Descriptions] = useDescription({
  bordered: false,
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
  <Card>
    <template #title>
      <slot name="title"></slot>
    </template>
    <template #extra>
      <slot name="extra"></slot>
    </template>
    <Row v-if="mode === 'member'" :gutter="24">
      <Col :span="6">
        <Avatar :size="180" shape="square" :src="user.avatar" />
      </Col>
      <Col :span="18">
        <Descriptions :column="2" :data="user" />
      </Col>
    </Row>
    <template v-else-if="mode === 'kefu'">
      <Avatar :size="140" shape="square" :src="user.avatar" />
      <Descriptions :column="1" :data="user" />
    </template>
  </Card>
</template>
