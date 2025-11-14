<script setup lang="ts">
import type {
  CaptchaVerifyPassingData,
  SliderCaptchaActionType,
  SliderRotateCaptchaProps,
  SliderRotateVerifyPassingData,
} from '../types';

import { computed, reactive, unref, useTemplateRef, watch } from 'vue';

import { $t } from '@vben/locales';

import { useTimeoutFn } from '@vueuse/core';

import SliderCaptcha from '../slider-captcha/index.vue';

const props = withDefaults(defineProps<SliderRotateCaptchaProps>(), {
  defaultTip: '',
  diffDegree: 20,
  imageSize: 260,
  maxDegree: 300,
  minDegree: 120,
  src: '',
});

const emit = defineEmits<{
  success: [CaptchaVerifyPassingData];
}>();

const slideBarRef = useTemplateRef<SliderCaptchaActionType>('slideBarRef');

const state = reactive({
  currentRotate: 0,
  dragging: false,
  endTime: 0,
  imgStyle: {},
  isPassing: false,
  randomRotate: 0,
  showTip: false,
  startTime: 0,
  toOrigin: false,
});

const modalValue = defineModel<boolean>({ default: false });

watch(
  () => state.isPassing,
  (isPassing) => {
    if (isPassing) {
      const { endTime, startTime } = state;
      const time = (endTime - startTime) / 1000;
      emit('success', { isPassing, time: time.toFixed(1) });
    }
    modalValue.value = isPassing;
  },
);

const getImgWrapStyleRef = computed(() => {
  const { imageSize, imageWrapperStyle } = props;
  return {
    height: `${imageSize}px`,
    width: `${imageSize}px`,
    ...imageWrapperStyle,
  };
});

const getFactorRef = computed(() => {
  const { maxDegree, minDegree } = props;
  if (minDegree > maxDegree) {
    console.warn('minDegree should not be greater than maxDegree');
  }

  if (minDegree === maxDegree) {
    return Math.floor(1 + Math.random() * 1) / 10 + 1;
  }
  return 1;
});

function handleStart() {
  state.startTime = Date.now();
}

function handleDragBarMove(data: SliderRotateVerifyPassingData) {
  state.dragging = true;
  const { imageSize, maxDegree } = props;
  const { moveX } = data;
  const denominator = imageSize!;
  if (denominator === 0) {
    return;
  }
  const currentRotate = Math.ceil(
    (moveX / denominator) * 1.5 * maxDegree! * unref(getFactorRef),
  );
  state.currentRotate = currentRotate;
  setImgRotate(state.randomRotate - currentRotate);
}

function handleImgOnLoad() {
  const { maxDegree, minDegree } = props;
  const ranRotate = Math.floor(
    minDegree! + Math.random() * (maxDegree! - minDegree!),
  ); // 生成随机角度
  state.randomRotate = ranRotate;
  setImgRotate(ranRotate);
}

function handleDragEnd() {
  const { currentRotate, randomRotate } = state;
  const { diffDegree } = props;

  if (Math.abs(randomRotate - currentRotate) >= (diffDegree || 20)) {
    setImgRotate(randomRotate);
    state.toOrigin = true;
    useTimeoutFn(() => {
      state.toOrigin = false;
      state.showTip = true;
      //  时间与动画时间保持一致
    }, 300);
  } else {
    checkPass();
  }
  state.showTip = true;
  state.dragging = false;
}

function setImgRotate(deg: number) {
  state.imgStyle = {
    transform: `rotateZ(${deg}deg)`,
  };
}

function checkPass() {
  state.isPassing = true;
  state.endTime = Date.now();
}

function resume() {
  state.showTip = false;
  const basicEl = unref(slideBarRef);
  if (!basicEl) {
    return;
  }
  state.isPassing = false;

  basicEl.resume();
  handleImgOnLoad();
}

const imgCls = computed(() => {
  return state.toOrigin ? ['transition-transform duration-300'] : [];
});

const verifyTip = computed(() => {
  return state.isPassing
    ? $t('ui.captcha.sliderRotateSuccessTip', [
        ((state.endTime - state.startTime) / 1000).toFixed(1),
      ])
    : $t('ui.captcha.sliderRotateFailTip');
});

defineExpose({
  resume,
});
</script>

<template>
  <div class="relative flex flex-col items-center">
    <div
      :style="getImgWrapStyleRef"
      class="border-border relative cursor-pointer overflow-hidden rounded-full border shadow-md"
    >
      <img
        :class="imgCls"
        :src="src"
        :style="state.imgStyle"
        alt="verify"
        class="w-full rounded-full"
        @click="resume"
        @load="handleImgOnLoad"
      />
      <div
        class="absolute bottom-3 left-0 z-10 block h-7 w-full text-center text-xs leading-[30px] text-white"
      >
        <div
          v-if="state.showTip"
          :class="{
            'bg-success/80': state.isPassing,
            'bg-destructive/80': !state.isPassing,
          }"
        >
          {{ verifyTip }}
        </div>
        <div v-if="!state.dragging" class="bg-black/30">
          {{ defaultTip || $t('ui.captcha.sliderRotateDefaultTip') }}
        </div>
      </div>
    </div>

    <SliderCaptcha
      ref="slideBarRef"
      v-model="modalValue"
      class="mt-5"
      is-slot
      @end="handleDragEnd"
      @move="handleDragBarMove"
      @start="handleStart"
    >
      <template v-for="(_, key) in $slots" :key="key" #[key]="slotProps">
        <slot :name="key" v-bind="slotProps"></slot>
      </template>
    </SliderCaptcha>
  </div>
</template>
