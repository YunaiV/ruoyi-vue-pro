import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户编号',
        allowClear: true,
      },
    },
    {
      fieldName: 'userType',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
        placeholder: '请选择用户类型',
        allowClear: true,
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
      title: '编号',
      field: 'id',
      minWidth: 100,
    },
    {
      title: '用户编号',
      field: 'userId',
      minWidth: 120,
    },
    {
      title: '用户类型',
      field: 'userType',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.USER_TYPE },
      },
    },
    {
      title: '余额',
      field: 'balance',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      title: '累计支出',
      field: 'totalExpense',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      title: '累计充值',
      field: 'totalRecharge',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      title: '冻结金额',
      field: 'freezePrice',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      field: 'actions',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 钱包交易记录列表字段 */
export function useTransactionGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'title',
      title: '关联业务标题',
      minWidth: 200,
    },
    {
      field: 'price',
      title: '交易金额',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'balance',
      title: '钱包余额',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'createTime',
      title: '交易时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
  ];
}
