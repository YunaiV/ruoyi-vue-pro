import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'status',
      label: '砍价状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择砍价状态',
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.PROMOTION_BARGAIN_RECORD_STATUS,
          'number',
        ),
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 50,
    },
    {
      field: 'avatar',
      title: '用户头像',
      minWidth: 120,
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
          shape: 'circle',
        },
      },
    },
    {
      field: 'nickname',
      title: '用户昵称',
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '发起时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'activity.name',
      title: '砍价活动',
      minWidth: 150,
    },
    {
      field: 'activity.bargainMinPrice',
      title: '最低价',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'bargainPrice',
      title: '当前价',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'activity.helpMaxCount',
      title: '总砍价次数',
      minWidth: 100,
    },
    {
      field: 'helpCount',
      title: '剩余砍价次数',
      minWidth: 100,
    },
    {
      field: 'status',
      title: '砍价状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PROMOTION_BARGAIN_RECORD_STATUS },
      },
    },
    {
      field: 'endTime',
      title: '结束时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'orderId',
      title: '订单编号',
      minWidth: 100,
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 助力列表表格列配置 */
export function useHelpGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 80,
    },
    {
      field: 'avatar',
      title: '用户头像',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
          shape: 'circle',
        },
      },
    },
    {
      field: 'nickname',
      title: '用户昵称',
      minWidth: 100,
    },
    {
      field: 'reducePrice',
      title: '砍价金额',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'createTime',
      title: '助力时间',
      width: 180,
      formatter: 'formatDateTime',
    },
  ];
}
