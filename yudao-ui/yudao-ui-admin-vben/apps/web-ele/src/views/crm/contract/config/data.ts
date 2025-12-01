import type { VbenFormSchema } from '#/adapter/form';

import { z } from '#/adapter/form';

export const schema: VbenFormSchema[] = [
  {
    component: 'RadioGroup',
    fieldName: 'notifyEnabled',
    label: '提前提醒设置',
    componentProps: {
      options: [
        { label: '提醒', value: true },
        { label: '不提醒', value: false },
      ],
    },
    defaultValue: true,
  },
  {
    component: 'Input',
    fieldName: 'notifyDays',
    componentProps: {
      placeholder: '请输入天数',
      class: '!w-full',
    },
    renderComponentContent: () => ({
      prepend: () => '提前',
      append: () => '天提醒',
    }),
    rules: z.coerce.number().int().min(0, '天数不能小于 0'),
    dependencies: {
      triggerFields: ['notifyEnabled'],
      show: (values) => values.notifyEnabled,
      trigger(values) {
        if (!values.notifyEnabled) {
          values.notifyDays = undefined;
        }
      },
    },
  },
];
