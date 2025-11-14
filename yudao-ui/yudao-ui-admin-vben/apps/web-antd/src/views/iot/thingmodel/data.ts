import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'type',
      label: '功能类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.IOT_THING_MODEL_TYPE, 'number'),
        placeholder: '请选择功能类型',
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
      field: 'type',
      title: '功能类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.IOT_THING_MODEL_TYPE },
      },
    },
    {
      field: 'name',
      title: '功能名称',
      minWidth: 150,
    },
    {
      field: 'identifier',
      title: '标识符',
      minWidth: 150,
    },
    {
      field: 'dataType',
      title: '数据类型',
      minWidth: 120,
      slots: { default: 'dataType' },
    },
    {
      field: 'dataDefinition',
      title: '数据定义',
      minWidth: 200,
      slots: { default: 'dataDefinition' },
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
