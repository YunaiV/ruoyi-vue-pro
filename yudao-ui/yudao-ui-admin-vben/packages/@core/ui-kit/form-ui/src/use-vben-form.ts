import type {
  BaseFormComponentType,
  ExtendedFormApi,
  VbenFormProps,
} from './types';

import { defineComponent, h, isReactive, onBeforeUnmount, watch } from 'vue';

import { useStore } from '@vben-core/shared/store';

import { FormApi } from './form-api';
import VbenUseForm from './vben-use-form.vue';

export function useVbenForm<
  T extends BaseFormComponentType = BaseFormComponentType,
>(options: VbenFormProps<T>) {
  const IS_REACTIVE = isReactive(options);
  const api = new FormApi(options);
  const extendedApi: ExtendedFormApi = api as never;
  extendedApi.useStore = (selector) => {
    return useStore(api.store, selector);
  };

  const Form = defineComponent(
    (props: VbenFormProps, { attrs, slots }) => {
      onBeforeUnmount(() => {
        api.unmount();
      });
      api.setState({ ...props, ...attrs });
      return () =>
        h(VbenUseForm, { ...props, ...attrs, formApi: extendedApi }, slots);
    },
    {
      name: 'VbenUseForm',
      inheritAttrs: false,
    },
  );
  // Add reactivity support
  if (IS_REACTIVE) {
    watch(
      () => options.schema,
      () => {
        api.setState({ schema: options.schema });
      },
      { immediate: true },
    );
  }

  return [Form, extendedApi] as const;
}
