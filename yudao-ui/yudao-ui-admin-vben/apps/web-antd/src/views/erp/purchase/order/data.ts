import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { erpPriceInputFormatter } from '@vben/utils';

import { z } from '#/adapter/form';
import { getAccountSimpleList } from '#/api/erp/finance/account';
import { getProductSimpleList } from '#/api/erp/product/product';
import { getSupplierSimpleList } from '#/api/erp/purchase/supplier';
import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 表单的配置项 */
export function useFormSchema(formType: string): VbenFormSchema[] {
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
      fieldName: 'no',
      label: '订单单号',
      component: 'Input',
      componentProps: {
        placeholder: '系统自动生成',
        disabled: true,
      },
    },
    {
      fieldName: 'orderTime',
      label: '订单时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '选择订单时间',
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
      },
      rules: 'required',
    },
    {
      label: '供应商',
      fieldName: 'supplierId',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择供应商',
        allowClear: true,
        showSearch: true,
        api: getSupplierSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        autoSize: { minRows: 1, maxRows: 1 },
        disabled: formType === 'detail',
      },
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'fileUrl',
      label: '附件',
      component: 'FileUpload',
      componentProps: {
        maxNumber: 1,
        maxSize: 10,
        accept: [
          'pdf',
          'doc',
          'docx',
          'xls',
          'xlsx',
          'txt',
          'jpg',
          'jpeg',
          'png',
        ],
        showDescription: formType !== 'detail',
        disabled: formType === 'detail',
      },
      formItemClass: 'col-span-3',
    },
    {
      fieldName: 'items',
      label: '采购产品清单',
      component: 'Input',
      formItemClass: 'col-span-3',
    },
    {
      fieldName: 'discountPercent',
      label: '优惠率(%)',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入优惠率',
        min: 0,
        max: 100,
        precision: 2,
      },
      rules: z.number().min(0).optional(),
    },
    {
      fieldName: 'discountPrice',
      label: '付款优惠',
      component: 'InputNumber',
      componentProps: {
        placeholder: '付款优惠',
        precision: 2,
        formatter: erpPriceInputFormatter,
        disabled: true,
      },
    },
    {
      fieldName: 'totalPrice',
      label: '优惠后金额',
      component: 'InputNumber',
      componentProps: {
        placeholder: '优惠后金额',
        precision: 2,
        formatter: erpPriceInputFormatter,
        disabled: true,
      },
    },
    {
      fieldName: 'accountId',
      label: '结算账户',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择结算账户',
        allowClear: true,
        showSearch: true,
        api: getAccountSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
    },
    {
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入支付订金',
        precision: 2,
        min: 0,
      },
      fieldName: 'depositPrice',
      label: '支付订金',
      rules: z.number().min(0).optional(),
    },
  ];
}

/** 表单的明细表格列 */
export function useFormItemColumns(
  disabled: boolean,
): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', title: '序号', minWidth: 50, fixed: 'left' },
    {
      field: 'productId',
      title: '产品名称',
      minWidth: 200,
      slots: { default: 'productId' },
    },
    {
      field: 'stockCount',
      title: '库存',
      minWidth: 80,
    },
    {
      field: 'productBarCode',
      title: '条码',
      minWidth: 120,
    },
    {
      field: 'productUnitName',
      title: '单位',
      minWidth: 80,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 150,
      slots: { default: 'remark' },
    },
    {
      field: 'count',
      title: '数量',
      minWidth: 120,
      fixed: 'right',
      slots: { default: 'count' },
    },
    {
      field: 'productPrice',
      title: '产品单价',
      minWidth: 120,
      fixed: 'right',
      slots: { default: 'productPrice' },
    },
    {
      field: 'totalProductPrice',
      title: '金额',
      minWidth: 120,
      fixed: 'right',
      formatter: 'formatAmount2',
    },
    {
      field: 'taxPercent',
      title: '税率(%)',
      minWidth: 105,
      fixed: 'right',
      slots: { default: 'taxPercent' },
    },
    {
      field: 'taxPrice',
      title: '税额',
      minWidth: 120,
      fixed: 'right',
      formatter: 'formatAmount2',
    },
    {
      field: 'totalPrice',
      title: '税额合计',
      minWidth: 120,
      fixed: 'right',
      formatter: 'formatAmount2',
    },
    {
      title: '操作',
      width: 50,
      fixed: 'right',
      slots: { default: 'actions' },
      visible: !disabled,
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'no',
      label: '订单单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入订单单号',
        allowClear: true,
      },
    },
    {
      fieldName: 'productId',
      label: '产品',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择产品',
        allowClear: true,
        showSearch: true,
        api: getProductSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
    },
    {
      fieldName: 'orderTime',
      label: '订单时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择供应商',
        allowClear: true,
        showSearch: true,
        api: getSupplierSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
    },
    {
      fieldName: 'creator',
      label: '创建人',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择创建人',
        allowClear: true,
        showSearch: true,
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.ERP_AUDIT_STATUS, 'number'),
        placeholder: '请选择状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
        allowClear: true,
      },
    },
    {
      fieldName: 'inStatus',
      label: '入库状态',
      component: 'Select',
      componentProps: {
        options: [
          { label: '未入库', value: 0 },
          { label: '部分入库', value: 1 },
          { label: '全部入库', value: 2 },
        ],
        placeholder: '请选择入库状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'returnStatus',
      label: '退货状态',
      component: 'Select',
      componentProps: {
        options: [
          { label: '未退货', value: 0 },
          { label: '部分退货', value: 1 },
          { label: '全部退货', value: 2 },
        ],
        placeholder: '请选择退货状态',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      width: 50,
      fixed: 'left',
    },
    {
      field: 'no',
      title: '订单单号',
      width: 200,
      fixed: 'left',
    },
    {
      field: 'productNames',
      title: '产品信息',
      showOverflow: 'tooltip',
      minWidth: 120,
    },
    {
      field: 'supplierName',
      title: '供应商',
      minWidth: 120,
    },
    {
      field: 'orderTime',
      title: '订单时间',
      width: 160,
      formatter: 'formatDate',
    },
    {
      field: 'creatorName',
      title: '创建人',
      minWidth: 120,
    },
    {
      field: 'totalCount',
      title: '总数量',
      formatter: 'formatAmount3',
      minWidth: 120,
    },
    {
      field: 'inCount',
      title: '入库数量',
      formatter: 'formatAmount3',
      minWidth: 120,
    },
    {
      field: 'returnCount',
      title: '退货数量',
      formatter: 'formatAmount3',
      minWidth: 120,
    },
    {
      field: 'totalProductPrice',
      title: '金额合计',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'totalPrice',
      title: '含税金额',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'depositPrice',
      title: '支付订金',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.ERP_AUDIT_STATUS },
      },
    },
    {
      title: '操作',
      width: 260,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
