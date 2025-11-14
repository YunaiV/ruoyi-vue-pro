import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 表单配置 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'spuId',
      label: '积分商城活动商品',
      component: 'Input',
      componentProps: {
        placeholder: '请选择商品',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入排序',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 4,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        clearable: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '活动编号',
      minWidth: 80,
    },
    {
      field: 'picUrl',
      title: '商品图片',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'spuName',
      title: '商品标题',
      minWidth: 300,
    },
    {
      field: 'marketPrice',
      title: '原价',
      minWidth: 100,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'point',
      title: '兑换积分',
      minWidth: 100,
    },
    {
      field: 'price',
      title: '兑换金额',
      minWidth: 100,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'status',
      title: '活动状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'stock',
      title: '库存',
      minWidth: 80,
    },
    {
      field: 'totalStock',
      title: '总库存',
      minWidth: 80,
    },
    {
      field: 'redeemedQuantity',
      title: '已兑换数量',
      minWidth: 100,
      slots: { default: 'redeemedQuantity' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
