<script lang="ts" setup>
import type { CSSProperties } from 'vue';

import type { CropperProps } from './typing';

import { computed, onMounted, onUnmounted, ref, unref, useAttrs } from 'vue';

import { useDebounceFn } from '@vueuse/core';
import Cropper from 'cropperjs';

import { defaultOptions } from './typing';

import 'cropperjs/dist/cropper.css';

defineOptions({ name: 'CropperImage' });

const props = withDefaults(defineProps<CropperProps>(), {
  src: '',
  alt: '',
  circled: false,
  realTimePreview: true,
  height: '360px',
  crossorigin: undefined,
  imageStyle: () => ({}),
  options: () => ({}),
});

const emit = defineEmits(['cropend', 'ready', 'cropendError']);
const attrs = useAttrs();

type ElRef<T extends HTMLElement = HTMLDivElement> = null | T;
const imgElRef = ref<ElRef<HTMLImageElement>>();
const cropper = ref<Cropper | null>();
const isReady = ref(false);

const debounceRealTimeCropped = useDebounceFn(realTimeCropped, 80);

const getImageStyle = computed((): CSSProperties => {
  return {
    height: props.height,
    maxWidth: '100%',
    ...props.imageStyle,
  };
});

const getClass = computed(() => {
  return [
    attrs.class,
    {
      'cropper-image--circled': props.circled,
    },
  ];
});

const getWrapperStyle = computed((): CSSProperties => {
  return { height: `${`${props.height}`.replace(/px/, '')}px` };
});

onMounted(init);

onUnmounted(() => {
  cropper.value?.destroy();
});

async function init() {
  const imgEl = unref(imgElRef);
  if (!imgEl) {
    return;
  }
  cropper.value = new Cropper(imgEl, {
    ...defaultOptions,
    ready: () => {
      isReady.value = true;
      realTimeCropped();
      emit('ready', cropper.value);
    },
    crop() {
      debounceRealTimeCropped();
    },
    zoom() {
      debounceRealTimeCropped();
    },
    cropmove() {
      debounceRealTimeCropped();
    },
    ...props.options,
  });
}

// Real-time display preview
function realTimeCropped() {
  props.realTimePreview && cropped();
}

// event: return base64 and width and height information after cropping
function cropped() {
  if (!cropper.value) {
    return;
  }
  const imgInfo = cropper.value.getData();
  const canvas = props.circled
    ? getRoundedCanvas()
    : cropper.value.getCroppedCanvas();
  canvas.toBlob((blob) => {
    if (!blob) {
      return;
    }
    const fileReader: FileReader = new FileReader();
    fileReader.readAsDataURL(blob);
    fileReader.onloadend = (e) => {
      emit('cropend', {
        imgBase64: e.target?.result ?? '',
        imgInfo,
      });
    };
    fileReader.addEventListener('error', () => {
      emit('cropendError');
    });
  }, 'image/png');
}

// Get a circular picture canvas
function getRoundedCanvas() {
  const sourceCanvas = cropper.value!.getCroppedCanvas();
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d')!;
  const width = sourceCanvas.width;
  const height = sourceCanvas.height;
  canvas.width = width;
  canvas.height = height;
  context.imageSmoothingEnabled = true;
  context.drawImage(sourceCanvas, 0, 0, width, height);
  context.globalCompositeOperation = 'destination-in';
  context.beginPath();
  context.arc(
    width / 2,
    height / 2,
    Math.min(width, height) / 2,
    0,
    2 * Math.PI,
    true,
  );
  context.fill();
  return canvas;
}
</script>

<template>
  <div :class="getClass" :style="getWrapperStyle">
    <img
      v-show="isReady"
      ref="imgElRef"
      :alt="alt"
      :crossorigin="crossorigin"
      :src="src"
      :style="getImageStyle"
      class="h-auto max-w-full"
    />
  </div>
</template>

<style lang="scss">
.cropper-image {
  &--circled {
    .cropper-view-box,
    .cropper-face {
      border-radius: 50%;
    }
  }
}
</style>
