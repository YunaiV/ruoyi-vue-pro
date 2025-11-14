<script lang="ts" setup>
import { ref, watch } from 'vue';

import { cn } from '@vben-core/shared/utils';

interface Props {
  class?: string;
  /**
   * @zh_CN 最小加载时间
   * @en_US Minimum loading time
   */
  minLoadingTime?: number;

  /**
   * @zh_CN loading状态开启
   */
  spinning?: boolean;
  /**
   * @zh_CN 文字
   */
  text?: string;
}

defineOptions({
  name: 'VbenLoading',
});

const props = withDefaults(defineProps<Props>(), {
  minLoadingTime: 50,
  text: '',
});
// const startTime = ref(0);
const showSpinner = ref(false);
const renderSpinner = ref(false);
const timer = ref<ReturnType<typeof setTimeout>>();

watch(
  () => props.spinning,
  (show) => {
    if (!show) {
      showSpinner.value = false;
      clearTimeout(timer.value);
      return;
    }

    // startTime.value = performance.now();
    timer.value = setTimeout(() => {
      // const loadingTime = performance.now() - startTime.value;

      showSpinner.value = true;
      if (showSpinner.value) {
        renderSpinner.value = true;
      }
    }, props.minLoadingTime);
  },
  {
    immediate: true,
  },
);

function onTransitionEnd() {
  if (!showSpinner.value) {
    renderSpinner.value = false;
  }
}
</script>

<template>
  <div
    :class="
      cn(
        'z-100 dark:bg-overlay bg-overlay-content absolute left-0 top-0 flex size-full flex-col items-center justify-center transition-all duration-500',
        {
          'invisible opacity-0': !showSpinner,
        },
        props.class,
      )
    "
    @transitionend="onTransitionEnd"
  >
    <slot name="icon" v-if="renderSpinner">
      <span class="dot relative inline-block size-9 text-3xl">
        <i
          v-for="index in 4"
          :key="index"
          class="bg-primary absolute block size-4 origin-[50%_50%] scale-75 rounded-full opacity-30"
        ></i>
      </span>
    </slot>

    <div v-if="text" class="text-primary mt-4 text-xs">{{ text }}</div>
    <slot></slot>
  </div>
</template>

<style scoped>
.dot {
  transform: rotate(45deg);
  animation: rotate-ani 1.2s infinite linear;
}

.dot i {
  animation: spin-move-ani 1s infinite linear alternate;
}

.dot i:nth-child(1) {
  top: 0;
  left: 0;
}

.dot i:nth-child(2) {
  top: 0;
  right: 0;
  animation-delay: 0.4s;
}

.dot i:nth-child(3) {
  right: 0;
  bottom: 0;
  animation-delay: 0.8s;
}

.dot i:nth-child(4) {
  bottom: 0;
  left: 0;
  animation-delay: 1.2s;
}

@keyframes rotate-ani {
  to {
    transform: rotate(405deg);
  }
}

@keyframes spin-move-ani {
  to {
    opacity: 1;
  }
}
</style>
