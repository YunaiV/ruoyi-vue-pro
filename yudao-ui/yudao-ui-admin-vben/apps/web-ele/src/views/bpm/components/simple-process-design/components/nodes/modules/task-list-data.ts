import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

/** 审批记录列表字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'assigneeUser',
      title: '审批人',
      slots: {
        default: ({ row }: { row: any }) => {
          return row.assigneeUser?.nickname || row.ownerUser?.nickname;
        },
      },
      minWidth: 100,
    },
    {
      field: 'deptName',
      title: '部门',
      slots: {
        default: ({ row }: { row: any }) => {
          return row.assigneeUser?.deptName || row.ownerUser?.deptName;
        },
      },
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '开始时间',
      formatter: 'formatDateTime',
      minWidth: 140,
    },
    {
      field: 'endTime',
      title: '结束时间',
      formatter: 'formatDateTime',
      minWidth: 140,
    },
    {
      field: 'status',
      title: '审批状态',
      minWidth: 90,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_TASK_STATUS },
      },
    },
    {
      field: 'reason',
      title: '审批建议',
      minWidth: 160,
    },
    {
      field: 'durationInMillis',
      title: '耗时',
      minWidth: 100,
      formatter: 'formatPast2',
    },
  ];
}
