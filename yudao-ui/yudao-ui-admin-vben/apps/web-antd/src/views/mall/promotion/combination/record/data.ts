import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'status',
      label: '拼团状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择拼团状态',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        placeholder: ['开始时间', '结束时间'],
        allowClear: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '拼团编号',
      minWidth: 80,
    },
    {
      field: 'avatar',
      title: '头像',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
          shape: 'circle',
        },
      },
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 100,
    },
    {
      field: 'headId',
      title: '开团团长',
      minWidth: 100,
    },
    {
      field: 'picUrl',
      title: '拼团商品图',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'spuName',
      title: '拼团商品',
      minWidth: 120,
    },
    {
      field: 'activityName',
      title: '拼团活动',
      minWidth: 140,
    },
    {
      field: 'userSize',
      title: '几人团',
      minWidth: 80,
    },
    {
      field: 'userCount',
      title: '参与人数',
      minWidth: 80,
    },
    {
      field: 'createTime',
      title: '参团时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '结束时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '拼团状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 用户列表表格列配置 */
export function useUserGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 80,
    },
    {
      field: 'avatar',
      title: '用户头像',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
          shape: 'circle',
        },
      },
    },
    {
      field: 'nickname',
      title: '用户昵称',
      minWidth: 100,
    },
    {
      field: 'headId',
      title: '开团团长',
      minWidth: 100,
      formatter: ({ cellValue }) => {
        return cellValue === 0 ? '团长' : '团员';
      },
    },
    {
      field: 'createTime',
      title: '参团时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '结束时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '拼团状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
  ];
}
