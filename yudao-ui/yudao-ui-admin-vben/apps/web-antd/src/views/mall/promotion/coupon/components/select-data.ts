import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import {
  discountFormat,
  remainedCountFormat,
  takeLimitCountFormat,
  validityTypeFormat,
} from '../formatter';

/** 优惠券选择的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '优惠券名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优惠券名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'discountType',
      label: '优惠类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PROMOTION_DISCOUNT_TYPE, 'number'),
        placeholder: '请选择优惠类型',
        allowClear: true,
      },
    },
  ];
}

/** 优惠券选择的表格列 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 55 },
    {
      field: 'name',
      title: '优惠券名称',
      minWidth: 140,
    },
    {
      field: 'productScope',
      title: '类型',
      minWidth: 80,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_PRODUCT_SCOPE },
      },
    },
    {
      field: 'discountType',
      title: '优惠类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_DISCOUNT_TYPE },
      },
    },
    {
      field: 'discountPrice',
      title: '优惠力度',
      minWidth: 100,
      formatter: ({ row }) => discountFormat(row),
    },
    {
      field: 'takeType',
      title: '领取方式',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_COUPON_TAKE_TYPE },
      },
    },
    {
      field: 'validityType',
      title: '使用时间',
      minWidth: 185,
      align: 'center',
      formatter: ({ row }) => validityTypeFormat(row),
    },
    {
      field: 'totalCount',
      title: '发放数量',
      minWidth: 100,
      align: 'center',
    },
    {
      field: 'remainedCount',
      title: '剩余数量',
      minWidth: 100,
      align: 'center',
      formatter: ({ row }) => remainedCountFormat(row),
    },
    {
      field: 'takeLimitCount',
      title: '领取上限',
      minWidth: 100,
      align: 'center',
      formatter: ({ row }) => takeLimitCountFormat(row),
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      align: 'center',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
  ];
}
