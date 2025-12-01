import type { Component } from 'vue';

import type { BaseFormComponentType } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { defineAsyncComponent, defineComponent, h, ref } from 'vue';

import { ApiComponent, globalShareState, IconPicker } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { notification } from '#/adapter/tdesign';

/**
 * 通用组件共同的使用的基础组件，原先放在 adapter/form 内部，限制了使用范围，这里提取出来，方便其他地方使用
 * 可用于 vben-form、vben-modal、vben-drawer 等组件使用,
 */

const AutoComplete = defineAsyncComponent(
  () => import('tdesign-vue-next/es/auto-complete'),
);
const Button = defineAsyncComponent(() => import('tdesign-vue-next/es/button'));
const Checkbox = defineAsyncComponent(
  () => import('tdesign-vue-next/es/checkbox'),
);
const CheckboxGroup = defineAsyncComponent(() =>
  import('tdesign-vue-next/es/checkbox').then((res) => res.CheckboxGroup),
);
const DatePicker = defineAsyncComponent(
  () => import('tdesign-vue-next/es/date-picker'),
);
const Divider = defineAsyncComponent(
  () => import('tdesign-vue-next/es/divider'),
);
const Input = defineAsyncComponent(() => import('tdesign-vue-next/es/input'));
const InputNumber = defineAsyncComponent(
  () => import('tdesign-vue-next/es/input-number'),
);
// const InputPassword = defineAsyncComponent(() =>
//   import('tdesign-vue-next/es/input').then((res) => res.InputPassword),
// );
// const Mentions = defineAsyncComponent(
//   () => import('tdesign-vue-next/es/mentions'),
// );
const Radio = defineAsyncComponent(() => import('tdesign-vue-next/es/radio'));
const RadioGroup = defineAsyncComponent(() =>
  import('tdesign-vue-next/es/radio').then((res) => res.RadioGroup),
);
const RangePicker = defineAsyncComponent(() =>
  import('tdesign-vue-next/es/date-picker').then((res) => res.DateRangePicker),
);
const Rate = defineAsyncComponent(() => import('tdesign-vue-next/es/rate'));
const Select = defineAsyncComponent(() => import('tdesign-vue-next/es/select'));
const Space = defineAsyncComponent(() => import('tdesign-vue-next/es/space'));
const Switch = defineAsyncComponent(() => import('tdesign-vue-next/es/switch'));
const Textarea = defineAsyncComponent(
  () => import('tdesign-vue-next/es/textarea'),
);
const TimePicker = defineAsyncComponent(
  () => import('tdesign-vue-next/es/time-picker'),
);
const TreeSelect = defineAsyncComponent(
  () => import('tdesign-vue-next/es/tree-select'),
);
const Upload = defineAsyncComponent(() => import('tdesign-vue-next/es/upload'));

const withDefaultPlaceholder = <T extends Component>(
  component: T,
  type: 'input' | 'select',
  componentProps: Recordable<any> = {},
) => {
  return defineComponent({
    name: component.name,
    inheritAttrs: false,
    setup: (props: any, { attrs, expose, slots }) => {
      const placeholder =
        props?.placeholder ||
        attrs?.placeholder ||
        $t(`ui.placeholder.${type}`);
      // 透传组件暴露的方法
      const innerRef = ref();
      expose(
        new Proxy(
          {},
          {
            get: (_target, key) => innerRef.value?.[key],
            has: (_target, key) => key in (innerRef.value || {}),
          },
        ),
      );
      return () =>
        h(
          component,
          { ...componentProps, placeholder, ...props, ...attrs, ref: innerRef },
          slots,
        );
    },
  });
};

// 这里需要自行根据业务组件库进行适配，需要用到的组件都需要在这里类型说明
export type ComponentType =
  | 'ApiSelect'
  | 'ApiTreeSelect'
  | 'AutoComplete'
  | 'Checkbox'
  | 'CheckboxGroup'
  | 'DatePicker'
  | 'DefaultButton'
  | 'Divider'
  | 'IconPicker'
  | 'Input'
  | 'InputNumber'
  // | 'InputPassword'
  // | 'Mentions'
  | 'PrimaryButton'
  | 'Radio'
  | 'RadioGroup'
  | 'RangePicker'
  | 'Rate'
  | 'Select'
  | 'Space'
  | 'Switch'
  | 'Textarea'
  | 'TimePicker'
  | 'TreeSelect'
  | 'Upload'
  | BaseFormComponentType;

async function initComponentAdapter() {
  const components: Partial<Record<ComponentType, Component>> = {
    // 如果你的组件体积比较大，可以使用异步加载
    // Button: () =>
    // import('xxx').then((res) => res.Button),
    ApiSelect: withDefaultPlaceholder(
      {
        ...ApiComponent,
        name: 'ApiSelect',
      },
      'select',
      {
        component: Select,
        loadingSlot: 'suffixIcon',
        visibleEvent: 'onDropdownVisibleChange',
        modelPropName: 'value',
      },
    ),
    ApiTreeSelect: withDefaultPlaceholder(
      {
        ...ApiComponent,
        name: 'ApiTreeSelect',
      },
      'select',
      {
        component: TreeSelect,
        fieldNames: { label: 'label', value: 'value', children: 'children' },
        loadingSlot: 'suffixIcon',
        modelPropName: 'value',
        optionsPropName: 'treeData',
        visibleEvent: 'onVisibleChange',
      },
    ),
    AutoComplete,
    Checkbox,
    CheckboxGroup,
    DatePicker,
    // 自定义默认按钮
    DefaultButton: (props, { attrs, slots }) => {
      return h(Button, { ...props, attrs, theme: 'default' }, slots);
    },
    Divider,
    IconPicker: withDefaultPlaceholder(IconPicker, 'select', {
      iconSlot: 'addonAfter',
      inputComponent: Input,
      modelValueProp: 'value',
    }),
    Input: withDefaultPlaceholder(Input, 'input'),
    InputNumber: withDefaultPlaceholder(InputNumber, 'input'),
    // InputPassword: withDefaultPlaceholder(InputPassword, 'input'),
    // Mentions: withDefaultPlaceholder(Mentions, 'input'),
    // 自定义主要按钮
    PrimaryButton: (props, { attrs, slots }) => {
      let ghost = false;
      let variant = props.variant;
      if (props.variant === 'ghost') {
        ghost = true;
        variant = 'base';
      }
      return h(
        Button,
        { ...props, ghost, variant, attrs, theme: 'primary' },
        slots,
      );
    },
    Radio,
    RadioGroup,
    RangePicker: (props, { attrs, slots }) => {
      return h(
        RangePicker,
        { ...props, modelValue: props.modelValue ?? [], attrs },
        slots,
      );
    },
    Rate,
    Select: withDefaultPlaceholder(Select, 'select'),
    Space,
    Switch,
    Textarea: withDefaultPlaceholder(Textarea, 'input'),
    TimePicker,
    TreeSelect: withDefaultPlaceholder(TreeSelect, 'select'),
    Upload,
  };

  // 将组件注册到全局共享状态中
  globalShareState.setComponents(components);

  // 定义全局共享状态中的消息提示
  globalShareState.defineMessage({
    // 复制成功消息提示
    copyPreferencesSuccess: (title, content) => {
      notification.success({
        title,
        content,
        placement: 'bottom-right',
      });
    },
  });
}

export { initComponentAdapter };
