import type { BacktopProps } from './backtop';

import { onMounted, ref, shallowRef } from 'vue';

import { useEventListener, useThrottleFn } from '@vueuse/core';

export const useBackTop = (props: BacktopProps) => {
  const el = shallowRef<HTMLElement>();
  const container = shallowRef<Document | HTMLElement>();
  const visible = ref(false);

  const handleScroll = () => {
    if (el.value) {
      visible.value = el.value.scrollTop >= (props?.visibilityHeight ?? 0);
    }
  };

  const handleClick = () => {
    el.value?.scrollTo({ behavior: 'smooth', top: 0 });
  };

  const handleScrollThrottled = useThrottleFn(handleScroll, 300, true);

  useEventListener(container, 'scroll', handleScrollThrottled);
  onMounted(() => {
    container.value = document;
    el.value = document.documentElement;

    if (props.target) {
      el.value = document.querySelector<HTMLElement>(props.target) ?? undefined;

      if (!el.value) {
        throw new Error(`target does not exist: ${props.target}`);
      }
      container.value = el.value;
    }
    // Give visible an initial value, fix #13066
    handleScroll();
  });

  return {
    handleClick,
    visible,
  };
};
