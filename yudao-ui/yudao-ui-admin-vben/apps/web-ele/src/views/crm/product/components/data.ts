import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

/** 产品详情列表的列定义 */
export function useDetailListColumns(
  showBusinessPrice: boolean,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'productName',
      title: '产品名称',
    },
    {
      field: 'productNo',
      title: '产品条码',
    },
    {
      field: 'productUnit',
      title: '产品单位',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_PRODUCT_UNIT },
      },
    },
    {
      field: 'productPrice',
      title: '产品价格（元）',
      formatter: 'formatAmount2',
    },
    {
      field: 'businessPrice',
      title: '商机价格（元）',
      formatter: 'formatAmount2',
      visible: showBusinessPrice,
    },
    {
      field: 'contractPrice',
      title: '合同价格（元）',
      formatter: 'formatAmount2',
      visible: !showBusinessPrice,
    },
    {
      field: 'count',
      title: '数量',
      formatter: 'formatAmount3',
    },
    {
      field: 'totalPrice',
      title: '合计金额（元）',
      formatter: 'formatAmount2',
    },
  ];
}

/** 产品编辑表格的列定义 */
export function useProductEditTableColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', title: '序号', minWidth: 50 },
    {
      field: 'productId',
      title: '产品名称',
      minWidth: 100,
      slots: { default: 'productId' },
    },
    {
      field: 'productNo',
      title: '条码',
      minWidth: 150,
    },
    {
      field: 'productUnit',
      title: '单位',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_PRODUCT_UNIT },
      },
    },
    {
      field: 'productPrice',
      title: '价格（元）',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'sellingPrice',
      title: '售价（元）',
      minWidth: 100,
      slots: { default: 'sellingPrice' },
    },
    {
      field: 'count',
      title: '数量',
      minWidth: 100,
      slots: { default: 'count' },
    },
    {
      field: 'totalPrice',
      title: '合计',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
