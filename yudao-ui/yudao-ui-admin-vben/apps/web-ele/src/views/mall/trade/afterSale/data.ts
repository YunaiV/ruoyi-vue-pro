import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getRangePickerDefaultProps } from '#/utils';

/** 拒绝售后表单的 schema 配置 */
export function useDisagreeFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      component: 'Textarea',
      fieldName: 'reason',
      label: '拒绝原因',
      componentProps: {
        placeholder: '请输入拒绝原因',
        rows: 4,
      },
      rules: z.string().min(2, { message: '拒绝原因不能少于 2 个字符' }),
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'spuName',
      label: '商品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入商品名称',
      },
    },
    {
      fieldName: 'no',
      label: '退款编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入退款编号',
      },
    },
    {
      fieldName: 'orderNo',
      label: '订单编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入订单编号',
      },
    },
    {
      fieldName: 'status',
      label: '售后状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_STATUS, 'number'),
        placeholder: '请选择售后状态',
        clearable: true,
      },
    },
    {
      fieldName: 'way',
      label: '售后方式',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_WAY, 'number'),
        placeholder: '请选择售后方式',
        clearable: true,
      },
    },
    {
      fieldName: 'type',
      label: '售后类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.TRADE_AFTER_SALE_TYPE, 'number'),
        placeholder: '请选择售后类型',
        clearable: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridPropTypes.Columns {
  return [
    {
      field: 'no',
      title: '退款编号',
      fixed: 'left',
      minWidth: 200,
    },
    {
      field: 'orderNo',
      title: '订单编号',
      fixed: 'left',
      minWidth: 200,
      slots: { default: 'orderNo' },
    },
    {
      field: 'productInfo',
      title: '商品信息',
      minWidth: 600,
      slots: { default: 'productInfo' },
    },
    {
      field: 'refundPrice',
      title: '订单金额',
      width: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'user.nickname',
      title: '买家',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '申请时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '售后状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: {
          type: DICT_TYPE.TRADE_AFTER_SALE_STATUS,
        },
      },
    },
    {
      field: 'way',
      title: '售后方式',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: {
          type: DICT_TYPE.TRADE_AFTER_SALE_WAY,
        },
      },
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      align: 'center',
      slots: { default: 'actions' },
    },
  ];
}
