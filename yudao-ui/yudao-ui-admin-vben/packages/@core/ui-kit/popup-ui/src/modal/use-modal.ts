import type { ExtendedModalApi, ModalApiOptions, ModalProps } from './modal';

import {
  defineComponent,
  h,
  inject,
  nextTick,
  provide,
  reactive,
  ref,
} from 'vue';

import { useStore } from '@vben-core/shared/store';

import { ModalApi } from './modal-api';
import VbenModal from './modal.vue';

const USER_MODAL_INJECT_KEY = Symbol('VBEN_MODAL_INJECT');

const DEFAULT_MODAL_PROPS: Partial<ModalProps> = {};

export function setDefaultModalProps(props: Partial<ModalProps>) {
  Object.assign(DEFAULT_MODAL_PROPS, props);
}

export function useVbenModal<TParentModalProps extends ModalProps = ModalProps>(
  options: ModalApiOptions = {},
) {
  // Modal一般会抽离出来，所以如果有传入 connectedComponent，则表示为外部调用，与内部组件进行连接
  // 外部的Modal通过provide/inject传递api

  const { connectedComponent } = options;
  if (connectedComponent) {
    const extendedApi = reactive({});
    const isModalReady = ref(true);
    const Modal = defineComponent(
      (props: TParentModalProps, { attrs, slots }) => {
        provide(USER_MODAL_INJECT_KEY, {
          extendApi(api: ExtendedModalApi) {
            // 不能直接给 reactive 赋值，会丢失响应
            // 不能用 Object.assign,会丢失 api 的原型函数
            Object.setPrototypeOf(extendedApi, api);
          },
          options,
          async reCreateModal() {
            isModalReady.value = false;
            await nextTick();
            isModalReady.value = true;
          },
        });
        checkProps(extendedApi as ExtendedModalApi, {
          ...props,
          ...attrs,
          ...slots,
        });
        return () =>
          h(
            isModalReady.value ? connectedComponent : 'div',
            {
              ...props,
              ...attrs,
            },
            slots,
          );
      },
      // eslint-disable-next-line vue/one-component-per-file
      {
        name: 'VbenParentModal',
        inheritAttrs: false,
      },
    );

    return [Modal, extendedApi as ExtendedModalApi] as const;
  }

  const injectData = inject<any>(USER_MODAL_INJECT_KEY, {});

  const mergedOptions = {
    ...DEFAULT_MODAL_PROPS,
    ...injectData.options,
    ...options,
  } as ModalApiOptions;

  mergedOptions.onOpenChange = (isOpen: boolean) => {
    options.onOpenChange?.(isOpen);
    injectData.options?.onOpenChange?.(isOpen);
  };

  const onClosed = mergedOptions.onClosed;
  mergedOptions.onClosed = () => {
    onClosed?.();
    if (mergedOptions.destroyOnClose) {
      injectData.reCreateModal?.();
    }
  };

  const api = new ModalApi(mergedOptions);

  const extendedApi: ExtendedModalApi = api as never;

  extendedApi.useStore = (selector) => {
    return useStore(api.store, selector);
  };

  const Modal = defineComponent(
    (props: ModalProps, { attrs, slots }) => {
      return () =>
        h(
          VbenModal,
          {
            ...props,
            ...attrs,
            modalApi: extendedApi,
          },
          slots,
        );
    },
    // eslint-disable-next-line vue/one-component-per-file
    {
      name: 'VbenModal',
      inheritAttrs: false,
    },
  );
  injectData.extendApi?.(extendedApi);

  return [Modal, extendedApi] as const;
}

async function checkProps(api: ExtendedModalApi, attrs: Record<string, any>) {
  if (!attrs || Object.keys(attrs).length === 0) {
    return;
  }
  await nextTick();

  const state = api?.store?.state;

  if (!state) {
    return;
  }

  const stateKeys = new Set(Object.keys(state));

  for (const attr of Object.keys(attrs)) {
    if (stateKeys.has(attr) && !['class'].includes(attr)) {
      // connectedComponent存在时，不要传入Modal的props，会造成复杂度提升，如果你需要修改Modal的props，请使用 useModal 或者api
      console.warn(
        `[Vben Modal]: When 'connectedComponent' exists, do not set props or slots '${attr}', which will increase complexity. If you need to modify the props of Modal, please use useVbenModal or api.`,
      );
    }
  }
}
