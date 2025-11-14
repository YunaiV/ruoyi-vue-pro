import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { formatDateTime } from '@vben/utils';

/** 新增/修改的表单 */
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
      label: '商品',
      component: 'Select',
      componentProps: {
        options: [
          { label: '华为手机 --- 1.00元', value: 1 },
          { label: '小米电视 --- 10.00元', value: 2 },
          { label: '苹果手表 --- 100.00元', value: 3 },
          { label: '华硕笔记本 --- 1000.00元', value: 4 },
          { label: '蔚来汽车 --- 200000.00元', value: 5 },
        ],
        placeholder: '请选择下单商品',
        clearable: true,
      },
      rules: 'required',
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '订单编号',
      minWidth: 100,
    },
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 100,
    },
    {
      field: 'spuName',
      title: '商品名字',
      minWidth: 150,
    },
    {
      field: 'price',
      title: '支付价格',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'refundPrice',
      title: '退款金额',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'payOrderId',
      title: '支付单号',
      minWidth: 120,
    },
    {
      field: 'payStatus',
      title: '是否支付',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'payTime',
      title: '支付时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'refundTime',
      title: '退款时间',
      minWidth: 180,
      formatter: ({ cellValue, row }) => {
        if (cellValue) {
          return formatDateTime(cellValue) as string;
        }
        if (row.payRefundId) {
          return '退款中，等待退款结果';
        }
        return '';
      },
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
