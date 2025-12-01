<script lang="ts" setup>
import type { MpUserApi } from '#/api/mp/user/index';

import { preferences } from '@vben/preferences';
import { formatDateTime } from '@vben/utils';

import Msg from './msg.vue';

defineOptions({ name: 'MsgList' });

const props = defineProps<{
  accountId: number;
  list: any[];
  user: Partial<MpUserApi.User>;
}>();

const SendFrom = {
  MpBot: 2,
  User: 1,
} as const; // 发送来源

function getAvatar(sendFrom: number) {
  return sendFrom === SendFrom.User
    ? props.user.avatar
    : preferences.app.defaultAvatar;
}

function getNickname(sendFrom: number) {
  return sendFrom === SendFrom.User ? props.user.nickname : '公众号';
}
</script>
<template>
  <div class="execution" v-for="item in props.list" :key="item.id">
    <div
      class="mb-[30px] flex items-start"
      :class="{ 'flex-row-reverse': item.sendFrom === SendFrom.MpBot }"
    >
      <div class="flex w-20 flex-col items-center text-center">
        <img
          :src="getAvatar(item.sendFrom)"
          class="mb-2 h-12 w-12 rounded-full border border-transparent object-cover"
        />
        <div class="text-sm font-semibold text-[#999]">
          {{ getNickname(item.sendFrom) }}
        </div>
      </div>
      <div class="relative mx-2 flex-1 rounded-[5px] border border-[#dedede]">
        <span
          v-if="item.sendFrom === SendFrom.MpBot"
          class="pointer-events-none absolute -left-2 top-[10px] h-0 w-0 border-y-[8px] border-r-[8px] border-y-transparent border-r-[transparent]"
          :class="{
            '-right-2 left-auto border-l-[8px] border-r-0 border-l-[#dedede]':
              item.sendFrom === SendFrom.MpBot,
          }"
        ></span>
        <span
          v-if="item.sendFrom === SendFrom.User"
          class="pointer-events-none absolute -left-[7px] top-[10px] h-0 w-0 border-y-[8px] border-r-[8px] border-y-transparent border-r-[#f8f8f8]"
          :class="{
            '-right-[7px] left-auto border-l-[8px] border-r-0 border-l-[#f8f8f8]':
              item.sendFrom === SendFrom.MpBot,
          }"
        ></span>
        <div
          class="flex items-center justify-between rounded-t-[5px] border-b border-[#eee] bg-[#f8f8f8] px-[15px] py-[5px]"
        >
          <div class="text-xs text-gray-500">
            {{ formatDateTime(item.createTime) }}
          </div>
        </div>
        <div
          class="overflow-hidden rounded-b-[5px] p-[15px] text-sm text-[#333]"
          :class="
            item.sendFrom === SendFrom.MpBot ? 'bg-[#6BED72]' : 'bg-white'
          "
        >
          <Msg :item="item" />
        </div>
      </div>
    </div>
  </div>
</template>
