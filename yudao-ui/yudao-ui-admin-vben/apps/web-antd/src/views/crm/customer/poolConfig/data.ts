import type { VbenFormSchema } from '#/adapter/form';

export const schema: VbenFormSchema[] = [
  {
    component: 'RadioGroup',
    fieldName: 'enabled',
    label: '客户公海规则设置',
    componentProps: {
      options: [
        { label: '开启', value: true },
        { label: '关闭', value: false },
      ],
    },
  },
  {
    component: 'InputNumber',
    fieldName: 'contactExpireDays',
    componentProps: {
      min: 0,
      precision: 0,
    },
    renderComponentContent: () => ({
      addonAfter: () => '天不跟进或',
    }),
    dependencies: {
      triggerFields: ['enabled'],
      show: (value) => value.enabled,
    },
  },
  {
    component: 'InputNumber',
    fieldName: 'dealExpireDays',
    renderComponentContent: () => ({
      addonBefore: () => '或',
      addonAfter: () => '天未成交',
    }),
    componentProps: {
      min: 0,
      precision: 0,
    },
    dependencies: {
      triggerFields: ['enabled'],
      show: (value) => value.enabled,
    },
  },
  {
    component: 'RadioGroup',
    fieldName: 'notifyEnabled',
    label: '提前提醒设置',
    componentProps: {
      options: [
        { label: '开启', value: true },
        { label: '关闭', value: false },
      ],
    },
    dependencies: {
      triggerFields: ['enabled'],
      show: (value) => value.enabled,
    },
    defaultValue: false,
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
      show: (value) => value.enabled && value.notifyEnabled,
    },
  },
];
