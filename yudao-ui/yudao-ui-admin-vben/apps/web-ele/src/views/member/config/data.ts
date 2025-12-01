import type { VbenFormSchema } from '#/adapter/form';

export const schema: VbenFormSchema[] = [
  {
    component: 'Input',
    fieldName: 'id',
    dependencies: {
      triggerFields: [''],
      show: () => false,
    },
  },
  {
    component: 'Switch',
    fieldName: 'pointTradeDeductEnable',
    label: '积分抵扣',
    help: '下单积分是否抵用订单金额',
  },
  {
    component: 'InputNumber',
    fieldName: 'pointTradeDeductUnitPrice',
    label: '积分抵扣',
    help: '积分抵用比例(1 积分抵多少金额)，单位：元',
    componentProps: {
      min: 0,
      precision: 2,
      placeholder: '请输入积分抵扣单价',
      controlsPosition: 'right',
      class: '!w-full',
    },
  },
  {
    component: 'InputNumber',
    fieldName: 'pointTradeDeductMaxPrice',
    label: '积分抵扣最大值',
    help: '单次下单积分使用上限，0 不限制',
    componentProps: {
      min: 0,
      placeholder: '请输入积分抵扣最大值',
      controlsPosition: 'right',
      class: '!w-full',
    },
  },
  {
    component: 'InputNumber',
    fieldName: 'pointTradeGivePoint',
    label: '1 元赠送多少分',
    help: '下单支付金额按比例赠送积分（实际支付 1 元赠送多少积分）',
    componentProps: {
      min: 0,
      placeholder: '请输入赠送积分比例',
      controlsPosition: 'right',
      class: '!w-full',
    },
  },
];
