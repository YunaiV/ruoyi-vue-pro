<script lang="ts" setup>
import type { MallKefuMessageApi } from '#/api/mall/promotion/kefu/message';

import { UserTypeEnum } from '@vben/constants';

/** 消息组件 */

defineProps<{
  message: MallKefuMessageApi.Message;
}>();
</script>

<template>
  <div
    :class="[
      message.senderType === UserTypeEnum.MEMBER
        ? `ml-2`
        : message.senderType === UserTypeEnum.ADMIN
          ? `mr-2`
          : '',
    ]"
  >
    <slot></slot>
  </div>
</template>
