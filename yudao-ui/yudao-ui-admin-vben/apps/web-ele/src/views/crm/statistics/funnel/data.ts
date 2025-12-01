import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { useUserStore } from '@vben/stores';
import { beginOfDay, endOfDay, formatDateTime, handleTree } from '@vben/utils';

import { getSimpleDeptList } from '#/api/system/dept';
import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

const userStore = useUserStore();

export const customerSummaryTabs = [
  {
    tab: '销售漏斗分析',
    key: 'funnel',
  },
  {
    tab: '新增商机分析',
    key: 'businessSummary',
  },
  {
    tab: '商机转化率分析',
    key: 'businessInversionRateSummary',
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
        allowClear: true,
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '请选择员工',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  activeTabName: any,
): VxeTableGridOptions['columns'] {
  switch (activeTabName) {
    case 'businessInversionRateSummary': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'name',
          title: '商机名称',
          minWidth: 100,
        },
        {
          field: 'customerName',
          title: '客户名称',
          minWidth: 200,
        },
        {
          field: 'totalPrice',
          title: '商机金额（元）',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
        {
          field: 'dealTime',
          title: '预计成交日期',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'ownerUserName',
          title: '负责人',
          minWidth: 200,
        },
        {
          field: 'ownerUserDeptName',
          title: '所属部门',
          minWidth: 200,
        },
        {
          field: 'contactLastTime',
          title: '最后跟进时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'updateTime',
          title: '更新时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'createTime',
          title: '创建时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'creatorName',
          title: '创建人',
          minWidth: 100,
        },
        {
          field: 'statusTypeName',
          title: '商机状态组',
          minWidth: 100,
        },
        {
          field: 'statusName',
          title: '商机阶段',
          minWidth: 100,
        },
      ];
    }
    case 'businessSummary': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'name',
          title: '商机名称',
          minWidth: 100,
        },
        {
          field: 'customerName',
          title: '客户名称',
          minWidth: 200,
        },
        {
          field: 'totalPrice',
          title: '商机金额（元）',
          minWidth: 200,
          formatter: 'formatAmount2',
        },
        {
          field: 'dealTime',
          title: '预计成交日期',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'ownerUserName',
          title: '负责人',
          minWidth: 200,
        },
        {
          field: 'ownerUserDeptName',
          title: '所属部门',
          minWidth: 200,
        },
        {
          field: 'contactLastTime',
          title: '最后跟进时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'updateTime',
          title: '更新时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'createTime',
          title: '创建时间',
          minWidth: 200,
          formatter: 'formatDateTime',
        },
        {
          field: 'creatorName',
          title: '创建人',
          minWidth: 100,
        },
        {
          field: 'statusTypeName',
          title: '商机状态组',
          minWidth: 100,
        },
        {
          field: 'statusName',
          title: '商机阶段',
          minWidth: 100,
        },
      ];
    }
    case 'funnel': {
      return [
        {
          type: 'seq',
          title: '序号',
        },
        {
          field: 'endStatus',
          title: '阶段',
          minWidth: 100,
          cellRender: {
            name: 'CellDict',
            props: { type: DICT_TYPE.CRM_BUSINESS_END_STATUS_TYPE },
          },
        },
        {
          field: 'businessCount',
          title: '商机数',
          minWidth: 200,
        },
        {
          field: 'totalPrice',
          title: '商机总金额(元)',
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
