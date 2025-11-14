<script setup lang="ts">
import type {
  CaptchaVerifyPassingData,
  SliderCaptchaProps,
  SliderRotateVerifyPassingData,
} from '../types';

import { reactive, unref, useTemplateRef, watch, watchEffect } from 'vue';

import { $t } from '@vben/locales';

import { cn } from '@vben-core/shared/utils';

import { useTimeoutFn } from '@vueuse/core';

import SliderCaptchaAction from './slider-captcha-action.vue';
import SliderCaptchaBar from './slider-captcha-bar.vue';
import SliderCaptchaContent from './slider-captcha-content.vue';

const props = withDefaults(defineProps<SliderCaptchaProps>(), {
  actionStyle: () => ({}),
  barStyle: () => ({}),
  contentStyle: () => ({}),
  isSlot: false,
  successText: '',
  text: '',
  wrapperStyle: () => ({}),
});

const emit = defineEmits<{
  end: [MouseEvent | TouchEvent];
  move: [SliderRotateVerifyPassingData];
  start: [MouseEvent | TouchEvent];
  success: [CaptchaVerifyPassingData];
}>();

const modelValue = defineModel<boolean>({ default: false });

const state = reactive({
  endTime: 0,
  isMoving: false,
  isPassing: false,
  moveDistance: 0,
  startTime: 0,
  toLeft: false,
});

defineExpose({
  resume,
});

const wrapperRef = useTemplateRef<HTMLDivElement>('wrapperRef');
const barRef = useTemplateRef<typeof SliderCaptchaBar>('barRef');
const contentRef = useTemplateRef<typeof SliderCaptchaContent>('contentRef');
const actionRef = useTemplateRef<typeof SliderCaptchaAction>('actionRef');

watch(
  () => state.isPassing,
  (isPassing) => {
    if (isPassing) {
      const { endTime, startTime } = state;
      const time = (endTime - startTime) / 1000;
      emit('success', { isPassing, time: time.toFixed(1) });
      modelValue.value = isPassing;
    }
  },
);

watchEffect(() => {
  state.isPassing = !!modelValue.value;
});

function getEventPageX(e: MouseEvent | TouchEvent): number {
  if ('pageX' in e) {
    return e.pageX;
  } else if ('touches' in e && e.touches[0]) {
    return e.touches[0].pageX;
  }
  return 0;
}

function handleDragStart(e: MouseEvent | TouchEvent) {
  if (state.isPassing) {
    return;
  }
  if (!actionRef.value) return;
  emit('start', e);

  state.moveDistance =
    getEventPageX(e) -
    Number.parseInt(
      actionRef.value.getStyle().left.replace('px', '') || '0',
      10,
    );
  state.startTime = Date.now();
  state.isMoving = true;
}

function getOffset(actionEl: HTMLDivElement) {
  const wrapperWidth = wrapperRef.value?.offsetWidth ?? 220;
  const actionWidth = actionEl?.offsetWidth ?? 40;
  const offset = wrapperWidth - actionWidth - 6;
  return { actionWidth, offset, wrapperWidth };
}

function handleDragMoving(e: MouseEvent | TouchEvent) {
  const { isMoving, moveDistance } = state;
  if (isMoving) {
    const actionEl = unref(actionRef);
    const barEl = unref(barRef);
    if (!actionEl || !barEl) return;
    const { actionWidth, offset, wrapperWidth } = getOffset(actionEl.getEl());
    const moveX = getEventPageX(e) - moveDistance;

    emit('move', {
      event: e,
      moveDistance,
      moveX,
    });
    if (moveX > 0 && moveX <= offset) {
      actionEl.setLeft(`${moveX}px`);
      barEl.setWidth(`${moveX + actionWidth / 2}px`);
    } else if (moveX > offset) {
      actionEl.setLeft(`${wrapperWidth - actionWidth}px`);
      barEl.setWidth(`${wrapperWidth - actionWidth / 2}px`);
      if (!props.isSlot) {
        checkPass();
      }
    }
  }
}

function handleDragOver(e: MouseEvent | TouchEvent) {
  const { isMoving, isPassing, moveDistance } = state;
  if (isMoving && !isPassing) {
    emit('end', e);
    const actionEl = actionRef.value;
    const barEl = unref(barRef);
    if (!actionEl || !barEl) return;
    const moveX = getEventPageX(e) - moveDistance;
    const { actionWidth, offset, wrapperWidth } = getOffset(actionEl.getEl());
    if (moveX < offset) {
      if (props.isSlot) {
        setTimeout(() => {
          if (modelValue.value) {
            const contentEl = unref(contentRef);
            if (contentEl) {
              contentEl.getEl().style.width = `${Number.parseInt(barEl.getEl().style.width)}px`;
            }
          } else {
            resume();
          }
        }, 0);
      } else {
        resume();
      }
    } else {
      actionEl.setLeft(`${wrapperWidth - actionWidth}px`);
      barEl.setWidth(`${wrapperWidth - actionWidth / 2}px`);
      checkPass();
    }
    state.isMoving = false;
  }
}

function checkPass() {
  if (props.isSlot) {
    resume();
    return;
  }
  state.endTime = Date.now();
  state.isPassing = true;
  state.isMoving = false;
}

function resume() {
  state.isMoving = false;
  state.isPassing = false;
  state.moveDistance = 0;
  state.toLeft = false;
  state.startTime = 0;
  state.endTime = 0;
  const actionEl = unref(actionRef);
  const barEl = unref(barRef);
  const contentEl = unref(contentRef);
  if (!actionEl || !barEl || !contentEl) return;

  contentEl.getEl().style.width = '100%';
  state.toLeft = true;
  useTimeoutFn(() => {
    state.toLeft = false;
    actionEl.setLeft('0');
    barEl.setWidth('0');
  }, 300);
}
</script>

<template>
  <div
    ref="wrapperRef"
    :class="
      cn(
        'border-border bg-background-deep relative flex h-10 w-full items-center overflow-hidden rounded-md border text-center',
        props.class,
      )
    "
    :style="wrapperStyle"
    @mouseleave="handleDragOver"
    @mousemove="handleDragMoving"
    @mouseup="handleDragOver"
    @touchend="handleDragOver"
    @touchmove="handleDragMoving"
  >
    <SliderCaptchaBar
      ref="barRef"
      :bar-style="barStyle"
      :to-left="state.toLeft"
    />
    <SliderCaptchaContent
      ref="contentRef"
      :content-style="contentStyle"
      :is-passing="state.isPassing"
      :success-text="successText || $t('ui.captcha.sliderSuccessText')"
      :text="text || $t('ui.captcha.sliderDefaultText')"
    >
      <template v-if="$slots.text" #text>
        <slot :is-passing="state.isPassing" name="text"></slot>
      </template>
    </SliderCaptchaContent>

    <SliderCaptchaAction
      ref="actionRef"
      :action-style="actionStyle"
      :is-passing="state.isPassing"
      :to-left="state.toLeft"
      @mousedown="handleDragStart"
      @touchstart="handleDragStart"
    >
      <template v-if="$slots.actionIcon" #icon>
        <slot :is-passing="state.isPassing" name="actionIcon"></slot>
      </template>
    </SliderCaptchaAction>
  </div>
</template>
