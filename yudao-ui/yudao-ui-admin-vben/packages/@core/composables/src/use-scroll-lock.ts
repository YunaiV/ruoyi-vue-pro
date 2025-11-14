import { getScrollbarWidth, needsScrollbar } from '@vben-core/shared/utils';

import {
  useScrollLock as _useScrollLock,
  tryOnBeforeUnmount,
  tryOnMounted,
} from '@vueuse/core';

export const SCROLL_FIXED_CLASS = `_scroll__fixed_`;

export function useScrollLock() {
  const isLocked = _useScrollLock(document.body);
  const scrollbarWidth = getScrollbarWidth();

  tryOnMounted(() => {
    if (!needsScrollbar()) {
      return;
    }
    document.body.style.paddingRight = `${scrollbarWidth}px`;

    const layoutFixedNodes = document.querySelectorAll<HTMLElement>(
      `.${SCROLL_FIXED_CLASS}`,
    );
    const nodes = [...layoutFixedNodes];
    if (nodes.length > 0) {
      nodes.forEach((node) => {
        node.dataset.transition = node.style.transition;
        node.style.transition = 'none';
        node.style.paddingRight = `${scrollbarWidth}px`;
      });
    }
    isLocked.value = true;
  });

  tryOnBeforeUnmount(() => {
    if (!needsScrollbar()) {
      return;
    }
    isLocked.value = false;
    const layoutFixedNodes = document.querySelectorAll<HTMLElement>(
      `.${SCROLL_FIXED_CLASS}`,
    );
    const nodes = [...layoutFixedNodes];
    if (nodes.length > 0) {
      nodes.forEach((node) => {
        node.style.paddingRight = '';
        requestAnimationFrame(() => {
          node.style.transition = node.dataset.transition || '';
        });
      });
    }
    document.body.style.paddingRight = '';
  });
}
