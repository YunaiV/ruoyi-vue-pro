import type { VxeTableGridOptions } from '#/adapter/vxe-table';

/** 商机关联列表列定义 */
export function useBusinessDetailListColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      width: 50,
      fixed: 'left',
    },
    {
      field: 'name',
      title: '商机名称',
      fixed: 'left',
      slots: { default: 'name' },
    },
    {
      field: 'customerName',
      title: '客户名称',
      fixed: 'left',
      slots: { default: 'customerName' },
    },
    {
      field: 'totalPrice',
      title: '商机金额（元）',
      formatter: 'formatAmount2',
    },
    {
      field: 'dealTime',
      title: '预计成交日期',
      formatter: 'formatDate',
    },
    {
      field: 'ownerUserName',
      title: '负责人',
    },
    {
      field: 'ownerUserDeptName',
      title: '所属部门',
    },
    {
      field: 'statusTypeName',
      title: '商机状态组',
      fixed: 'right',
    },
    {
      field: 'statusName',
      title: '商机阶段',
      fixed: 'right',
    },
  ];
}
