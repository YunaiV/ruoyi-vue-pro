<!-- dall3 -->
<script setup lang="ts">
import type { AiImageApi } from '#/api/ai/image';
import type { AiModelModelApi } from '#/api/ai/model/model';

import { ref } from 'vue';

import { alert, confirm } from '@vben/common-ui';
import {
  AiPlatformEnum,
  ImageHotEnglishWords,
  StableDiffusionClipGuidancePresets,
  StableDiffusionSamplers,
  StableDiffusionStylePresets,
} from '@vben/constants';

import {
  ElButton,
  ElInputNumber,
  ElMessage,
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

function hasChinese(str: string) {
  return /[\u4E00-\u9FA5]/.test(str);
}

// 定义属性
const drawIn = ref<boolean>(false); // 生成中
const selectHotWord = ref<string>(''); // 选中的热词
// 表单
const prompt = ref<string>(''); // 提示词
const width = ref<number>(512); // 图片宽度
const height = ref<number>(512); // 图片高度
const sampler = ref<string>('DDIM'); // 采样方法
const steps = ref<number>(20); // 迭代步数
const seed = ref<number>(42); // 控制生成图像的随机性
const scale = ref<number>(7.5); // 引导系数
const clipGuidancePreset = ref<string>('NONE'); // 文本提示相匹配的图像(clip_guidance_preset) 简称 CLIP
const stylePreset = ref<string>('3d-model'); // 风格

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
  // 从 models 中查找匹配的模型
  const selectModel = 'stable-diffusion-v1-6';
  const matchedModel = props.models.find(
    (item) =>
      item.model === selectModel &&
      item.platform === AiPlatformEnum.STABLE_DIFFUSION,
  );
  if (!matchedModel) {
    ElMessage.error('该模型不可用，请选择其它模型');
    return;
  }

  // 二次确认
  if (hasChinese(prompt.value)) {
    await alert('暂不支持中文！');
    return;
  }
  await confirm(`确认生成内容?`);

  try {
    // 加载中
    drawIn.value = true;
    // 回调
    emits('onDrawStart', AiPlatformEnum.STABLE_DIFFUSION);
    // 发送请求
    const form = {
      modelId: matchedModel.id,
      prompt: prompt.value, // 提示词
      width: width.value, // 图片宽度
      height: height.value, // 图片高度
      options: {
        seed: seed.value, // 随机种子
        steps: steps.value, // 图片生成步数
        scale: scale.value, // 引导系数
        sampler: sampler.value, // 采样算法
        clipGuidancePreset: clipGuidancePreset.value, // 文本提示相匹配的图像 CLIP
        stylePreset: stylePreset.value, // 风格
      },
    } as unknown as AiImageApi.ImageDrawReqVO;
    await drawImage(form);
  } finally {
    // 回调
    emits('onDrawComplete', AiPlatformEnum.STABLE_DIFFUSION);
    // 加载结束
    drawIn.value = false;
  }
}

/** 填充值 */
async function settingValues(detail: AiImageApi.Image) {
  prompt.value = detail.prompt;
  width.value = detail.width;
  height.value = detail.height;
  seed.value = detail.options?.seed;
  steps.value = detail.options?.steps;
  scale.value = detail.options?.scale;
  sampler.value = detail.options?.sampler;
  clipGuidancePreset.value = detail.options?.clipGuidancePreset;
  stylePreset.value = detail.options?.stylePreset;
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

  <!-- 热词区域 -->
  <div class="mt-8 flex flex-col">
    <div><b>随机热词</b></div>
    <ElSpace wrap class="mt-4 flex flex-wrap gap-2">
      <ElButton
        round
        class="m-0"
        :type="selectHotWord === hotWord ? 'primary' : 'default'"
        v-for="hotWord in ImageHotEnglishWords"
        :key="hotWord"
        @click="handleHotWordClick(hotWord)"
      >
        {{ hotWord }}
      </ElButton>
    </ElSpace>
  </div>

  <!-- 参数项：采样方法 -->
  <div class="mt-8">
    <div><b>采样方法</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <ElSelect
        v-model="sampler"
        placeholder="Select"
        size="large"
        class="!w-80"
      >
        <ElOption
          v-for="item in StableDiffusionSamplers"
          :key="item.key"
          :value="item.key"
          :label="item.name"
        >
          {{ item.name }}
        </ElOption>
      </ElSelect>
    </ElSpace>
  </div>

  <!-- CLIP -->
  <div class="mt-8">
    <div><b>CLIP</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <ElSelect
        v-model="clipGuidancePreset"
        placeholder="Select"
        size="large"
        class="!w-80"
      >
        <ElOption
          v-for="item in StableDiffusionClipGuidancePresets"
          :key="item.key"
          :value="item.key"
          :label="item.name"
        >
          {{ item.name }}
        </ElOption>
      </ElSelect>
    </ElSpace>
  </div>

  <!-- 风格 -->
  <div class="mt-8">
    <div><b>风格</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <ElSelect
        v-model="stylePreset"
        placeholder="Select"
        size="large"
        class="!w-80"
      >
        <ElOption
          v-for="item in StableDiffusionStylePresets"
          :key="item.key"
          :label="item.name"
          :value="item.key"
        >
          {{ item.name }}
        </ElOption>
      </ElSelect>
    </ElSpace>
  </div>

  <!-- 图片尺寸 -->
  <div class="mt-8">
    <div><b>图片尺寸</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <div class="flex items-center gap-2">
        <ElInputNumber
          v-model="width"
          placeholder="图片宽度"
          controls-position="right"
          class="!w-32"
        />
      </div>
      <span class="mx-2">×</span>
      <div class="flex items-center gap-2">
        <ElInputNumber
          v-model="height"
          placeholder="图片高度"
          controls-position="right"
          class="!w-32"
        />
      </div>
    </ElSpace>
  </div>

  <!-- 迭代步数 -->
  <div class="mt-8">
    <div><b>迭代步数</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <ElInputNumber
        v-model="steps"
        size="large"
        class="!w-80"
        placeholder="Please input"
        controls-position="right"
      />
    </ElSpace>
  </div>

  <!-- 引导系数 -->
  <div class="mt-8">
    <div><b>引导系数</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <ElInputNumber
        v-model="scale"
        size="large"
        class="!w-80"
        placeholder="Please input"
        controls-position="right"
      />
    </ElSpace>
  </div>

  <!-- 随机因子 -->
  <div class="mt-8">
    <div><b>随机因子</b></div>
    <ElSpace wrap class="mt-4 w-full">
      <ElInputNumber
        v-model="seed"
        size="large"
        class="!w-80"
        placeholder="Please input"
        controls-position="right"
      />
    </ElSpace>
  </div>

  <!-- 生成按钮 -->
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
