import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { erpPriceInputFormatter } from '@vben/utils';

import { getAccountSimpleList } from '#/api/erp/finance/account';
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
      label: '付款单号',
      component: 'Input',
      componentProps: {
        placeholder: '系统自动生成',
        disabled: true,
      },
    },
    {
      fieldName: 'paymentTime',
      label: '付款时间',
      component: 'DatePicker',
      componentProps: {
        disabled: formType === 'detail',
        placeholder: '选择付款时间',
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: 'ApiSelect',
      componentProps: {
        disabled: formType === 'detail',
        placeholder: '请选择供应商',
        clearable: true,
        filterable: true,
        api: getSupplierSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
      rules: 'required',
    },
    {
      fieldName: 'financeUserId',
      label: '财务人员',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择财务人员',
        clearable: true,
        filterable: true,
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        autosize: { minRows: 1, maxRows: 1 },
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
      label: '采购入库、退货单',
      component: 'Input',
      formItemClass: 'col-span-3',
    },
    {
      fieldName: 'accountId',
      label: '付款账户',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择付款账户',
        clearable: true,
        filterable: true,
        api: getAccountSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
    },
    {
      fieldName: 'totalPrice',
      label: '合计付款',
      component: 'InputNumber',
      componentProps: {
        placeholder: '合计付款',
        precision: 2,
        formatter: erpPriceInputFormatter,
        disabled: true,
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'discountPrice',
      label: '优惠金额',
      component: 'InputNumber',
      componentProps: {
        disabled: formType === 'detail',
        placeholder: '请输入优惠金额',
        precision: 2,
        formatter: erpPriceInputFormatter,
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'paymentPrice',
      label: '实际付款',
      component: 'InputNumber',
      componentProps: {
        placeholder: '实际付款',
        precision: 2,
        formatter: erpPriceInputFormatter,
        disabled: true,
        controlsPosition: 'right',
        class: '!w-full',
      },
      dependencies: {
        triggerFields: ['totalPrice', 'discountPrice'],
        componentProps: (values) => {
          const totalPrice = values.totalPrice || 0;
          const discountPrice = values.discountPrice || 0;
          values.paymentPrice = totalPrice - discountPrice;
          return {};
        },
      },
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
      field: 'bizNo',
      title: '采购单据编号',
      minWidth: 200,
    },
    {
      field: 'totalPrice',
      title: '应付金额',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'paidPrice',
      title: '已付金额',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'paymentPrice',
      title: '本次付款',
      minWidth: 115,
      fixed: 'right',
      slots: { default: 'paymentPrice' },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 150,
      slots: { default: 'remark' },
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
      label: '付款单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入付款单号',
        clearable: true,
      },
    },
    {
      fieldName: 'paymentTime',
      label: '付款时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择供应商',
        clearable: true,
        filterable: true,
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
        clearable: true,
        filterable: true,
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
    },
    {
      fieldName: 'financeUserId',
      label: '财务人员',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择财务人员',
        clearable: true,
        filterable: true,
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
    },
    {
      fieldName: 'accountId',
      label: '付款账户',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择付款账户',
        clearable: true,
        filterable: true,
        api: getAccountSimpleList,
        labelField: 'name',
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
        clearable: true,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
        clearable: true,
      },
    },
    {
      fieldName: 'bizNo',
      label: '采购单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入采购单号',
        clearable: true,
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
      title: '付款单号',
      width: 180,
      fixed: 'left',
    },
    {
      field: 'supplierName',
      title: '供应商',
      minWidth: 120,
    },
    {
      field: 'paymentTime',
      title: '付款时间',
      width: 160,
      formatter: 'formatDate',
    },
    {
      field: 'creatorName',
      title: '创建人',
      minWidth: 120,
    },
    {
      field: 'financeUserName',
      title: '财务人员',
      minWidth: 120,
    },
    {
      field: 'accountName',
      title: '付款账户',
      minWidth: 120,
    },
    {
      field: 'totalPrice',
      title: '合计付款',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'discountPrice',
      title: '优惠金额',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'paymentPrice',
      title: '实际付款',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
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

/** 采购入库单选择表单的配置项 */
export function usePurchaseInGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'no',
      label: '入库单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入入库单号',
        clearable: true,
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: 'Input',
      componentProps: {
        disabled: true,
        placeholder: '已自动选择供应商',
      },
    },
    {
      fieldName: 'paymentStatus',
      label: '付款状态',
      component: 'Select',
      componentProps: {
        options: [
          { label: '未付款', value: 0 },
          { label: '部分付款', value: 1 },
          { label: '全部付款', value: 2 },
        ],
        placeholder: '请选择付款状态',
        clearable: true,
      },
    },
  ];
}

/** 采购入库单选择列表的字段 */
export function usePurchaseInGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      width: 50,
      fixed: 'left',
    },
    {
      field: 'no',
      title: '入库单号',
      width: 200,
      fixed: 'left',
    },
    {
      field: 'supplierName',
      title: '供应商',
      minWidth: 120,
    },
    {
      field: 'inTime',
      title: '入库时间',
      width: 160,
      formatter: 'formatDate',
    },
    {
      field: 'totalPrice',
      title: '应付金额',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'paymentPrice',
      title: '已付金额',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'unPaymentPrice',
      title: '未付金额',
      formatter: ({ row }) => {
        return erpPriceInputFormatter(row.totalPrice - row.paymentPrice || 0);
      },
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.ERP_AUDIT_STATUS },
      },
    },
  ];
}

/** 采购退货单选择表单的配置项 */
export function useSaleReturnGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'no',
      label: '退货单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入退货单号',
        clearable: true,
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: 'Input',
      componentProps: {
        disabled: true,
        placeholder: '已自动选择供应商',
      },
    },
    {
      fieldName: 'refundStatus',
      label: '退款状态',
      component: 'Select',
      componentProps: {
        options: [
          { label: '未退款', value: 0 },
          { label: '部分退款', value: 1 },
          { label: '全部退款', value: 2 },
        ],
        placeholder: '请选择退款状态',
        clearable: true,
      },
    },
  ];
}

/** 采购退货单选择列表的字段 */
export function useSaleReturnGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      width: 50,
      fixed: 'left',
    },
    {
      field: 'no',
      title: '退货单号',
      width: 200,
      fixed: 'left',
    },
    {
      field: 'supplierName',
      title: '供应商',
      minWidth: 120,
    },
    {
      field: 'returnTime',
      title: '退货时间',
      width: 160,
      formatter: 'formatDate',
    },
    {
      field: 'totalPrice',
      title: '应退金额',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'refundPrice',
      title: '已退金额',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'unRefundPrice',
      title: '未退金额',
      formatter: ({ row }) => {
        return erpPriceInputFormatter(row.totalPrice - row.refundPrice || 0);
      },
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.ERP_AUDIT_STATUS },
      },
    },
  ];
}
