<script lang="ts" setup>
import type { AiWriteApi } from '#/api/ai/write';

import { nextTick, ref } from 'vue';

import { Page } from '@vben/common-ui';
import { WriteExample } from '@vben/constants';

import { ElMessage } from 'element-plus';

import { writeStream } from '#/api/ai/write';

import Left from './modules/left.vue';
import Right from './modules/right.vue';

const writeResult = ref(''); // 写作结果
const isWriting = ref(false); // 是否正在写作中
const abortController = ref<AbortController>(); // // 写作进行中 abort 控制器(控制 stream 写作)

const rightRef = ref<InstanceType<typeof Right>>(); // 写作面板

/** 提交写作 */
function handleSubmit(data: Partial<AiWriteApi.Write>) {
  abortController.value = new AbortController();
  writeResult.value = '';
  isWriting.value = true;
  writeStream({
    data,
    onMessage: async (res: any) => {
      const { code, data, msg } = JSON.parse(res.data);
      if (code !== 0) {
        ElMessage.error(`写作异常! ${msg}`);
        handleStopStream();
        return;
      }
      writeResult.value = writeResult.value + data;
      // 滚动到底部
      await nextTick();
      rightRef.value?.scrollToBottom();
    },
    ctrl: abortController.value,
    onClose: handleStopStream,
    onError: (error: any) => {
      console.error('写作异常', error);
      handleStopStream();
      // 需要抛出异常，禁止重试
      throw error;
    },
  });
}

/** 停止 stream 生成 */
function handleStopStream() {
  abortController.value?.abort();
  isWriting.value = false;
}

/** 点击示例触发 */
function handleExampleClick(type: keyof typeof WriteExample) {
  writeResult.value = WriteExample[type].data;
}

/** 点击重置的时候清空写作的结果*/
function handleReset() {
  writeResult.value = '';
}
</script>

<template>
  <Page auto-content-height>
    <div class="absolute bottom-0 left-0 right-0 top-0 m-4 flex">
      <Left
        :is-writing="isWriting"
        class="mr-4 h-full rounded-lg"
        @submit="handleSubmit"
        @reset="handleReset"
        @example="handleExampleClick"
      />
      <Right
        :is-writing="isWriting"
        @stop-stream="handleStopStream"
        ref="rightRef"
        class="flex-grow"
        v-model:content="writeResult"
      />
    </div>
  </Page>
</template>
