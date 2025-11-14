<script setup lang="ts">
import type {
  CaptchaVerifyPassingData,
  SliderCaptchaActionType,
  SliderRotateVerifyPassingData,
  SliderTranslateCaptchaProps,
} from '../types';

import {
  computed,
  onMounted,
  reactive,
  ref,
  unref,
  useTemplateRef,
  watch,
} from 'vue';

import { $t } from '@vben/locales';

import SliderCaptcha from '../slider-captcha/index.vue';

const props = withDefaults(defineProps<SliderTranslateCaptchaProps>(), {
  defaultTip: '',
  canvasWidth: 420,
  canvasHeight: 280,
  squareLength: 42,
  circleRadius: 10,
  src: '',
  diffDistance: 3,
});

const emit = defineEmits<{
  success: [CaptchaVerifyPassingData];
}>();

const PI: number = Math.PI;
enum CanvasOpr {
  // eslint-disable-next-line no-unused-vars
  Clip = 'clip',
  // eslint-disable-next-line no-unused-vars
  Fill = 'fill',
}

const modalValue = defineModel<boolean>({ default: false });

const slideBarRef = useTemplateRef<SliderCaptchaActionType>('slideBarRef');
const puzzleCanvasRef = useTemplateRef<HTMLCanvasElement>('puzzleCanvasRef');
const pieceCanvasRef = useTemplateRef<HTMLCanvasElement>('pieceCanvasRef');

const state = reactive({
  dragging: false,
  startTime: 0,
  endTime: 0,
  pieceX: 0,
  pieceY: 0,
  moveDistance: 0,
  isPassing: false,
  showTip: false,
});

const left = ref('0');

const pieceStyle = computed(() => {
  return {
    left: left.value,
  };
});

function setLeft(val: string) {
  left.value = val;
}

const verifyTip = computed(() => {
  return state.isPassing
    ? $t('ui.captcha.sliderTranslateSuccessTip', [
        ((state.endTime - state.startTime) / 1000).toFixed(1),
      ])
    : $t('ui.captcha.sliderTranslateFailTip');
});
function handleStart() {
  state.startTime = Date.now();
}

function handleDragBarMove(data: SliderRotateVerifyPassingData) {
  state.dragging = true;
  const { moveX } = data;
  state.moveDistance = moveX;
  setLeft(`${moveX}px`);
}

function handleDragEnd() {
  const { pieceX } = state;
  const { diffDistance } = props;

  if (Math.abs(pieceX - state.moveDistance) >= (diffDistance || 3)) {
    setLeft('0');
    state.moveDistance = 0;
  } else {
    checkPass();
  }
  state.showTip = true;
  state.dragging = false;
}

function checkPass() {
  state.isPassing = true;
  state.endTime = Date.now();
}

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

function resetCanvas() {
  const { canvasWidth, canvasHeight } = props;
  const puzzleCanvas = unref(puzzleCanvasRef);
  const pieceCanvas = unref(pieceCanvasRef);
  if (!puzzleCanvas || !pieceCanvas) return;
  pieceCanvas.width = canvasWidth;
  const puzzleCanvasCtx = puzzleCanvas.getContext('2d');
  // Canvas2D: Multiple readback operations using getImageData
  // are faster with the willReadFrequently attribute set to true.
  // See: https://html.spec.whatwg.org/multipage/canvas.html#concept-canvas-will-read-frequently (anonymous)
  const pieceCanvasCtx = pieceCanvas.getContext('2d', {
    willReadFrequently: true,
  });
  if (!puzzleCanvasCtx || !pieceCanvasCtx) return;
  puzzleCanvasCtx.clearRect(0, 0, canvasWidth, canvasHeight);
  pieceCanvasCtx.clearRect(0, 0, canvasWidth, canvasHeight);
}

function initCanvas() {
  const { canvasWidth, canvasHeight, squareLength, circleRadius, src } = props;
  const puzzleCanvas = unref(puzzleCanvasRef);
  const pieceCanvas = unref(pieceCanvasRef);
  if (!puzzleCanvas || !pieceCanvas) return;
  const puzzleCanvasCtx = puzzleCanvas.getContext('2d');
  // Canvas2D: Multiple readback operations using getImageData
  // are faster with the willReadFrequently attribute set to true.
  // See: https://html.spec.whatwg.org/multipage/canvas.html#concept-canvas-will-read-frequently (anonymous)
  const pieceCanvasCtx = pieceCanvas.getContext('2d', {
    willReadFrequently: true,
  });
  if (!puzzleCanvasCtx || !pieceCanvasCtx) return;
  const img = new Image();
  // 解决跨域
  img.crossOrigin = 'Anonymous';
  img.src = src;
  img.addEventListener('load', () => {
    draw(puzzleCanvasCtx, pieceCanvasCtx);
    puzzleCanvasCtx.drawImage(img, 0, 0, canvasWidth, canvasHeight);
    pieceCanvasCtx.drawImage(img, 0, 0, canvasWidth, canvasHeight);
    const pieceLength = squareLength + 2 * circleRadius + 3;
    const sx = state.pieceX;
    const sy = state.pieceY - 2 * circleRadius - 1;
    const imageData = pieceCanvasCtx.getImageData(
      sx,
      sy,
      pieceLength,
      pieceLength,
    );
    pieceCanvas.width = pieceLength;
    pieceCanvasCtx.putImageData(imageData, 0, sy);
    setLeft('0');
  });
}

function getRandomNumberByRange(start: number, end: number) {
  return Math.round(Math.random() * (end - start) + start);
}

// 绘制拼图
function draw(ctx1: CanvasRenderingContext2D, ctx2: CanvasRenderingContext2D) {
  const { canvasWidth, canvasHeight, squareLength, circleRadius } = props;
  state.pieceX = getRandomNumberByRange(
    squareLength + 2 * circleRadius,
    canvasWidth - (squareLength + 2 * circleRadius),
  );
  state.pieceY = getRandomNumberByRange(
    3 * circleRadius,
    canvasHeight - (squareLength + 2 * circleRadius),
  );
  drawPiece(ctx1, state.pieceX, state.pieceY, CanvasOpr.Fill);
  drawPiece(ctx2, state.pieceX, state.pieceY, CanvasOpr.Clip);
}

// 绘制拼图切块
function drawPiece(
  ctx: CanvasRenderingContext2D,
  x: number,
  y: number,
  opr: CanvasOpr,
) {
  const { squareLength, circleRadius } = props;
  ctx.beginPath();
  ctx.moveTo(x, y);
  ctx.arc(
    x + squareLength / 2,
    y - circleRadius + 2,
    circleRadius,
    0.72 * PI,
    2.26 * PI,
  );
  ctx.lineTo(x + squareLength, y);
  ctx.arc(
    x + squareLength + circleRadius - 2,
    y + squareLength / 2,
    circleRadius,
    1.21 * PI,
    2.78 * PI,
  );
  ctx.lineTo(x + squareLength, y + squareLength);
  ctx.lineTo(x, y + squareLength);
  ctx.arc(
    x + circleRadius - 2,
    y + squareLength / 2,
    circleRadius + 0.4,
    2.76 * PI,
    1.24 * PI,
    true,
  );
  ctx.lineTo(x, y);
  ctx.lineWidth = 2;
  ctx.fillStyle = 'rgba(255, 255, 255, 0.7)';
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.7)';
  ctx.stroke();
  opr === CanvasOpr.Clip ? ctx.clip() : ctx.fill();
  ctx.globalCompositeOperation = 'destination-over';
}

function resume() {
  state.showTip = false;
  const basicEl = unref(slideBarRef);
  if (!basicEl) {
    return;
  }
  state.dragging = false;
  state.isPassing = false;
  state.pieceX = 0;
  state.pieceY = 0;

  basicEl.resume();
  resetCanvas();
  initCanvas();
}

onMounted(() => {
  initCanvas();
});
</script>

<template>
  <div class="relative flex flex-col items-center">
    <div
      class="border-border relative flex cursor-pointer overflow-hidden border shadow-md"
    >
      <canvas
        ref="puzzleCanvasRef"
        :width="canvasWidth"
        :height="canvasHeight"
        @click="resume"
      ></canvas>
      <canvas
        ref="pieceCanvasRef"
        :width="canvasWidth"
        :height="canvasHeight"
        :style="pieceStyle"
        class="absolute"
        @click="resume"
      ></canvas>
      <div
        class="h-15 absolute bottom-3 left-0 z-10 block w-full text-center text-xs leading-[30px] text-white"
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
          {{ defaultTip || $t('ui.captcha.sliderTranslateDefaultTip') }}
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
