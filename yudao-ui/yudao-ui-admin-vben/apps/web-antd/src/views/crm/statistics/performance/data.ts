import type { VbenFormSchema } from '#/adapter/form';

import { useUserStore } from '@vben/stores';
import { handleTree } from '@vben/utils';

import { getSimpleDeptList } from '#/api/system/dept';
import { getSimpleUserList } from '#/api/system/user';

const userStore = useUserStore();

export const customerSummaryTabs = [
  {
    tab: '员工合同数量统计',
    key: 'ContractCountPerformance',
  },
  {
    tab: '员工合同金额统计',
    key: 'ContractPricePerformance',
  },
  {
    tab: '员工回款金额统计',
    key: 'ReceivablePricePerformance',
  },
];

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'time',
      label: '选择年份',
      component: 'DatePicker',
      componentProps: {
        picker: 'year',
        format: 'YYYY',
        valueFormat: 'YYYY',
        placeholder: '请选择年份',
      },
      defaultValue: new Date().getFullYear().toString(),
    },
    {
      fieldName: 'deptId',
      label: '归属部门',
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => {
          const data = await getSimpleDeptList();
          return handleTree(data);
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        treeDefaultExpandAll: true,
        placeholder: '请选择归属部门',
      },
      defaultValue: userStore.userInfo?.deptId,
    },
    {
      fieldName: 'userId',
      label: '员工',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '请选择员工',
        allowClear: true,
      },
    },
  ];
}
