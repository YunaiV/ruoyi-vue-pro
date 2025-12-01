<script lang="ts" setup>
import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Tag } from 'ant-design-vue';
import BenzAMRRecorder from 'benz-amr-recorder'; // 因为微信语音是 amr 格式，所以需要用到 amr 解码器：https://www.npmjs.com/package/benz-amr-recorder

/** 微信消息 - 语音 */
defineOptions({ name: 'WxVoicePlayer' });

const props = withDefaults(
  defineProps<{
    content?: string; // 语音文本
    url: string; // 语音地址，例如说：https://www.iocoder.cn/xxx.amr
  }>(),
  {
    content: '',
  },
);

const amr = ref<any>();
const playing = ref(false);
const duration = ref<number>();

/** 处理点击，播放或暂停 */
function playVoice() {
  // 情况一：未初始化，则创建 BenzAMRRecorder
  if (amr.value === undefined) {
    amrInit();
    return;
  }
  // 情况二：已经初始化，则根据情况播放或暂时
  if (amr.value.isPlaying()) {
    amrStop();
  } else {
    amrPlay();
  }
}

/** 音频初始化 */
function amrInit() {
  amr.value = new BenzAMRRecorder();
  // 设置播放
  amr.value.initWithUrl(props.url).then(() => {
    amrPlay();
    duration.value = amr.value.getDuration();
  });
  // 监听暂停
  amr.value.onEnded(() => {
    playing.value = false;
  });
}

/** 音频播放 */
function amrPlay() {
  playing.value = true;
  amr.value.play();
}

/** 音频暂停 */
function amrStop() {
  playing.value = false;
  amr.value.stop();
}
</script>

<template>
  <div
    class="flex min-h-[50px] min-w-[120px] cursor-pointer flex-col items-center justify-center rounded-[10px] bg-[#eaeaea] p-[8px_12px]"
    @click="playVoice"
  >
    <div class="flex items-center">
      <IconifyIcon
        v-if="playing !== true"
        icon="lucide:circle-play"
        :size="32"
      />
      <IconifyIcon v-else icon="lucide:circle-pause" :size="32" />
      <span v-if="duration" class="ml-2 text-xs">{{ duration }} 秒</span>
    </div>
    <div v-if="content" class="mt-2">
      <Tag color="success">语音识别</Tag>
      {{ content }}
    </div>
  </div>
</template>
