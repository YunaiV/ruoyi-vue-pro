import type { VbenFormSchema } from '#/adapter/form';

import { beginOfDay, endOfDay, formatDateTime } from '@vben/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'accountId',
      label: '公众号',
      component: 'Input',
    },
    {
      fieldName: 'dateRange',
      label: '时间范围',
      component: 'RangePicker',
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
      defaultValue: [
        formatDateTime(beginOfDay(new Date(Date.now() - 3600 * 1000 * 24 * 7))),
        formatDateTime(endOfDay(new Date(Date.now() - 3600 * 1000 * 24))),
      ],
    },
  ];
}
