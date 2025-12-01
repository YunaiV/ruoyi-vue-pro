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
      label: '目的名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目的名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '目的状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'type',
      label: '目的类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_DATA_SINK_TYPE_ENUM, 'number'),
        placeholder: '请选择目的类型',
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

/** 目的表单 Schema */
export function useSinkFormSchema(): VbenFormSchema[] {
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
      label: '目的名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目的名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '目的描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入目的描述',
        rows: 3,
      },
    },
    {
      fieldName: 'type',
      label: '目的类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_DATA_SINK_TYPE_ENUM, 'number'),
        placeholder: '请选择目的类型',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '目的状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
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
      title: '目的编号',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '目的名称',
      minWidth: 150,
    },
    {
      field: 'description',
      title: '目的描述',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '目的状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'type',
      title: '目的类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.IOT_DATA_SINK_TYPE_ENUM },
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
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
