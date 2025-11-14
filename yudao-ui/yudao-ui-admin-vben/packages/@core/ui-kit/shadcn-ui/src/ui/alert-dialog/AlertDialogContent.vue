<script setup lang="ts">
import type {
  AlertDialogContentEmits,
  AlertDialogContentProps,
} from 'radix-vue';

import type { ClassType } from '@vben-core/typings';

import { computed, ref } from 'vue';

import { cn } from '@vben-core/shared/utils';

import {
  AlertDialogContent,
  AlertDialogPortal,
  useForwardPropsEmits,
} from 'radix-vue';

import AlertDialogOverlay from './AlertDialogOverlay.vue';

const props = withDefaults(
  defineProps<
    AlertDialogContentProps & {
      centered?: boolean;
      class?: ClassType;
      modal?: boolean;
      open?: boolean;
      overlayBlur?: number;
      zIndex?: number;
    }
  >(),
  { modal: true },
);
const emits = defineEmits<
  AlertDialogContentEmits & { close: []; closed: []; opened: [] }
>();

const delegatedProps = computed(() => {
  const { class: _, modal: _modal, open: _open, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);

const contentRef = ref<InstanceType<typeof AlertDialogContent> | null>(null);
function onAnimationEnd(event: AnimationEvent) {
  // 只有在 contentRef 的动画结束时才触发 opened/closed 事件
  if (event.target === contentRef.value?.$el) {
    if (props.open) {
      emits('opened');
    } else {
      emits('closed');
    }
  }
}
defineExpose({
  getContentRef: () => contentRef.value,
});
</script>

<template>
  <AlertDialogPortal>
    <Transition name="fade" appear>
      <AlertDialogOverlay
        v-if="open && modal"
        :style="{
          ...(zIndex ? { zIndex } : {}),
          position: 'fixed',
          backdropFilter:
            overlayBlur && overlayBlur > 0 ? `blur(${overlayBlur}px)` : 'none',
        }"
        @click="() => emits('close')"
      />
    </Transition>
    <AlertDialogContent
      ref="contentRef"
      :style="{ ...(zIndex ? { zIndex } : {}), position: 'fixed' }"
      @animationend="onAnimationEnd"
      v-bind="forwarded"
      :class="
        cn(
          'z-popup bg-background p-6 shadow-lg outline-none sm:rounded-xl',
          'data-[state=open]:animate-in data-[state=open]:fade-in-0 data-[state=open]:zoom-in-95',
          'data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=closed]:zoom-out-95',
          {
            'data-[state=open]:slide-in-from-top-[48%] data-[state=closed]:slide-out-to-top-[48%]':
              !centered,
            'data-[state=open]:slide-in-from-top-[98%] data-[state=closed]:slide-out-to-top-[148%]':
              centered,
            'top-[10vh]': !centered,
            'top-1/2 -translate-y-1/2': centered,
          },
          props.class,
        )
      "
    >
      <slot></slot>
    </AlertDialogContent>
  </AlertDialogPortal>
</template>
