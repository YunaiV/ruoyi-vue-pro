<!-- dall3 -->
<script setup lang="ts">
import type { ImageModel, ImageSize } from '@vben/constants';

import type { AiImageApi } from '#/api/ai/image';
import type { AiModelModelApi } from '#/api/ai/model/model';

import { ref } from 'vue';

import { confirm } from '@vben/common-ui';
import {
  AiPlatformEnum,
  Dall3Models,
  Dall3SizeList,
  Dall3StyleList,
  ImageHotWords,
} from '@vben/constants';

import { ElButton, ElImage, ElMessage, ElSpace } from 'element-plus';

import { drawImage } from '#/api/ai/image';

const props = defineProps({
  models: {
    type: Array<AiModelModelApi.Model>,
    default: () => [] as AiModelModelApi.Model[],
  },
}); // 接收父组件传入的模型列表
const emits = defineEmits(['onDrawStart', 'onDrawComplete']);

const prompt = ref<string>(''); // 提示词
const drawIn = ref<boolean>(false); // 生成中
const selectHotWord = ref<string>(''); // 选中的热词
const selectModel = ref<string>('dall-e-3'); // 模型
const selectSize = ref<string>('1024x1024'); // 选中 size
const style = ref<string>('vivid'); // style 样式

/** 选择热词 */
async function handleHotWordClick(hotWord: string) {
  // 情况一：取消选中
  if (selectHotWord.value === hotWord) {
    selectHotWord.value = '';
    return;
  }
  // 情况二：选中
  selectHotWord.value = hotWord;
  prompt.value = hotWord;
}

/** 选择 model 模型 */
async function handleModelClick(model: ImageModel) {
  selectModel.value = model.key;
  // 可以在这里添加模型特定的处理逻辑
  // 例如，如果未来需要根据不同模型设置不同参数
  if (model.key === 'dall-e-3') {
    // DALL-E-3 模型特定的处理
    style.value = 'vivid'; // 默认设置vivid风格
  } else if (model.key === 'dall-e-2') {
    // DALL-E-2 模型特定的处理
    style.value = 'natural'; // 如果有其他DALL-E-2适合的默认风格
  }

  // 更新其他相关参数
  // 例如可以默认选择最适合当前模型的尺寸
  const recommendedSize = Dall3SizeList.find(
    (size) =>
      (model.key === 'dall-e-3' && size.key === '1024x1024') ||
      (model.key === 'dall-e-2' && size.key === '512x512'),
  );

  if (recommendedSize) {
    selectSize.value = recommendedSize.key;
  }
}

/** 选择 style 样式  */
async function handleStyleClick(imageStyle: ImageModel) {
  style.value = imageStyle.key;
}

/** 选择 size 大小  */
async function handleSizeClick(imageSize: ImageSize) {
  selectSize.value = imageSize.key;
}

/**  图片生产  */
async function handleGenerateImage() {
  // 从 models 中查找匹配的模型
  const matchedModel = props.models.find(
    (item) =>
      item.model === selectModel.value &&
      item.platform === AiPlatformEnum.OPENAI,
  );
  if (!matchedModel) {
    ElMessage.error('该模型不可用，请选择其它模型');
    return;
  }

  // 二次确认
  await confirm(`确认生成内容?`);
  try {
    // 加载中
    drawIn.value = true;
    // 回调
    emits('onDrawStart', AiPlatformEnum.OPENAI);
    const imageSize = Dall3SizeList.find(
      (item) => item.key === selectSize.value,
    ) as ImageSize;
    const form = {
      platform: AiPlatformEnum.OPENAI,
      prompt: prompt.value, // 提示词
      modelId: matchedModel.id, // 使用匹配到的模型
      style: style.value, // 图像生成的风格
      width: imageSize.width, // size 不能为空
      height: imageSize.height, // size 不能为空
      options: {
        style: style.value, // 图像生成的风格
      },
    } as AiImageApi.ImageDrawReqVO;
    // 发送请求
    await drawImage(form);
  } finally {
    // 回调
    emits('onDrawComplete', AiPlatformEnum.OPENAI);
    // 加载结束
    drawIn.value = false;
  }
}

/** 填充值 */
async function settingValues(detail: AiImageApi.Image) {
  prompt.value = detail.prompt;
  selectModel.value = detail.model;
  style.value = detail.options?.style;
  const imageSize = Dall3SizeList.find(
    (item) => item.key === `${detail.width}x${detail.height}`,
  ) as ImageSize;
  await handleSizeClick(imageSize);
}

/** 暴露组件方法 */
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
    <div><b>随机热词</b></div>
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
    <div><b>模型选择</b></div>
    <ElSpace wrap class="mt-4 flex flex-wrap gap-2">
      <div
        class="flex w-28 cursor-pointer flex-col items-center overflow-hidden rounded-lg border-2"
        :class="[
          selectModel === model.key ? '!border-blue-500' : 'border-transparent',
        ]"
        v-for="model in Dall3Models"
        :key="model.key"
        @click="handleModelClick(model)"
      >
        <ElImage
          :preview-src-list="[]"
          :src="model.image"
          fit="contain"
          class="w-full"
        />
        <div class="text-sm font-bold text-gray-600">
          {{ model.name }}
        </div>
      </div>
    </ElSpace>
  </div>

  <div class="mt-8">
    <div><b>风格选择</b></div>
    <ElSpace wrap class="mt-4 flex flex-wrap gap-2">
      <div
        class="flex w-28 cursor-pointer flex-col items-center overflow-hidden rounded-lg border-2"
        :class="[
          style === imageStyle.key ? 'border-blue-500' : 'border-transparent',
        ]"
        v-for="imageStyle in Dall3StyleList"
        :key="imageStyle.key"
      >
        <ElImage
          :preview-src-list="[]"
          :src="imageStyle.image"
          fit="contain"
          @click="handleStyleClick(imageStyle)"
        />
        <div class="text-sm font-bold text-gray-600">
          {{ imageStyle.name }}
        </div>
      </div>
    </ElSpace>
  </div>

  <div class="mt-8 w-full">
    <div><b>画面比例</b></div>
    <ElSpace wrap class="mt-5 flex w-full flex-wrap gap-2">
      <div
        class="flex cursor-pointer flex-col items-center"
        v-for="imageSize in Dall3SizeList"
        :key="imageSize.key"
        @click="handleSizeClick(imageSize)"
      >
        <div
          class="flex h-12 w-12 flex-col items-center justify-center rounded-lg border bg-card p-0"
          :class="[
            selectSize === imageSize.key ? 'border-blue-500' : 'border-white',
          ]"
        >
          <div :style="imageSize.style"></div>
        </div>
        <div class="text-sm font-bold text-gray-600">
          {{ imageSize.name }}
        </div>
      </div>
    </ElSpace>
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
