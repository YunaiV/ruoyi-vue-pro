<script lang="ts" setup>
import { MpMsgType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import {
  WxLocation,
  WxMusic,
  WxNews,
  WxVideoPlayer,
  WxVoicePlayer,
} from '#/views/mp/components';

import MsgEvent from './msg-event.vue';

defineOptions({ name: 'WxMsg' });

withDefaults(
  defineProps<{
    item?: any;
  }>(),
  {
    item: {},
  },
);
</script>

<template>
  <div>
    <MsgEvent v-if="item.type === MpMsgType.Event" :item="item" />

    <div v-else-if="item.type === MpMsgType.Text">{{ item.content }}</div>

    <div v-else-if="item.type === MpMsgType.Voice">
      <WxVoicePlayer :url="item.mediaUrl" :content="item.recognition" />
    </div>

    <div v-else-if="item.type === MpMsgType.Image">
      <a :href="item.mediaUrl" target="_blank">
        <img :src="item.mediaUrl" class="w-[100px]" alt="图片消息" />
      </a>
    </div>

    <div
      v-else-if="item.type === MpMsgType.Video || item.type === 'shortvideo'"
      class="text-center"
    >
      <WxVideoPlayer :url="item.mediaUrl" />
    </div>

    <div v-else-if="item.type === MpMsgType.Link" class="flex flex-col gap-2">
      <a :href="item.url" target="_blank" class="text-success no-underline">
        <div class="flex items-center text-sm font-medium text-[#52c41a]">
          <IconifyIcon icon="lucide:link" class="mr-1" />
          {{ item.title }}
        </div>
      </a>
      <div class="text-xs text-[#666]">{{ item.description }}</div>
    </div>

    <div v-else-if="item.type === MpMsgType.Location">
      <WxLocation
        :label="item.label"
        :location-y="item.locationY"
        :location-x="item.locationX"
      />
    </div>

    <div v-else-if="item.type === MpMsgType.News" class="w-[300px]">
      <WxNews :articles="item.articles" />
    </div>

    <div v-else-if="item.type === MpMsgType.Music">
      <WxMusic
        :title="item.title"
        :description="item.description"
        :thumb-media-url="item.thumbMediaUrl"
        :music-url="item.musicUrl"
        :hq-music-url="item.hqMusicUrl"
      />
    </div>
  </div>
</template>
