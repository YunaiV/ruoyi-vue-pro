import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';
import type { MpAccountApi } from '#/api/mp/account';

import { formatDateTime } from '@vben/utils';

import { getSimpleAccountList } from '#/api/mp/account';

let accountList: MpAccountApi.Account[] = [];
getSimpleAccountList().then((data) => (accountList = data));

/** 搜索表单配置 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'accountId',
      label: '公众号',
      component: 'Select',
      componentProps: {
        options: accountList.map((item) => ({
          label: item.name,
          value: item.id,
        })),
        placeholder: '请选择公众号',
        clearable: true,
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridPropTypes.Columns {
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
      width: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
