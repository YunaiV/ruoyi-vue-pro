import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getProductSimpleList } from '#/api/erp/product/product';
import { getSupplierSimpleList } from '#/api/erp/purchase/supplier';
import { getWarehouseSimpleList } from '#/api/erp/stock/warehouse';
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
      label: '入库单号',
      component: 'Input',
      componentProps: {
        placeholder: '系统自动生成',
        disabled: true,
      },
    },
    {
      fieldName: 'inTime',
      label: '入库时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '选择入库时间',
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
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
      label: '入库产品清单',
      component: 'Input',
      formItemClass: 'col-span-3',
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
      field: 'warehouseId',
      title: '仓库名称',
      minWidth: 150,
      slots: { default: 'warehouseId' },
    },
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
      formatter: 'formatAmount3',
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
      field: 'totalPrice',
      title: '金额',
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
      label: '入库单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入入库单号',
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
      fieldName: 'inTime',
      label: '入库时间',
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
      fieldName: 'warehouseId',
      label: '仓库',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择仓库',
        allowClear: true,
        showSearch: true,
        api: getWarehouseSimpleList,
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
      title: '入库单号',
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
      field: 'inTime',
      title: '入库时间',
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
      field: 'totalPrice',
      title: '总金额',
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
