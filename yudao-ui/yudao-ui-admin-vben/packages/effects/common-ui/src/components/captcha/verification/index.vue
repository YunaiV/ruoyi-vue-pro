<script setup lang="ts">
/**
 * Verify 验证码组件
 * @description 分发验证码使用
 */
import type { VerificationProps } from './typing';

import { defineAsyncComponent, markRaw, ref, toRefs, watchEffect } from 'vue';

import { IconifyIcon } from '@vben/icons';

import './verify.css';

defineOptions({
  name: 'Verification',
});

const props = withDefaults(defineProps<VerificationProps>(), {
  arith: 0,
  barSize: () => ({
    height: '40px',
    width: '310px',
  }),
  blockSize: () => ({
    height: '50px',
    width: '50px',
  }),
  captchaType: 'blockPuzzle',
  explain: '',
  figure: 0,
  imgSize: () => ({
    height: '155px',
    width: '310px',
  }),
  mode: 'fixed',
  space: 5,
});

const emit = defineEmits(['onSuccess', 'onError', 'onClose', 'onReady']);

const VerifyPoints = defineAsyncComponent(() => import('./verify-points.vue'));
const VerifySlide = defineAsyncComponent(() => import('./verify-slide.vue'));

const { captchaType, mode, checkCaptchaApi, getCaptchaApi } = toRefs(props);
const verifyType = ref();
const componentType = ref();

const instance = ref<InstanceType<typeof VerifyPoints | typeof VerifySlide>>();

const showBox = ref(false);

/**
 * refresh
 * @description 刷新
 */
const refresh = () => {
  if (instance.value && instance.value.refresh) instance.value.refresh();
};

const show = () => {
  if (mode.value === 'pop') showBox.value = true;
};

const onError = (proxy: any) => {
  emit('onError', proxy);
  refresh();
};

const onReady = (proxy: any) => {
  emit('onReady', proxy);
  refresh();
};

const onClose = () => {
  emit('onClose');
  showBox.value = false;
};

const onSuccess = (data: any) => {
  emit('onSuccess', data);
};

watchEffect(() => {
  switch (captchaType.value) {
    case 'blockPuzzle': {
      verifyType.value = '2';
      componentType.value = markRaw(VerifySlide);
      break;
    }
    case 'clickWord': {
      verifyType.value = '';
      componentType.value = markRaw(VerifyPoints);
      break;
    }
  }
});

defineExpose({
  onClose,
  onError,
  onReady,
  onSuccess,
  show,
  refresh,
});
</script>

<template>
  <div v-show="showBox">
    <div
      :class="mode === 'pop' ? 'verifybox' : ''"
      :style="{ 'max-width': `${parseInt(imgSize.width) + 20}px` }"
    >
      <div v-if="mode === 'pop'" class="verifybox-top">
        {{ $t('ui.captcha.title') }}
        <span class="verifybox-close" @click="onClose">
          <IconifyIcon icon="lucide:x" class="size-5" />
        </span>
      </div>
      <div
        :style="{ padding: mode === 'pop' ? '10px' : '0' }"
        class="verifybox-bottom"
      >
        <component
          :is="componentType"
          v-if="componentType"
          ref="instance"
          :arith="arith"
          :bar-size="barSize"
          :block-size="blockSize"
          :captcha-type="captchaType"
          :check-captcha-api="checkCaptchaApi"
          :explain="explain"
          :figure="figure"
          :get-captcha-api="getCaptchaApi"
          :img-size="imgSize"
          :mode="mode"
          :space="space"
          :type="verifyType"
          @on-close="onClose"
          @on-error="onError"
          @on-ready="onReady"
          @on-success="onSuccess"
        />
      </div>
    </div>
  </div>
</template>
