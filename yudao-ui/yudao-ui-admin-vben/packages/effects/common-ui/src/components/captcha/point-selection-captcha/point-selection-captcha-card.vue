<script setup lang="ts">
import type { PointSelectionCaptchaCardProps } from '../types';

import { computed } from 'vue';

import { $t } from '@vben/locales';

import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@vben-core/shadcn-ui';

const props = withDefaults(defineProps<PointSelectionCaptchaCardProps>(), {
  height: '220px',
  paddingX: '12px',
  paddingY: '16px',
  title: '',
  width: '300px',
});

const emit = defineEmits<{
  click: [MouseEvent];
}>();

const parseValue = (value: number | string) => {
  if (typeof value === 'number') {
    return value;
  }
  const parsed = Number.parseFloat(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

const rootStyles = computed(() => ({
  padding: `${parseValue(props.paddingY)}px ${parseValue(props.paddingX)}px`,
  width: `${parseValue(props.width) + parseValue(props.paddingX) * 2}px`,
}));

const captchaStyles = computed(() => {
  return {
    height: `${parseValue(props.height)}px`,
    width: `${parseValue(props.width)}px`,
  };
});

function handleClick(e: MouseEvent) {
  emit('click', e);
}
</script>
<template>
  <Card :style="rootStyles" aria-labelledby="captcha-title" role="region">
    <CardHeader class="p-0">
      <CardTitle id="captcha-title" class="flex items-center justify-between">
        <template v-if="$slots.title">
          <slot name="title">{{ $t('ui.captcha.title') }}</slot>
        </template>
        <template v-else>
          <span>{{ title }}</span>
        </template>
        <div class="flex items-center justify-end">
          <slot name="extra"></slot>
        </div>
      </CardTitle>
    </CardHeader>
    <CardContent class="relative mt-2 flex w-full overflow-hidden rounded p-0">
      <img
        v-show="captchaImage"
        :alt="$t('ui.captcha.alt')"
        :src="captchaImage"
        :style="captchaStyles"
        class="relative z-10"
        @click="handleClick"
      />
      <div class="absolute inset-0">
        <slot></slot>
      </div>
    </CardContent>
    <CardFooter class="mt-2 flex justify-between p-0">
      <slot name="footer"></slot>
    </CardFooter>
  </Card>
</template>
