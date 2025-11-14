import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户编号',
        allowClear: true,
      },
    },
    {
      fieldName: 'userType',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
        placeholder: '请选择用户类型',
        allowClear: true,
      },
    },
    {
      fieldName: 'clientId',
      label: '客户端编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户端编号',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'accessToken',
      title: '访问令牌',
      minWidth: 300,
    },
    {
      field: 'refreshToken',
      title: '刷新令牌',
      minWidth: 300,
    },
    {
      field: 'userId',
      title: '用户编号',
      minWidth: 100,
    },
    {
      field: 'userType',
      title: '用户类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.USER_TYPE },
      },
    },
    {
      field: 'clientId',
      title: '客户端编号',
      minWidth: 120,
    },
    {
      field: 'expiresTime',
      title: '过期时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
