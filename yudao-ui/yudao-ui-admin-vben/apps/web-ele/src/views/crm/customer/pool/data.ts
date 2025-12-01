import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '客户名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'mobile',
      label: '手机',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机',
        allowClear: true,
      },
    },
    {
      fieldName: 'industryId',
      label: '所属行业',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_CUSTOMER_INDUSTRY, 'number'),
        placeholder: '请选择所属行业',
        allowClear: true,
      },
    },
    {
      fieldName: 'level',
      label: '客户级别',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_CUSTOMER_LEVEL, 'number'),
        placeholder: '请选择客户级别',
        allowClear: true,
      },
    },
    {
      fieldName: 'source',
      label: '客户来源',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_CUSTOMER_SOURCE, 'number'),
        placeholder: '请选择客户来源',
        allowClear: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: '客户名称',
      field: 'name',
      minWidth: 160,
      fixed: 'left',
      slots: { default: 'name' },
    },
    {
      title: '客户来源',
      field: 'source',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_CUSTOMER_SOURCE },
      },
    },
    {
      title: '手机',
      field: 'mobile',
      minWidth: 120,
    },
    {
      title: '电话',
      field: 'telephone',
      minWidth: 120,
    },
    {
      title: '邮箱',
      field: 'email',
      minWidth: 140,
    },
    {
      title: '客户级别',
      field: 'level',
      minWidth: 135,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_CUSTOMER_LEVEL },
      },
    },
    {
      title: '客户行业',
      field: 'industryId',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_CUSTOMER_INDUSTRY },
      },
    },
    {
      title: '下次联系时间',
      field: 'contactNextTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '备注',
      field: 'remark',
      minWidth: 200,
    },
    {
      title: '成交状态',
      field: 'dealStatus',
      minWidth: 80,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      title: '最后跟进时间',
      field: 'contactLastTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '最后跟进记录',
      field: 'contactLastContent',
      minWidth: 200,
    },
    {
      title: '更新时间',
      field: 'updateTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '创建人',
      field: 'creatorName',
      minWidth: 100,
    },
  ];
}
