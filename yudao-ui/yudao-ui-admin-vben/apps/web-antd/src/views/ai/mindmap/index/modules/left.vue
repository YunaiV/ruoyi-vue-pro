<script setup lang="ts">
import { reactive, ref } from 'vue';

import { MindMapContentExample } from '@vben/constants';

import { Button, Textarea } from 'ant-design-vue';

defineProps<{
  isGenerating: boolean;
}>();

const emits = defineEmits(['submit', 'directGenerate']);

const formData = reactive({
  prompt: '',
});

const generatedContent = ref(MindMapContentExample); // 已有的内容

defineExpose({
  setGeneratedContent(newContent: string) {
    // 设置已有的内容，在生成结束的时候将结果赋值给该值
    generatedContent.value = newContent;
  },
});
</script>
<template>
  <div class="flex w-80 flex-col rounded-lg bg-card p-5">
    <h3 class="h-7 w-full text-center text-xl leading-7 text-primary">
      思维导图创作中心
    </h3>
    <div class="mt-4 flex-grow overflow-y-auto">
      <div>
        <b>您的需求？</b>
        <Textarea
          v-model:value="formData.prompt"
          :maxlength="1024"
          :rows="8"
          class="mt-4 w-full"
          placeholder="请输入提示词，让AI帮你完善"
          show-count
        />
        <Button
          class="mt-4 !w-full"
          type="primary"
          :loading="isGenerating"
          @click="emits('submit', formData)"
        >
          智能生成思维导图
        </Button>
      </div>
      <div class="mt-7">
        <b>使用已有内容生成？</b>
        <Textarea
          v-model:value="generatedContent"
          :maxlength="1024"
          :rows="8"
          class="mt-4 w-full"
          placeholder="例如：童话里的小屋应该是什么样子？"
          show-count
        />
        <Button
          class="mt-4 !w-full"
          type="primary"
          @click="emits('directGenerate', generatedContent)"
          :disabled="isGenerating"
        >
          直接生成
        </Button>
      </div>
    </div>
  </div>
</template>
