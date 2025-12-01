import type { VxeTableGridOptions } from '#/adapter/vxe-table';

/** 详情列表的字段 */
export function useDetailListColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: '客户名称',
      field: 'customerName',
      minWidth: 150,
    },
    {
      title: '合同编号',
      field: 'contractNo',
      minWidth: 150,
    },
    {
      title: '期数',
      field: 'period',
      minWidth: 150,
    },
    {
      title: '计划回款(元)',
      field: 'price',
      minWidth: 150,
      formatter: 'formatAmount2',
    },
    {
      title: '计划回款日期',
      field: 'returnTime',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      title: '提前几天提醒',
      field: 'remindDays',
      minWidth: 150,
    },
    {
      title: '提醒日期',
      field: 'remindTime',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      title: '负责人',
      field: 'ownerUserName',
      minWidth: 150,
    },
    {
      title: '备注',
      field: 'remark',
      minWidth: 150,
    },
    {
      title: '操作',
      field: 'actions',
      width: 240,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
