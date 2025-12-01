import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridProps } from '#/adapter/vxe-table';

import {
  discountFormat,
  remainedCountFormat,
  usePriceFormat,
  validityTypeFormat,
} from '../formatter';

/** 搜索表单的 schema */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '优惠券名称',
      componentProps: {
        placeholder: '请输入优惠券名称',
        clearable: true,
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridProps['columns'] {
  return [
    {
      title: '优惠券名称',
      field: 'name',
      minWidth: 120,
    },
    {
      title: '优惠金额 / 折扣',
      field: 'discount',
      minWidth: 120,
      formatter: ({ row }) => discountFormat(row),
    },
    {
      title: '最低消费',
      field: 'usePrice',
      minWidth: 100,
      formatter: ({ row }) => usePriceFormat(row),
    },
    {
      title: '有效期限',
      field: 'validityType',
      minWidth: 140,
      formatter: ({ row }) => validityTypeFormat(row),
    },
    {
      title: '剩余数量',
      minWidth: 100,
      formatter: ({ row }) => remainedCountFormat(row),
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
