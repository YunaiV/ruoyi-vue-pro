import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { useUserStore } from '@vben/stores';
import {
  beginOfDay,
  endOfDay,
  erpCalculatePercentage,
  formatDateTime,
  handleTree,
} from '@vben/utils';

import { getSimpleDeptList } from '#/api/system/dept';
import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

const userStore = useUserStore();

export const customerSummaryTabs = [
  {
    tab: '客户总量分析',
    key: 'customerSummary',
  },
  {
    tab: '客户跟进次数分析',
    key: 'followUpSummary',
  },
  {
    tab: '客户跟进方式分析',
    key: 'followUpType',
  },
  {
    tab: '客户转化率分析',
    key: 'conversionStat',
  },
  {
    tab: '公海客户分析',
    key: 'poolSummary',
  },
  {
    tab: '员工客户成交周期分析',
    key: 'dealCycleByUser',
  },
  {
    tab: '地区客户成交周期分析',
    key: 'dealCycleByArea',
  },
  {
    tab: '产品客户成交周期分析',
    key: 'dealCycleByProduct',
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
      fieldName: 'interval',
      label: '时间间隔',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择时间间隔',
        options: getDictOptions(DICT_TYPE.DATE_INTERVAL, 'number'),
      },
      defaultValue: 2,
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
    case 'conversionStat': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'customerName',
          title: '客户名称',
          minWidth: 100,
        },
        {
          field: 'contractName',
          title: '合同名称',
          minWidth: 200,
        },
        {
          field: 'totalPrice',
          title: '合同总金额',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
        {
          field: 'receivablePrice',
          title: '回款金额',
          minWidth: 200,
          formatter: 'formatAmount2',
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
          field: 'industryId',
          title: '客户行业',
          minWidth: 100,
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.CRM_CUSTOMER_INDUSTRY },
          },
        },
        {
          field: 'ownerUserName',
          title: '负责人',
          minWidth: 200,
        },
        {
          field: 'creatorUserName',
          title: '创建人',
          minWidth: 200,
        },
        {
          field: 'createTime',
          title: '创建时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'orderDate',
          title: '下单日期',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
      ];
    }
    case 'customerSummary': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'ownerUserName',
          title: '员工姓名',
          minWidth: 100,
        },
        {
          field: 'customerCreateCount',
          title: '新增客户数',
          minWidth: 200,
        },
        {
          field: 'customerDealCount',
          title: '成交客户数',
          minWidth: 200,
        },
        {
          field: 'customerDealRate',
          title: '客户成交率(%)',
          minWidth: 200,
          formatter: ({ row }) => {
            return erpCalculatePercentage(
              row.customerDealCount,
              row.customerCreateCount,
            );
          },
        },
        {
          field: 'contractPrice',
          title: '合同总金额',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
        {
          field: 'receivablePrice',
          title: '回款金额',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
        {
          field: 'creceivablePrice',
          title: '未回款金额',
          minWidth: 200,
          formatter: ({ row }) => {
            return erpCalculatePercentage(
              row.receivablePrice,
              row.contractPrice,
            );
          },
        },
        {
          field: 'ccreceivablePrice',
          title: '回款完成率(%)',
          formatter: ({ row }) => {
            return erpCalculatePercentage(
              row.receivablePrice,
              row.contractPrice,
            );
          },
        },
      ];
    }
    case 'dealCycleByArea': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'areaName',
          title: '区域',
          minWidth: 200,
        },
        {
          field: 'customerDealCycle',
          title: '成交周期(天)',
          minWidth: 200,
        },
        {
          field: 'customerDealCount',
          title: '成交客户数',
          minWidth: 200,
        },
      ];
    }
    case 'dealCycleByProduct': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'productName',
          title: '产品名称',
          minWidth: 200,
        },
        {
          field: 'customerDealCycle',
          title: '成交周期(天)',
          minWidth: 200,
        },
        {
          field: 'customerDealCount',
          title: '成交客户数',
          minWidth: 200,
        },
      ];
    }
    case 'dealCycleByUser': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'ownerUserName',
          title: '日期',
          minWidth: 200,
        },
        {
          field: 'customerDealCycle',
          title: '成交周期(天)',
          minWidth: 200,
        },
        {
          field: 'customerDealCount',
          title: '成交客户数',
          minWidth: 200,
        },
      ];
    }
    case 'followUpSummary': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'ownerUserName',
          title: '员工姓名',
          minWidth: 200,
        },
        {
          field: 'followUpRecordCount',
          title: '跟进次数',
          minWidth: 200,
        },
        {
          field: 'followUpCustomerCount',
          title: '跟进客户数',
          minWidth: 200,
        },
      ];
    }
    case 'followUpType': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'followUpType',
          title: '跟进方式',
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.CRM_FOLLOW_UP_TYPE },
          },
        },
        {
          field: 'followUpRecordCount',
          title: '个数',
          minWidth: 200,
        },
        {
          field: 'portion',
          title: '占比(%)',
          minWidth: 200,
        },
      ];
    }
    case 'poolSummary': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'ownerUserName',
          title: '员工姓名',
          minWidth: 200,
        },
        {
          field: 'customerPutCount',
          title: '进入公海客户数',
          minWidth: 200,
        },
        {
          field: 'customerTakeCount',
          title: '公海领取客户数',
          minWidth: 200,
        },
      ];
    }
    default: {
      return [];
    }
  }
}
