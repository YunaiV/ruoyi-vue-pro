import type { App, Directive, DirectiveBinding } from 'vue';

import { h, render } from 'vue';

import { VbenLoading, VbenSpinner } from '@vben-core/shadcn-ui';
import { isString } from '@vben-core/shared/utils';

const LOADING_INSTANCE_KEY = Symbol('loading');
const SPINNER_INSTANCE_KEY = Symbol('spinner');

const CLASS_NAME_RELATIVE = 'spinner-parent--relative';

const loadingDirective: Directive = {
  mounted(el, binding) {
    const instance = h(VbenLoading, getOptions(binding));
    render(instance, el);

    el.classList.add(CLASS_NAME_RELATIVE);
    el[LOADING_INSTANCE_KEY] = instance;
  },
  unmounted(el) {
    const instance = el[LOADING_INSTANCE_KEY];
    el.classList.remove(CLASS_NAME_RELATIVE);
    render(null, el);
    instance.el.remove();

    el[LOADING_INSTANCE_KEY] = null;
  },

  updated(el, binding) {
    const instance = el[LOADING_INSTANCE_KEY];
    const options = getOptions(binding);
    if (options && instance?.component) {
      try {
        Object.keys(options).forEach((key) => {
          instance.component.props[key] = options[key];
        });
        instance.component.update();
      } catch (error) {
        console.error(
          'Failed to update loading component in directive:',
          error,
        );
      }
    }
  },
};

function getOptions(binding: DirectiveBinding) {
  if (binding.value === undefined) {
    return { spinning: true };
  } else if (typeof binding.value === 'boolean') {
    return { spinning: binding.value };
  } else {
    return { ...binding.value };
  }
}

const spinningDirective: Directive = {
  mounted(el, binding) {
    const instance = h(VbenSpinner, getOptions(binding));
    render(instance, el);

    el.classList.add(CLASS_NAME_RELATIVE);
    el[SPINNER_INSTANCE_KEY] = instance;
  },
  unmounted(el) {
    const instance = el[SPINNER_INSTANCE_KEY];
    el.classList.remove(CLASS_NAME_RELATIVE);
    render(null, el);
    instance.el.remove();

    el[SPINNER_INSTANCE_KEY] = null;
  },

  updated(el, binding) {
    const instance = el[SPINNER_INSTANCE_KEY];
    const options = getOptions(binding);
    if (options && instance?.component) {
      try {
        Object.keys(options).forEach((key) => {
          instance.component.props[key] = options[key];
        });
        instance.component.update();
      } catch (error) {
        console.error(
          'Failed to update spinner component in directive:',
          error,
        );
      }
    }
  },
};

type loadingDirectiveParams = {
  /** 是否注册loading指令。如果提供一个string，则将指令注册为指定的名称 */
  loading?: boolean | string;
  /** 是否注册spinning指令。如果提供一个string，则将指令注册为指定的名称 */
  spinning?: boolean | string;
};

/**
 * 注册loading指令
 * @param app
 * @param params
 */
export function registerLoadingDirective(
  app: App,
  params?: loadingDirectiveParams,
) {
  // 注入一个样式供指令使用，确保容器是相对定位
  const style = document.createElement('style');
  style.id = CLASS_NAME_RELATIVE;
  style.innerHTML = `
    .${CLASS_NAME_RELATIVE} {
      position: relative !important;
    }
  `;
  document.head.append(style);
  if (params?.loading !== false) {
    app.directive(
      isString(params?.loading) ? params.loading : 'loading',
      loadingDirective,
    );
  }
  if (params?.spinning !== false) {
    app.directive(
      isString(params?.spinning) ? params.spinning : 'spinning',
      spinningDirective,
    );
  }
}
