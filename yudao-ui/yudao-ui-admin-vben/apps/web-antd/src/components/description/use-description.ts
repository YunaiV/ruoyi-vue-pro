import type { DescriptionsOptions } from './typing';

import { defineComponent, h, isReactive, reactive, watch } from 'vue';

import Description from './description.vue';

/** 描述列表 api 定义 */
class DescriptionApi {
  private state = reactive<Record<string, any>>({});

  constructor(options: DescriptionsOptions) {
    this.state = { ...options };
  }

  getState(): DescriptionsOptions {
    return this.state as DescriptionsOptions;
  }

  // TODO @xingyu：【setState】纠结下：1）vben2.0 是 data https://doc.vvbin.cn/components/desc.html#usage；
  setState(newState: Partial<DescriptionsOptions>) {
    this.state = { ...this.state, ...newState };
  }
}

export type ExtendedDescriptionApi = DescriptionApi;

export function useDescription(options: DescriptionsOptions) {
  const IS_REACTIVE = isReactive(options);
  const api = new DescriptionApi(options);
  // 扩展 API
  const extendedApi: ExtendedDescriptionApi = api as never;
  const Desc = defineComponent({
    name: 'UseDescription',
    inheritAttrs: false,
    setup(_, { attrs, slots }) {
      // 合并props和attrs到state
      api.setState({ ...attrs });

      return () =>
        h(
          Description,
          {
            ...api.getState(),
            ...attrs,
          },
          slots,
        );
    },
  });

  // 响应式支持
  if (IS_REACTIVE) {
    watch(
      () => options.schema,
      (newSchema) => {
        api.setState({ schema: newSchema });
      },
      { immediate: true, deep: true },
    );

    watch(
      () => options.data,
      (newData) => {
        api.setState({ data: newData });
      },
      { immediate: true, deep: true },
    );
  }

  return [Desc, extendedApi] as const;
}
