import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

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
      component: 'Input',
      fieldName: 'name',
      label: '岗位名称',
      componentProps: {
        placeholder: '请输入岗位名称',
      },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'code',
      label: '岗位编码',
      componentProps: {
        placeholder: '请输入岗位编码',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '显示顺序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入显示顺序',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '岗位状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'remark',
      label: '岗位备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入岗位备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '岗位名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入岗位名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'code',
      label: '岗位编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入岗位编码',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '岗位状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择岗位状态',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '岗位编号',
      minWidth: 200,
    },
    {
      field: 'name',
      title: '岗位名称',
      minWidth: 200,
    },
    {
      field: 'code',
      title: '岗位编码',
      minWidth: 200,
    },
    {
      field: 'sort',
      title: '显示顺序',
      minWidth: 100,
    },
    {
      field: 'remark',
      title: '岗位备注',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '岗位状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
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
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
