import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { fenToYuan } from '@vben/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择活动状态',
        allowClear: true,
      },
    },
  ];
}

/** 列表的表格列 */
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
        props: {
          height: 40,
        },
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
      formatter: ({ row }) => `￥${fenToYuan(row.marketPrice)}`,
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
      formatter: ({ row }) => {
        return (row.totalStock || 0) - (row.stock || 0);
      },
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

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
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
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入排序',
        class: '!w-full',
      },
      defaultValue: 0,
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
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'spuId',
      label: '活动商品',
      component: 'Input',
      rules: 'required',
      formItemClass: 'col-span-2',
      renderComponentContent: () => ({
        default: () => null,
      }),
    },
  ];
}
