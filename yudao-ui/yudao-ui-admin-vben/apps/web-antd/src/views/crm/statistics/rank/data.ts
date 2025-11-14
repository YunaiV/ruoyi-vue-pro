import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { useUserStore } from '@vben/stores';
import { beginOfDay, endOfDay, formatDateTime, handleTree } from '@vben/utils';

import { getSimpleDeptList } from '#/api/system/dept';
import { getRangePickerDefaultProps } from '#/utils';

const userStore = useUserStore();

export const customerSummaryTabs = [
  {
    tab: '合同金额排行',
    key: 'contractPriceRank',
  },
  {
    tab: '回款金额排行',
    key: 'receivablePriceRank',
  },
  {
    tab: '签约合同排行',
    key: 'contractCountRank',
  },
  {
    tab: '产品销量排行',
    key: 'productSalesRank',
  },
  {
    tab: '新增客户数排行',
    key: 'customerCountRank',
  },
  {
    tab: '新增联系人数排行',
    key: 'contactCountRank',
  },
  {
    tab: '跟进次数排行',
    key: 'followCountRank',
  },
  {
    tab: '跟进客户数排行',
    key: 'followCustomerCountRank',
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
      ] as [Date, Date],
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
      },
      defaultValue: userStore.userInfo?.deptId,
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  activeTabName: any,
): VxeTableGridOptions['columns'] {
  switch (activeTabName) {
    case 'contactCountRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '创建人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '新增联系人数（个）',
          minWidth: 200,
        },
      ];
    }
    case 'contractCountRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '签约合同数（个）',
          minWidth: 200,
        },
      ];
    }
    case 'contractPriceRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '合同金额（元）',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
      ];
    }
    case 'customerCountRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '新增客户数（个）',
          minWidth: 200,
        },
      ];
    }
    case 'followCountRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '跟进次数（次）',
          minWidth: 200,
        },
      ];
    }
    case 'followCustomerCountRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '跟进客户数（个）',
          minWidth: 200,
        },
      ];
    }
    case 'productSalesRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '产品销量',
          minWidth: 200,
        },
      ];
    }
    case 'receivablePriceRank': {
      return [
        {
          type: 'seq',
          title: '公司排名',
        },
        {
          field: 'nickname',
          title: '签订人',
          minWidth: 200,
        },
        {
          field: 'deptName',
          title: '部门',
          minWidth: 200,
        },
        {
          field: 'count',
          title: '回款金额（元）',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
      ];
    }
    default: {
      return [];
    }
  }
}
