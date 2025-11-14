import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';

export const EVENT_EXECUTION_OPTIONS = [
  {
    label: 'start',
    value: 'start',
  },
  {
    label: 'end',
    value: 'end',
  },
];

export const EVENT_OPTIONS = [
  { label: 'create', value: 'create' },
  { label: 'assignment', value: 'assignment' },
  { label: 'complete', value: 'complete' },
  { label: 'delete', value: 'delete' },
  { label: 'update', value: 'update' },
  { label: 'timeout', value: 'timeout' },
];

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
      fieldName: 'name',
      label: '名字',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名字',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'type',
      label: '类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.BPM_PROCESS_LISTENER_TYPE, 'string'),
        allowClear: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'event',
      label: '事件',
      component: 'Select',
      componentProps: {
        options: EVENT_OPTIONS,
        allowClear: true,
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['type'],
        trigger: (values) => (values.event = undefined),
        componentProps: (values) => ({
          options:
            values.type === 'execution'
              ? EVENT_EXECUTION_OPTIONS
              : EVENT_OPTIONS,
        }),
      },
    },
    {
      fieldName: 'valueType',
      label: '值类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.BPM_PROCESS_LISTENER_VALUE_TYPE,
          'string',
        ),
        allowClear: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'value',
      label: '类路径|表达式',
      component: 'Input',
      rules: 'required',
      dependencies: {
        triggerFields: ['valueType'],
        trigger: (values) => (values.value = undefined),
        componentProps: (values) => ({
          placeholder:
            values.valueType === 'class' ? '请输入类路径' : '请输入表达式',
        }),
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '名字',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名字',
        allowClear: true,
      },
    },
    {
      fieldName: 'type',
      label: '类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择类型',
        options: getDictOptions(DICT_TYPE.BPM_PROCESS_LISTENER_TYPE, 'string'),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '名字',
      minWidth: 200,
    },
    {
      field: 'type',
      title: '类型',
      minWidth: 200,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_PROCESS_LISTENER_TYPE },
      },
    },
    {
      field: 'event',
      title: '事件',
      minWidth: 200,
    },
    {
      field: 'valueType',
      title: '值类型',
      minWidth: 200,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_PROCESS_LISTENER_VALUE_TYPE },
      },
    },
    {
      field: 'value',
      title: '值',
      minWidth: 200,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'actions',
      title: '操作',
      minWidth: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
