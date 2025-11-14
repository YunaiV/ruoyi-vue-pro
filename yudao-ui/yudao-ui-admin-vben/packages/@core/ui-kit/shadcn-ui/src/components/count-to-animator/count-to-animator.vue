<script lang="ts" setup>
import { computed, onMounted, ref, unref, watch, watchEffect } from 'vue';

import { isNumber } from '@vben-core/shared/utils';

import { TransitionPresets, useTransition } from '@vueuse/core';

interface Props {
  autoplay?: boolean;
  color?: string;
  decimal?: string;
  decimals?: number;
  duration?: number;
  endVal?: number;
  prefix?: string;
  separator?: string;
  startVal?: number;
  suffix?: string;
  transition?: keyof typeof TransitionPresets;
  useEasing?: boolean;
}

defineOptions({ name: 'CountToAnimator' });

const props = withDefaults(defineProps<Props>(), {
  autoplay: true,
  color: '',
  decimal: '.',
  decimals: 0,
  duration: 1500,
  endVal: 2021,
  prefix: '',
  separator: ',',
  startVal: 0,
  suffix: '',
  transition: 'linear',
  useEasing: true,
});

const emit = defineEmits<{
  finished: [];
  /**
   * @deprecated 请使用{@link finished}事件
   */
  onFinished: [];
  /**
   * @deprecated 请使用{@link started}事件
   */
  onStarted: [];
  started: [];
}>();

const source = ref(props.startVal);
const disabled = ref(false);
let outputValue = useTransition(source);

const value = computed(() => formatNumber(unref(outputValue)));

watchEffect(() => {
  source.value = props.startVal;
});

watch([() => props.startVal, () => props.endVal], () => {
  if (props.autoplay) {
    start();
  }
});

onMounted(() => {
  props.autoplay && start();
});

function start() {
  run();
  source.value = props.endVal;
}

function reset() {
  source.value = props.startVal;
  run();
}

function run() {
  outputValue = useTransition(source, {
    disabled,
    duration: props.duration,
    onFinished: () => {
      emit('finished');
      emit('onFinished');
    },
    onStarted: () => {
      emit('started');
      emit('onStarted');
    },
    ...(props.useEasing
      ? { transition: TransitionPresets[props.transition] }
      : {}),
  });
}

function formatNumber(num: number | string) {
  if (!num && num !== 0) {
    return '';
  }
  const { decimal, decimals, prefix, separator, suffix } = props;
  num = Number(num).toFixed(decimals);
  num += '';

  const x = num.split('.');
  let x1 = x[0];
  const x2 = x.length > 1 ? decimal + x[1] : '';

  const rgx = /(\d+)(\d{3})/;
  if (separator && !isNumber(separator) && x1) {
    while (rgx.test(x1)) {
      x1 = x1.replace(rgx, `$1${separator}$2`);
    }
  }
  return prefix + x1 + x2 + suffix;
}

defineExpose({ reset });
</script>
<template>
  <span :style="{ color }">
    {{ value }}
  </span>
</template>
