import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '流程名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '抄送时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'processInstanceName',
      title: '流程名称',
      minWidth: 200,
      fixed: 'left',
    },
    {
      field: 'summary',
      title: '摘要',
      minWidth: 200,
      formatter: ({ cellValue }) => {
        return cellValue && cellValue.length > 0
          ? cellValue
              .map((item: any) => `${item.key} : ${item.value}`)
              .join('\n')
          : '-';
      },
    },
    {
      field: 'startUser.nickname',
      title: '流程发起人',
      minWidth: 120,
    },
    {
      field: 'processInstanceStartTime',
      title: '流程发起时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'activityName',
      title: '抄送节点',
      minWidth: 180,
    },
    {
      field: 'createUser.nickname',
      title: '抄送人',
      minWidth: 180,
      formatter: ({ cellValue }) => {
        return cellValue || '-';
      },
    },
    {
      field: 'reason',
      title: '抄送意见',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '抄送时间',
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
