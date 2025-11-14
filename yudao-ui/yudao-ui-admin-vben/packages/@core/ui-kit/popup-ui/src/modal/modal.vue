<script lang="ts" setup>
import type { ExtendedModalApi, ModalProps } from './modal';

import {
  computed,
  nextTick,
  onDeactivated,
  provide,
  ref,
  unref,
  useId,
  watch,
} from 'vue';

import {
  useIsMobile,
  usePriorityValues,
  useSimpleLocale,
} from '@vben-core/composables';
import { Expand, Shrink } from '@vben-core/icons';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  VbenButton,
  VbenHelpTooltip,
  VbenIconButton,
  VbenLoading,
  VisuallyHidden,
} from '@vben-core/shadcn-ui';
import { ELEMENT_ID_MAIN_CONTENT } from '@vben-core/shared/constants';
import { globalShareState } from '@vben-core/shared/global-state';
import { cn } from '@vben-core/shared/utils';

import { useModalDraggable } from './use-modal-draggable';

interface Props extends ModalProps {
  modalApi?: ExtendedModalApi;
}

const props = withDefaults(defineProps<Props>(), {
  appendToMain: false,
  destroyOnClose: false,
  modalApi: undefined,
});

const components = globalShareState.getComponents();

const contentRef = ref();
const wrapperRef = ref<HTMLElement>();
const dialogRef = ref();
const headerRef = ref();
const footerRef = ref();

const id = useId();

provide('DISMISSABLE_MODAL_ID', id);

const { $t } = useSimpleLocale();
const { isMobile } = useIsMobile();
const state = props.modalApi?.useStore?.();

const {
  appendToMain,
  bordered,
  cancelText,
  centered,
  class: modalClass,
  closable,
  closeOnClickModal,
  closeOnPressEscape,
  confirmDisabled,
  confirmLoading,
  confirmText,
  contentClass,
  description,
  destroyOnClose,
  draggable,
  footer: showFooter,
  footerClass,
  fullscreen,
  fullscreenButton,
  header,
  headerClass,
  loading: showLoading,
  modal,
  openAutoFocus,
  overlayBlur,
  showCancelButton,
  showConfirmButton,
  submitting,
  title,
  titleTooltip,
  animationType,
  zIndex,
} = usePriorityValues(props, state);

const shouldFullscreen = computed(() => fullscreen.value || isMobile.value);

const shouldDraggable = computed(
  () => draggable.value && !shouldFullscreen.value && header.value,
);

const getAppendTo = computed(() => {
  return appendToMain.value
    ? `#${ELEMENT_ID_MAIN_CONTENT}>div:not(.absolute)>div`
    : undefined;
});

const { dragging, transform } = useModalDraggable(
  dialogRef,
  headerRef,
  shouldDraggable,
  getAppendTo,
);

const firstOpened = ref(false);
const isClosed = ref(true);

watch(
  () => state?.value?.isOpen,
  async (v) => {
    if (v) {
      isClosed.value = false;
      if (!firstOpened.value) firstOpened.value = true;
      await nextTick();
      if (!contentRef.value) return;
      const innerContentRef = contentRef.value.getContentRef();
      dialogRef.value = innerContentRef.$el;
      // reopen modal reassign value
      const { offsetX, offsetY } = transform;
      dialogRef.value.style.transform = `translate(${offsetX}px, ${offsetY}px)`;
    }
  },
  { immediate: true },
);

// watch(
//   () => [showLoading.value, submitting.value],
//   ([l, s]) => {
//     if ((s || l) && wrapperRef.value) {
//       wrapperRef.value.scrollTo({
//         // behavior: 'smooth',
//         top: 0,
//       });
//     }
//   },
// );

/**
 * 在开启keepAlive情况下 直接通过浏览器按钮/手势等返回 不会关闭弹窗
 */
onDeactivated(() => {
  // 如果弹窗没有被挂载到内容区域，则关闭弹窗
  if (!appendToMain.value) {
    props.modalApi?.close();
  }
});

function handleFullscreen() {
  props.modalApi?.setState((prev) => {
    // if (prev.fullscreen) {
    //   resetPosition();
    // }
    return { ...prev, fullscreen: !fullscreen.value };
  });
}
function interactOutside(e: Event) {
  if (!closeOnClickModal.value || submitting.value) {
    e.preventDefault();
    e.stopPropagation();
  }
}
function escapeKeyDown(e: KeyboardEvent) {
  if (!closeOnPressEscape.value || submitting.value) {
    e.preventDefault();
  }
}

function handerOpenAutoFocus(e: Event) {
  if (!openAutoFocus.value) {
    e?.preventDefault();
  }
}

// pointer-down-outside
function pointerDownOutside(e: Event) {
  const target = e.target as HTMLElement;
  const isDismissableModal = target?.dataset.dismissableModal;
  if (
    !closeOnClickModal.value ||
    isDismissableModal !== id ||
    submitting.value
  ) {
    e.preventDefault();
    e.stopPropagation();
  }
}

function handleFocusOutside(e: Event) {
  e.preventDefault();
  e.stopPropagation();
}

const getForceMount = computed(() => {
  return !unref(destroyOnClose) && unref(firstOpened);
});

function handleClosed() {
  isClosed.value = true;
  props.modalApi?.onClosed();
}
</script>
<template>
  <Dialog
    :modal="false"
    :open="state?.isOpen"
    @update:open="() => (!submitting ? modalApi?.close() : undefined)"
  >
    <DialogContent
      ref="contentRef"
      :append-to="getAppendTo"
      :class="
        cn(
          'left-0 right-0 top-[10vh] mx-auto flex max-h-[80%] w-[520px] flex-col p-0',
          shouldFullscreen ? 'sm:rounded-none' : 'sm:rounded-[var(--radius)]',
          modalClass,
          {
            'border-border border': bordered,
            'shadow-3xl': !bordered,
            'left-0 top-0 size-full max-h-full !translate-x-0 !translate-y-0':
              shouldFullscreen,
            'top-1/2 !-translate-y-1/2': centered && !shouldFullscreen,
            'duration-300': !dragging,
            hidden: isClosed,
          },
        )
      "
      :force-mount="getForceMount"
      :modal="modal"
      :open="state?.isOpen"
      :show-close="closable"
      :animation-type="animationType"
      :z-index="zIndex"
      :overlay-blur="overlayBlur"
      close-class="top-3"
      @close-auto-focus="handleFocusOutside"
      @closed="handleClosed"
      :close-disabled="submitting"
      @escape-key-down="escapeKeyDown"
      @focus-outside="handleFocusOutside"
      @interact-outside="interactOutside"
      @open-auto-focus="handerOpenAutoFocus"
      @opened="() => modalApi?.onOpened()"
      @pointer-down-outside="pointerDownOutside"
    >
      <DialogHeader
        ref="headerRef"
        :class="
          cn(
            'px-5 py-4',
            {
              'border-b': bordered,
              hidden: !header,
              'cursor-move select-none': shouldDraggable,
            },
            headerClass,
          )
        "
      >
        <DialogTitle v-if="title" class="text-left">
          <slot name="title">
            {{ title }}

            <slot v-if="titleTooltip" name="titleTooltip">
              <VbenHelpTooltip trigger-class="pb-1">
                {{ titleTooltip }}
              </VbenHelpTooltip>
            </slot>
          </slot>
        </DialogTitle>
        <DialogDescription v-if="description">
          <slot name="description">
            {{ description }}
          </slot>
        </DialogDescription>
        <VisuallyHidden v-if="!title || !description">
          <DialogTitle v-if="!title" />
          <DialogDescription v-if="!description" />
        </VisuallyHidden>
      </DialogHeader>
      <div
        ref="wrapperRef"
        :class="
          cn('relative min-h-40 flex-1 overflow-y-auto p-3', contentClass, {
            'pointer-events-none': showLoading || submitting,
          })
        "
      >
        <slot></slot>
      </div>
      <VbenLoading v-if="showLoading || submitting" spinning />
      <VbenIconButton
        v-if="fullscreenButton"
        class="hover:bg-accent hover:text-accent-foreground text-foreground/80 flex-center absolute right-10 top-3 hidden size-6 rounded-full px-1 text-lg opacity-70 transition-opacity hover:opacity-100 focus:outline-none disabled:pointer-events-none sm:block"
        @click="handleFullscreen"
      >
        <Shrink v-if="fullscreen" class="size-3.5" />
        <Expand v-else class="size-3.5" />
      </VbenIconButton>

      <DialogFooter
        v-if="showFooter"
        ref="footerRef"
        :class="
          cn(
            'flex-row items-center justify-end p-2',
            {
              'border-t': bordered,
            },
            footerClass,
          )
        "
      >
        <slot name="prepend-footer"></slot>
        <slot name="footer">
          <component
            :is="components.DefaultButton || VbenButton"
            v-if="showCancelButton"
            variant="ghost"
            :disabled="submitting"
            @click="() => modalApi?.onCancel()"
          >
            <slot name="cancelText">
              {{ cancelText || $t('cancel') }}
            </slot>
          </component>
          <slot name="center-footer"></slot>
          <component
            :is="components.PrimaryButton || VbenButton"
            v-if="showConfirmButton"
            :disabled="confirmDisabled"
            :loading="confirmLoading || submitting"
            @click="() => modalApi?.onConfirm()"
          >
            <slot name="confirmText">
              {{ confirmText || $t('confirm') }}
            </slot>
          </component>
        </slot>
        <slot name="append-footer"></slot>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>
