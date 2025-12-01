import type { Component } from 'vue';

import type { DescInstance, DescriptionProps } from './typing';

import { h, reactive } from 'vue';

import Description from './description.vue';

export function useDescription(options?: Partial<DescriptionProps>) {
  const propsState = reactive<Partial<DescriptionProps>>(options || {});

  const api: DescInstance = {
    setDescProps: (descProps: Partial<DescriptionProps>): void => {
      Object.assign(propsState, descProps);
    },
  };

  // 创建一个包装组件，将 propsState 合并到 props 中
  const DescriptionWrapper: Component = {
    name: 'UseDescription',
    inheritAttrs: false,
    setup(_props, { attrs, slots }) {
      return () => {
        // @ts-ignore - 避免类型实例化过深
        return h(Description, { ...propsState, ...attrs }, slots);
      };
    },
  };

  return [DescriptionWrapper, api] as const;
}
