import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

/** 联系人明细列表列配置 */
export function useDetailListColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      width: 50,
      fixed: 'left',
    },
    {
      field: 'name',
      title: '姓名',
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
      field: 'sex',
      title: '性别',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_USER_SEX },
      },
    },
    {
      field: 'mobile',
      title: '手机',
    },
    {
      field: 'telephone',
      title: '电话',
    },
    {
      field: 'email',
      title: '邮箱',
    },
    {
      field: 'post',
      title: '职位',
    },
    {
      field: 'detailAddress',
      title: '地址',
    },
    {
      field: 'master',
      title: '关键决策人',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
  ];
}
