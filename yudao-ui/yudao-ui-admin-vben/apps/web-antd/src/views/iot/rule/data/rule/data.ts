import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '规则名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入规则名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '规则状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 规则表单 Schema */
export function useRuleFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        show: false,
        triggerFields: ['id'],
      },
    },
    {
      fieldName: 'name',
      label: '规则名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入规则名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '规则描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入规则描述',
        rows: 3,
      },
    },
    {
      fieldName: 'status',
      label: '规则状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
      rules: 'required',
    },
    {
      fieldName: 'sinkIds',
      label: '数据目的',
      component: 'Select',
      componentProps: {
        placeholder: '请选择数据目的',
        mode: 'multiple',
        allowClear: true,
        options: [],
      },
      rules: 'required',
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '规则编号',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '规则名称',
      minWidth: 150,
    },
    {
      field: 'description',
      title: '规则描述',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '规则状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'sourceConfigs',
      title: '数据源',
      minWidth: 100,
      formatter: ({ cellValue }: any) => `${cellValue?.length || 0} 个`,
    },
    {
      field: 'sinkIds',
      title: '数据目的',
      minWidth: 100,
      formatter: ({ cellValue }: any) => `${cellValue?.length || 0} 个`,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
