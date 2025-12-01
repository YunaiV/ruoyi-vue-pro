import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { BpmModelApi } from '#/api/bpm/model';

import { DICT_TYPE } from '@vben/constants';

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<BpmModelApi.Model>['columns'] {
  return [
    {
      field: 'name',
      title: '流程名称',
      minWidth: 200,
      slots: { default: 'name' },
    },
    {
      field: 'startUserIds',
      title: '可见范围',
      minWidth: 150,
      slots: { default: 'startUserIds' },
    },
    {
      field: 'type',
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
      field: 'deploymentTime',
      title: '最后发布',
      minWidth: 280,
      slots: { default: 'deploymentTime' },
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
