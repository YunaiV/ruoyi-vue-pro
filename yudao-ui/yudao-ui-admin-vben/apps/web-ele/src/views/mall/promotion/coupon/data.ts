import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

import { getRangePickerDefaultProps } from '#/utils';

import { discountFormat } from './formatter';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'nickname',
      label: '会员昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入会员昵称',
        clearable: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '领取时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'nickname',
      title: '会员昵称',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '优惠券名称',
      minWidth: 140,
    },
    {
      field: 'productScope',
      title: '类型',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_PRODUCT_SCOPE },
      },
    },
    {
      field: 'discountType',
      title: '优惠',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_DISCOUNT_TYPE },
      },
    },
    {
      field: 'discountPrice',
      title: '优惠力度',
      minWidth: 110,
      formatter: ({ row }) => {
        return discountFormat(row);
      },
    },
    {
      field: 'takeType',
      title: '领取方式',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_COUPON_TAKE_TYPE },
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_COUPON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '领取时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'useTime',
      title: '使用时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'actions',
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
