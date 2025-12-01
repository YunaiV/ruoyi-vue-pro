import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { formatDateTime } from '@vben/utils';

/** 获取表格列配置 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'cover',
      title: '图片',
      width: 360,
      slots: { default: 'cover' },
    },
    {
      field: 'title',
      title: '标题',
      slots: { default: 'title' },
    },
    {
      field: 'updateTime',
      title: '修改时间',
      formatter: ({ row }) => {
        return formatDateTime(row.updateTime * 1000);
      },
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'accountId',
      label: '公众号',
      component: 'Input',
    },
  ];
}
