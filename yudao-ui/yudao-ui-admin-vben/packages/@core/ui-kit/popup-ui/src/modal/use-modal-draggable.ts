/**
 * @copy https://github.com/element-plus/element-plus/blob/dev/packages/hooks/use-draggable/index.ts
 * 调整部分细节
 */

import type { ComputedRef, Ref } from 'vue';

import { onBeforeUnmount, onMounted, reactive, ref, watchEffect } from 'vue';

import { unrefElement } from '@vueuse/core';

export function useModalDraggable(
  targetRef: Ref<HTMLElement | undefined>,
  dragRef: Ref<HTMLElement | undefined>,
  draggable: ComputedRef<boolean>,
  containerSelector?: ComputedRef<string | undefined>,
) {
  const transform = reactive({
    offsetX: 0,
    offsetY: 0,
  });

  const dragging = ref(false);

  const onMousedown = (e: MouseEvent) => {
    const downX = e.clientX;
    const downY = e.clientY;

    if (!targetRef.value) {
      return;
    }

    const targetRect = targetRef.value.getBoundingClientRect();
    const { offsetX, offsetY } = transform;
    const targetLeft = targetRect.left;
    const targetTop = targetRect.top;
    const targetWidth = targetRect.width;
    const targetHeight = targetRect.height;

    let containerRect: DOMRect | null = null;

    if (containerSelector?.value) {
      const container = document.querySelector(containerSelector.value);
      if (container) {
        containerRect = container.getBoundingClientRect();
      }
    }

    let maxLeft, maxTop, minLeft, minTop;
    if (containerRect) {
      minLeft = containerRect.left - targetLeft + offsetX;
      maxLeft = containerRect.right - targetLeft - targetWidth + offsetX;
      minTop = containerRect.top - targetTop + offsetY;
      maxTop = containerRect.bottom - targetTop - targetHeight + offsetY;
    } else {
      const docElement = document.documentElement;
      const clientWidth = docElement.clientWidth;
      const clientHeight = docElement.clientHeight;
      minLeft = -targetLeft + offsetX;
      minTop = -targetTop + offsetY;
      maxLeft = clientWidth - targetLeft - targetWidth + offsetX;
      maxTop = clientHeight - targetTop - targetHeight + offsetY;
    }

    const onMousemove = (e: MouseEvent) => {
      let moveX = offsetX + e.clientX - downX;
      let moveY = offsetY + e.clientY - downY;

      moveX = Math.min(Math.max(moveX, minLeft), maxLeft);
      moveY = Math.min(Math.max(moveY, minTop), maxTop);

      transform.offsetX = moveX;
      transform.offsetY = moveY;

      if (targetRef.value) {
        targetRef.value.style.transform = `translate(${moveX}px, ${moveY}px)`;
        dragging.value = true;
      }
    };

    const onMouseup = () => {
      dragging.value = false;
      document.removeEventListener('mousemove', onMousemove);
      document.removeEventListener('mouseup', onMouseup);
    };

    document.addEventListener('mousemove', onMousemove);
    document.addEventListener('mouseup', onMouseup);
  };

  const onDraggable = () => {
    const dragDom = unrefElement(dragRef);
    if (dragDom && targetRef.value) {
      dragDom.addEventListener('mousedown', onMousedown);
    }
  };

  const offDraggable = () => {
    const dragDom = unrefElement(dragRef);
    if (dragDom && targetRef.value) {
      dragDom.removeEventListener('mousedown', onMousedown);
    }
  };

  const resetPosition = () => {
    transform.offsetX = 0;
    transform.offsetY = 0;

    const target = unrefElement(targetRef);
    if (target) {
      target.style.transform = 'none';
    }
  };

  onMounted(() => {
    watchEffect(() => {
      if (draggable.value) {
        onDraggable();
      } else {
        offDraggable();
      }
    });
  });

  onBeforeUnmount(() => {
    offDraggable();
  });

  return {
    dragging,
    resetPosition,
    transform,
  };
}
