/**
 * 通用组件共同的使用的基础组件，原先放在 adapter/form 内部，限制了使用范围，这里提取出来，方便其他地方使用
 * 可用于 vben-form、vben-modal、vben-drawer 等组件使用,
 */

import type { Component } from 'vue';

import type { BaseFormComponentType } from '@vben/common-ui';
import type { Recordable } from '@vben/types';

import { defineAsyncComponent, defineComponent, h, ref } from 'vue';

import { ApiComponent, globalShareState, IconPicker } from '@vben/common-ui';
import { $t } from '@vben/locales';

import { message } from '#/adapter/naive';
import { FileUpload, ImageUpload } from '#/components/upload';

const NButton = defineAsyncComponent(() =>
  import('naive-ui/es/button').then((res) => res.NButton),
);
const NCheckbox = defineAsyncComponent(() =>
  import('naive-ui/es/checkbox').then((res) => res.NCheckbox),
);
const NCheckboxGroup = defineAsyncComponent(() =>
  import('naive-ui/es/checkbox').then((res) => res.NCheckboxGroup),
);
const NDatePicker = defineAsyncComponent(() =>
  import('naive-ui/es/date-picker').then((res) => res.NDatePicker),
);
const NDivider = defineAsyncComponent(() =>
  import('naive-ui/es/divider').then((res) => res.NDivider),
);
const NInput = defineAsyncComponent(() =>
  import('naive-ui/es/input').then((res) => res.NInput),
);
const NInputNumber = defineAsyncComponent(() =>
  import('naive-ui/es/input-number').then((res) => res.NInputNumber),
);
const NRadio = defineAsyncComponent(() =>
  import('naive-ui/es/radio').then((res) => res.NRadio),
);
const NRadioButton = defineAsyncComponent(() =>
  import('naive-ui/es/radio').then((res) => res.NRadioButton),
);
const NRadioGroup = defineAsyncComponent(() =>
  import('naive-ui/es/radio').then((res) => res.NRadioGroup),
);
const NSelect = defineAsyncComponent(() =>
  import('naive-ui/es/select').then((res) => res.NSelect),
);
const NSpace = defineAsyncComponent(() =>
  import('naive-ui/es/space').then((res) => res.NSpace),
);
const NSwitch = defineAsyncComponent(() =>
  import('naive-ui/es/switch').then((res) => res.NSwitch),
);
const NTimePicker = defineAsyncComponent(() =>
  import('naive-ui/es/time-picker').then((res) => res.NTimePicker),
);
const NTreeSelect = defineAsyncComponent(() =>
  import('naive-ui/es/tree-select').then((res) => res.NTreeSelect),
);
const NUpload = defineAsyncComponent(() =>
  import('naive-ui/es/upload').then((res) => res.NUpload),
);

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
  | 'Checkbox'
  | 'CheckboxGroup'
  | 'DatePicker'
  | 'Divider'
  | 'FileUpload'
  | 'IconPicker'
  | 'ImageUpload'
  | 'Input'
  | 'InputNumber'
  | 'RadioGroup'
  | 'Select'
  | 'Space'
  | 'Switch'
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
        component: NSelect,
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
        component: NTreeSelect,
        nodeKey: 'value',
        loadingSlot: 'arrow',
        keyField: 'value',
        modelPropName: 'value',
        optionsPropName: 'options',
        visibleEvent: 'onVisibleChange',
      },
    ),
    Checkbox: NCheckbox,
    CheckboxGroup: (props, { attrs, slots }) => {
      let defaultSlot;
      if (Reflect.has(slots, 'default')) {
        defaultSlot = slots.default;
      } else {
        const { options } = attrs;
        if (Array.isArray(options)) {
          defaultSlot = () => options.map((option) => h(NCheckbox, option));
        }
      }
      return h(
        NCheckboxGroup,
        { ...props, ...attrs },
        { default: defaultSlot },
      );
    },
    DatePicker: NDatePicker,
    // 自定义默认按钮
    DefaultButton: (props, { attrs, slots }) => {
      return h(NButton, { ...props, attrs, type: 'default' }, slots);
    },
    // 自定义主要按钮
    PrimaryButton: (props, { attrs, slots }) => {
      return h(NButton, { ...props, attrs, type: 'primary' }, slots);
    },
    Divider: NDivider,
    IconPicker: withDefaultPlaceholder(IconPicker, 'select', {
      iconSlot: 'suffix',
      inputComponent: NInput,
    }),
    Input: withDefaultPlaceholder(NInput, 'input'),
    InputNumber: withDefaultPlaceholder(NInputNumber, 'input'),
    RadioGroup: (props, { attrs, slots }) => {
      let defaultSlot;
      if (Reflect.has(slots, 'default')) {
        defaultSlot = slots.default;
      } else {
        const { options } = attrs;
        if (Array.isArray(options)) {
          defaultSlot = () =>
            options.map((option) =>
              h(attrs.isButton ? NRadioButton : NRadio, option),
            );
        }
      }
      const groupRender = h(
        NRadioGroup,
        { ...props, ...attrs },
        { default: defaultSlot },
      );
      return attrs.isButton
        ? h(NSpace, { vertical: true }, () => groupRender)
        : groupRender;
    },
    Select: withDefaultPlaceholder(NSelect, 'select'),
    Space: NSpace,
    Switch: NSwitch,
    TimePicker: NTimePicker,
    TreeSelect: withDefaultPlaceholder(NTreeSelect, 'select'),
    Upload: NUpload,
    FileUpload,
    ImageUpload,
  };

  // 将组件注册到全局共享状态中
  globalShareState.setComponents(components);

  // 定义全局共享状态中的消息提示
  globalShareState.defineMessage({
    // 复制成功消息提示
    copyPreferencesSuccess: (title, content) => {
      message.success(content || title, {
        duration: 0,
      });
    },
  });
}

export { initComponentAdapter };
