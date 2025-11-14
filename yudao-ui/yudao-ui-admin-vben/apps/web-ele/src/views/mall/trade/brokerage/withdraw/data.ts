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
      fieldName: 'type',
      label: '提现类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择提现类型',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.BROKERAGE_WITHDRAW_TYPE, 'number'),
      },
    },
    {
      fieldName: 'userAccount',
      label: '账号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入账号',
        allowClear: true,
      },
    },
    {
      fieldName: 'userName',
      label: '真实姓名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入真实姓名',
        allowClear: true,
      },
    },
    {
      fieldName: 'bankName',
      label: '提现银行',
      component: 'Select',
      componentProps: {
        placeholder: '请选择提现银行',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.BROKERAGE_BANK_NAME, 'string'),
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.BROKERAGE_WITHDRAW_STATUS, 'number'),
      },
    },
    {
      fieldName: 'createTime',
      label: '申请时间',
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
      minWidth: 80,
    },
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 80,
    },
    {
      field: 'userNickname',
      title: '用户昵称',
      minWidth: 80,
    },
    {
      field: 'price',
      title: '提现金额',
      minWidth: 80,
      formatter: 'formatAmount2',
    },
    {
      field: 'feePrice',
      title: '提现手续费',
      minWidth: 80,
      formatter: 'formatAmount2',
    },
    {
      field: 'type',
      title: '提现方式',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BROKERAGE_WITHDRAW_TYPE },
      },
    },
    {
      title: '提现信息',
      minWidth: 200,
      slots: { default: 'withdraw-info' },
    },
    {
      field: 'createTime',
      title: '申请时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      title: '状态',
      minWidth: 200,
      slots: { default: 'status-info' },
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
