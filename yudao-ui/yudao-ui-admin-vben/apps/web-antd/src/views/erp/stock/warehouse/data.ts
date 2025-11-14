import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpWarehouseApi } from '#/api/erp/stock/warehouse';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';

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
      fieldName: 'name',
      label: '仓库名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'address',
      label: '仓库地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库地址',
      },
    },
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'warehousePrice',
      label: '仓储费(元)',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入仓储费，单位：元/天/KG',
        min: 0,
        precision: 2,
      },
    },
    {
      fieldName: 'truckagePrice',
      label: '搬运费(元)',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入搬运费，单位：元',
        min: 0,
        precision: 2,
      },
    },
    {
      fieldName: 'principal',
      label: '负责人',
      component: 'Input',
      componentProps: {
        placeholder: '请输入负责人',
      },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入排序',
        precision: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '仓库名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '仓库状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择仓库状态',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  onDefaultStatusChange?: (
    newStatus: boolean,
    row: ErpWarehouseApi.Warehouse,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'name',
      title: '仓库名称',
      minWidth: 150,
    },
    {
      field: 'address',
      title: '仓库地址',
      minWidth: 200,
      showOverflow: 'tooltip',
    },
    {
      field: 'warehousePrice',
      title: '仓储费',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'truckagePrice',
      title: '搬运费',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'principal',
      title: '负责人',
      minWidth: 100,
    },
    {
      field: 'sort',
      title: '排序',
      minWidth: 80,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'defaultStatus',
      title: '是否默认',
      minWidth: 100,
      cellRender: {
        attrs: { beforeChange: onDefaultStatusChange },
        name: 'CellSwitch',
        props: {
          checkedValue: true,
          unCheckedValue: false,
        },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 150,
      showOverflow: 'tooltip',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
