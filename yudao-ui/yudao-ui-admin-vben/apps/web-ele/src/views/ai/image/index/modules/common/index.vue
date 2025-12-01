<!-- dall3 -->
<script setup lang="ts">
import type { AiImageApi } from '#/api/ai/image';
import type { AiModelModelApi } from '#/api/ai/model/model';

import { ref, watch } from 'vue';

import { confirm } from '@vben/common-ui';
import {
  AiPlatformEnum,
  ImageHotWords,
  OtherPlatformEnum,
} from '@vben/constants';

import {
  ElButton,
  ElInputNumber,
  ElOption,
  ElSelect,
  ElSpace,
} from 'element-plus';

import { drawImage } from '#/api/ai/image';

const props = defineProps({
  models: {
    type: Array<AiModelModelApi.Model>,
    default: () => [] as AiModelModelApi.Model[],
  },
}); // 接收父组件传入的模型列表
const emits = defineEmits(['onDrawStart', 'onDrawComplete']);

const drawIn = ref<boolean>(false); // 生成中
const selectHotWord = ref<string>(''); // 选中的热词

const prompt = ref<string>(''); // 提示词
const width = ref<number>(512); // 图片宽度
const height = ref<number>(512); // 图片高度
const otherPlatform = ref<string>(AiPlatformEnum.TONG_YI); // 平台
const platformModels = ref<AiModelModelApi.Model[]>([]); // 模型列表
const modelId = ref<number>(); // 选中的模型

/** 选择热词 */
async function handleHotWordClick(hotWord: string) {
  // 情况一：取消选中
  if (selectHotWord.value === hotWord) {
    selectHotWord.value = '';
    return;
  }
  // 情况二：选中
  selectHotWord.value = hotWord; // 选中
  prompt.value = hotWord; // 替换提示词
}

/** 图片生成 */
async function handleGenerateImage() {
  // 二次确认
  await confirm(`确认生成内容?`);
  try {
    // 加载中
    drawIn.value = true;
    // 回调
    emits('onDrawStart', otherPlatform.value);
    // 发送请求
    const form = {
      platform: otherPlatform.value,
      modelId: modelId.value, // 模型
      prompt: prompt.value, // 提示词
      width: width.value, // 图片宽度
      height: height.value, // 图片高度
      options: {},
    } as unknown as AiImageApi.ImageDrawReqVO;
    await drawImage(form);
  } finally {
    // 回调
    emits('onDrawComplete', otherPlatform.value);
    // 加载结束
    drawIn.value = false;
  }
}

/** 填充值 */
async function settingValues(detail: AiImageApi.Image) {
  prompt.value = detail.prompt;
  width.value = detail.width;
  height.value = detail.height;
}

/** 平台切换 */
async function handlerPlatformChange(platform: any) {
  // 根据选择的平台筛选模型
  platformModels.value = props.models.filter(
    (item: AiModelModelApi.Model) => item.platform === platform,
  );
  // 切换平台，默认选择一个模型
  modelId.value =
    platformModels.value.length > 0 && platformModels.value[0]
      ? platformModels.value[0].id
      : undefined;
}

/** 监听 models 变化 */
watch(
  () => props.models,
  () => {
    handlerPlatformChange(otherPlatform.value);
  },
  { immediate: true, deep: true },
);

defineExpose({ settingValues });
</script>
<template>
  <div class="prompt">
    <b>画面描述</b>
    <p>建议使用"形容词 + 动词 + 风格"的格式，使用"，"隔开</p>
    <el-input
      v-model="prompt"
      :maxlength="1024"
      :rows="5"
      type="textarea"
      class="mt-4 w-full"
      placeholder="例如：童话里的小屋应该是什么样子？"
      show-word-limit
    />
  </div>

  <div class="mt-8 flex flex-col">
    <div>
      <b>随机热词</b>
    </div>
    <ElSpace wrap class="mt-4 flex flex-wrap justify-start">
      <ElButton
        round
        class="m-0"
        :type="selectHotWord === hotWord ? 'primary' : 'default'"
        v-for="hotWord in ImageHotWords"
        :key="hotWord"
        @click="handleHotWordClick(hotWord)"
      >
        {{ hotWord }}
      </ElButton>
    </ElSpace>
  </div>

  <div class="mt-8">
    <div>
      <b>平台</b>
    </div>
    <ElSpace wrap class="mt-4 w-full">
      <ElSelect
        v-model="otherPlatform"
        placeholder="Select"
        size="large"
        class="!w-80"
        @change="handlerPlatformChange"
      >
        <ElOption
          v-for="item in OtherPlatformEnum"
          :key="item.key"
          :value="item.key"
          :label="item.name"
        >
          {{ item.name }}
        </ElOption>
      </ElSelect>
    </ElSpace>
  </div>

  <div class="mt-8">
    <div>
      <b>模型</b>
    </div>
    <ElSpace wrap class="mt-4 w-full">
      <ElSelect
        v-model="modelId"
        placeholder="Select"
        size="large"
        class="!w-80"
      >
        <ElOption
          v-for="item in platformModels"
          :key="item.id"
          :value="item.id"
          :label="item.name"
        >
          {{ item.name }}
        </ElOption>
      </ElSelect>
    </ElSpace>
  </div>

  <div class="mt-8">
    <div>
      <b>图片尺寸</b>
    </div>
    <div class="mt-4 flex items-center gap-2">
      <ElInputNumber
        v-model="width"
        placeholder="图片宽度"
        controls-position="right"
        class="!w-32"
      />
      <span class="mx-2">×</span>
      <ElInputNumber
        v-model="height"
        placeholder="图片高度"
        controls-position="right"
        class="!w-32"
      />
    </div>
  </div>

  <div class="mt-12 flex justify-center">
    <ElButton
      type="primary"
      size="large"
      round
      :loading="drawIn"
      :disabled="prompt.length === 0"
      @click="handleGenerateImage"
    >
      {{ drawIn ? '生成中' : '生成内容' }}
    </ElButton>
  </div>
</template>
