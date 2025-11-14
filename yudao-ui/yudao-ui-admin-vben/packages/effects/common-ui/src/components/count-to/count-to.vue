<script lang="ts" setup>
import type { CountToProps } from './types';

import { computed, onMounted, ref, watch } from 'vue';

import { isString } from '@vben-core/shared/utils';

import { TransitionPresets, useTransition } from '@vueuse/core';

const props = withDefaults(defineProps<CountToProps>(), {
  startVal: 0,
  duration: 2000,
  separator: ',',
  decimal: '.',
  decimals: 0,
  delay: 0,
  transition: () => TransitionPresets.easeOutExpo,
});

const emit = defineEmits(['started', 'finished']);

const lastValue = ref(props.startVal);

onMounted(() => {
  lastValue.value = props.endVal;
});

watch(
  () => props.endVal,
  (val) => {
    lastValue.value = val;
  },
);

const currentValue = useTransition(lastValue, {
  delay: computed(() => props.delay),
  duration: computed(() => props.duration),
  disabled: computed(() => props.disabled),
  transition: computed(() => {
    return isString(props.transition)
      ? TransitionPresets[props.transition]
      : props.transition;
  }),
  onStarted() {
    emit('started');
  },
  onFinished() {
    emit('finished');
  },
});

const numMain = computed(() => {
  const result = currentValue.value
    .toFixed(props.decimals)
    .split('.')[0]
    ?.replaceAll(/\B(?=(\d{3})+(?!\d))/g, props.separator);
  return result;
});

const numDec = computed(() => {
  return (
    props.decimal + currentValue.value.toFixed(props.decimals).split('.')[1]
  );
});
</script>
<template>
  <div class="count-to" v-bind="$attrs">
    <slot name="prefix">
      <div
        class="count-to-prefix"
        :style="prefixStyle"
        :class="prefixClass"
        v-if="prefix"
      >
        {{ prefix }}
      </div>
    </slot>
    <div class="count-to-main" :class="mainClass" :style="mainStyle">
      <span>{{ numMain }}</span>
      <span
        class="count-to-main-decimal"
        v-if="decimals > 0"
        :class="decimalClass"
        :style="decimalStyle"
      >
        {{ numDec }}
      </span>
    </div>
    <slot name="suffix">
      <div
        class="count-to-suffix"
        :style="suffixStyle"
        :class="suffixClass"
        v-if="suffix"
      >
        {{ suffix }}
      </div>
    </slot>
  </div>
</template>
<style lang="scss" scoped>
.count-to {
  display: flex;
  align-items: baseline;

  &-prefix {
    // font-size: 1rem;
  }

  &-suffix {
    // font-size: 1rem;
  }

  &-main {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
    // font-size: 1.5rem;

    &-decimal {
      // font-size: 0.8rem;
    }
  }
}
</style>
