<script setup lang="ts">
import type { PropType } from 'vue';

import type { AiImageApi } from '#/api/ai/image';

import { onMounted, ref, toRefs, watch } from 'vue';

import { confirm } from '@vben/common-ui';
import { AiImageStatusEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Button, Card, Image, message } from 'ant-design-vue';

const props = defineProps({
  detail: {
    type: Object as PropType<AiImageApi.Image>,
    default: () => ({}),
  },
});
const emits = defineEmits(['onBtnClick', 'onMjBtnClick']);

const cardImageRef = ref<any>(); // 卡片 image ref

/** 处理点击事件  */
async function handleButtonClick(type: string, detail: AiImageApi.Image) {
  emits('onBtnClick', type, detail);
}

/** 处理 Midjourney 按钮点击事件  */
async function handleMidjourneyBtnClick(
  button: AiImageApi.ImageMidjourneyButtons,
) {
  await confirm(`确认操作 "${button.label} ${button.emoji}" ?`);
  emits('onMjBtnClick', button, props.detail);
}

/** 监听详情 */
const { detail } = toRefs(props);
watch(detail, async (newVal) => {
  await handleLoading(newVal.status);
});
const loading = ref();

/** 处理加载状态 */
async function handleLoading(status: number) {
  // 情况一：如果是生成中，则设置加载中的 loading
  if (status === AiImageStatusEnum.IN_PROGRESS) {
    loading.value = message.loading({
      content: `生成中...`,
    });
  } else {
    // 情况二：如果已经生成结束，则移除 loading
    if (loading.value) {
      setTimeout(loading.value, 100);
    }
  }
}

/** 初始化 */
onMounted(async () => {
  await handleLoading(props.detail.status);
});
</script>
<template>
  <Card class="relative flex h-auto w-80 flex-col rounded-lg">
    <!-- 图片操作区 -->
    <div class="flex flex-row justify-between">
      <div>
        <Button v-if="detail?.status === AiImageStatusEnum.IN_PROGRESS">
          生成中
        </Button>
        <Button v-else-if="detail?.status === AiImageStatusEnum.SUCCESS">
          已完成
        </Button>
        <Button danger v-else-if="detail?.status === AiImageStatusEnum.FAIL">
          异常
        </Button>
      </div>
      <div class="flex">
        <!-- TODO @AI：居右对齐 -->
        <Button
          class="m-0 p-2"
          type="text"
          @click="handleButtonClick('download', detail)"
        >
          <IconifyIcon icon="lucide:download" />
        </Button>
        <Button
          class="m-0 p-2"
          type="text"
          @click="handleButtonClick('regeneration', detail)"
        >
          <IconifyIcon icon="lucide:refresh-cw" />
        </Button>
        <Button
          class="m-0 p-2"
          type="text"
          @click="handleButtonClick('delete', detail)"
        >
          <IconifyIcon icon="lucide:trash" />
        </Button>
        <Button
          class="m-0 p-2"
          type="text"
          @click="handleButtonClick('more', detail)"
        >
          <IconifyIcon icon="lucide:ellipsis-vertical" />
        </Button>
      </div>
    </div>

    <!-- 图片展示区域 -->
    <div class="mt-5 h-72 flex-1 overflow-hidden" ref="cardImageRef">
      <Image class="w-full rounded-lg" :src="detail?.picUrl" />
      <div v-if="detail?.status === AiImageStatusEnum.FAIL">
        {{ detail?.errorMessage }}
      </div>
    </div>

    <!-- Midjourney 专属操作按钮 -->
    <div class="mt-2 flex w-full flex-wrap justify-start">
      <Button
        size="small"
        v-for="(button, index) in detail?.buttons"
        :key="index"
        class="m-2 ml-0 min-w-10"
        @click="handleMidjourneyBtnClick(button)"
      >
        {{ button.label }}{{ button.emoji }}
      </Button>
    </div>
  </Card>
</template>
