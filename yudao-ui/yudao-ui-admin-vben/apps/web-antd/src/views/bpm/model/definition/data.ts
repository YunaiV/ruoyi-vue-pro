import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '定义编号',
      minWidth: 250,
    },
    {
      field: 'name',
      title: '流程名称',
      minWidth: 150,
    },
    {
      field: 'icon',
      title: '流程图标',
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
        props: {
          width: 24,
          height: 24,
        },
      },
    },
    {
      field: 'startUsers',
      title: '可见范围',
      minWidth: 100,
      slots: { default: 'startUsers' },
    },
    {
      field: 'modelType',
      title: '流程类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_MODEL_TYPE },
      },
    },
    {
      field: 'formType',
      title: '表单信息',
      minWidth: 150,
      slots: { default: 'formInfo' },
    },
    {
      field: 'version',
      title: '流程版本',
      minWidth: 80,
      cellRender: {
        name: 'CellTag',
      },
    },
    {
      field: 'deploymentTime',
      title: '部署时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
