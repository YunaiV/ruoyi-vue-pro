import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { useUserStore } from '@vben/stores';
import { beginOfDay, endOfDay, formatDateTime, handleTree } from '@vben/utils';

import { getSimpleDeptList } from '#/api/system/dept';
import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

const userStore = useUserStore();

export const customerSummaryTabs = [
  {
    tab: '城市分布分析',
    key: 'area',
  },
  {
    tab: '客户级别分析',
    key: 'level',
  },
  {
    tab: '客户来源分析',
    key: 'source',
  },
  {
    tab: '客户行业分析',
    key: 'industry',
  },
];

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'times',
      label: '时间范围',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
      },
      defaultValue: [
        formatDateTime(beginOfDay(new Date(Date.now() - 3600 * 1000 * 24 * 7))),
        formatDateTime(endOfDay(new Date(Date.now() - 3600 * 1000 * 24))),
      ],
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

/** 列表的字段 */
export function useGridColumns(
  activeTabName: any,
): VxeTableGridOptions['columns'] {
  switch (activeTabName) {
    case 'industry': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'industryId',
          title: '客户行业',
          minWidth: 100,
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.CRM_CUSTOMER_INDUSTRY },
          },
        },
        {
          field: 'customerCount',
          title: '客户个数',
          minWidth: 200,
        },
        {
          field: 'dealCount',
          title: '成交个数',
          minWidth: 200,
        },
        {
          field: 'industryPortion',
          title: '行业占比(%)',
          minWidth: 200,
        },
        {
          field: 'dealPortion',
          title: '成交占比(%)',
          minWidth: 200,
        },
      ];
    }
    case 'level': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'level',
          title: '客户级别',
          minWidth: 100,
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.CRM_CUSTOMER_LEVEL },
          },
        },
        {
          field: 'customerCount',
          title: '客户个数',
          minWidth: 200,
        },
        {
          field: 'dealCount',
          title: '成交个数',
          minWidth: 200,
        },
        {
          field: 'industryPortion',
          title: '行业占比(%)',
          minWidth: 200,
        },
        {
          field: 'dealPortion',
          title: '成交占比(%)',
          minWidth: 200,
        },
      ];
    }
    case 'source': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'source',
          title: '客户来源',
          minWidth: 100,
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.CRM_CUSTOMER_SOURCE },
          },
        },
        {
          field: 'customerCount',
          title: '客户个数',
          minWidth: 200,
        },
        {
          field: 'dealCount',
          title: '成交个数',
          minWidth: 200,
        },
        {
          field: 'industryPortion',
          title: '行业占比(%)',
          minWidth: 200,
        },
        {
          field: 'dealPortion',
          title: '成交占比(%)',
          minWidth: 200,
        },
      ];
    }
    default: {
      return [];
    }
  }
}
