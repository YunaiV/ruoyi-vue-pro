<script setup lang="ts">
import { Button } from 'ant-design-vue';

const emits = defineEmits(['onNewConversation']);

/** 新建 conversation 聊天对话 */
function handlerNewChat() {
  emits('onNewConversation');
}
</script>

<template>
  <div class="flex h-full w-full flex-row justify-center">
    <div class="flex flex-col justify-center">
      <div class="text-center text-sm text-gray-400">
        点击下方按钮，开始你的对话吧
      </div>
      <div class="mt-5 flex flex-row justify-center">
        <Button type="primary" round @click="handlerNewChat">新建对话</Button>
      </div>
    </div>
  </div>
</template>
