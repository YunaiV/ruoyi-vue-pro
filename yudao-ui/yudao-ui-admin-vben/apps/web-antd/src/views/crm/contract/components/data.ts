import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { erpPriceInputFormatter } from '@vben/utils';

export function useDetailListColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: '合同编号',
      field: 'no',
      minWidth: 150,
      fixed: 'left',
    },
    {
      title: '合同名称',
      field: 'name',
      minWidth: 150,
      fixed: 'left',
      slots: { default: 'name' },
    },
    {
      title: '合同金额（元）',
      field: 'totalPrice',
      minWidth: 150,
      formatter: 'formatAmount2',
    },
    {
      title: '合同开始时间',
      field: 'startTime',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      title: '合同结束时间',
      field: 'endTime',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      title: '已回款金额（元）',
      field: 'totalReceivablePrice',
      minWidth: 150,
      formatter: 'formatAmount2',
    },
    {
      title: '未回款金额（元）',
      field: 'unpaidPrice',
      minWidth: 150,
      formatter: ({ row }) => {
        return erpPriceInputFormatter(
          row.totalPrice - row.totalReceivablePrice,
        );
      },
    },
    {
      title: '负责人',
      field: 'ownerUserName',
      minWidth: 150,
    },
    {
      title: '所属部门',
      field: 'ownerUserDeptName',
      minWidth: 150,
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      title: '创建人',
      field: 'creatorName',
      minWidth: 150,
    },
    {
      title: '备注',
      field: 'remark',
      minWidth: 150,
    },
    {
      title: '合同状态',
      field: 'auditStatus',
      fixed: 'right',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_AUDIT_STATUS },
      },
    },
  ];
}
