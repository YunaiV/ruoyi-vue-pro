import type { VbenFormSchema } from '#/adapter/form';

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
    component: 'InputNumber',
    fieldName: 'notifyDays',
    componentProps: {
      min: 0,
      precision: 0,
    },
    renderComponentContent: () => ({
      addonBefore: () => '提前',
      addonAfter: () => '天提醒',
    }),
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
