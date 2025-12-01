<script setup lang="ts">
import type { DialogContentEmits, DialogContentProps } from 'reka-ui';

import type { ClassType } from '@vben-core/typings';

import { computed, ref } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { X } from 'lucide-vue-next';
import { DialogClose, DialogContent, useForwardPropsEmits } from 'reka-ui';

import DialogOverlay from './DialogOverlay.vue';

const props = withDefaults(
  defineProps<
    DialogContentProps & {
      animationType?: 'scale' | 'slide';
      appendTo?: HTMLElement | string;
      class?: ClassType;
      closeClass?: ClassType;
      closeDisabled?: boolean;
      modal?: boolean;
      open?: boolean;
      overlayBlur?: number;
      showClose?: boolean;
      zIndex?: number;
    }
  >(),
  {
    appendTo: 'body',
    animationType: 'slide',
    closeDisabled: false,
    showClose: true,
  },
);
const emits = defineEmits<
  DialogContentEmits & { close: []; closed: []; opened: [] }
>();

const delegatedProps = computed(() => {
  const {
    class: _,
    modal: _modal,
    open: _open,
    showClose: __,
    animationType: ___,
    ...delegated
  } = props;

  return delegated;
});

function isAppendToBody() {
  return (
    props.appendTo === 'body' ||
    props.appendTo === document.body ||
    !props.appendTo
  );
}

const position = computed(() => {
  return isAppendToBody() ? 'fixed' : 'absolute';
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);

const contentRef = ref<InstanceType<typeof DialogContent> | null>(null);
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
  <Teleport defer :to="appendTo">
    <Transition name="fade">
      <DialogOverlay
        v-if="open && modal"
        :style="{
          ...(zIndex ? { zIndex } : {}),
          position,
          backdropFilter:
            overlayBlur && overlayBlur > 0 ? `blur(${overlayBlur}px)` : 'none',
        }"
        @click="() => emits('close')"
      />
    </Transition>
    <DialogContent
      ref="contentRef"
      :style="{ ...(zIndex ? { zIndex } : {}), position }"
      @animationend="onAnimationEnd"
      v-bind="forwarded"
      :class="
        cn(
          'z-popup w-full bg-background p-6 shadow-lg outline-none data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 sm:rounded-xl',
          {
            'data-[state=closed]:slide-out-to-top-[48%] data-[state=open]:slide-in-from-top-[48%]':
              animationType === 'slide',
          },
          props.class,
        )
      "
    >
      <slot></slot>

      <DialogClose
        v-if="showClose"
        :disabled="closeDisabled"
        :class="
          cn(
            'flex-center absolute right-3 top-3 h-6 w-6 rounded-full px-1 text-lg text-foreground/80 opacity-70 transition-opacity hover:bg-accent hover:text-accent-foreground hover:opacity-100 focus:outline-none disabled:pointer-events-none data-[state=open]:bg-accent data-[state=open]:text-muted-foreground',
            props.closeClass,
          )
        "
        @click="() => emits('close')"
      >
        <X class="h-4 w-4" />
      </DialogClose>
    </DialogContent>
  </Teleport>
</template>
