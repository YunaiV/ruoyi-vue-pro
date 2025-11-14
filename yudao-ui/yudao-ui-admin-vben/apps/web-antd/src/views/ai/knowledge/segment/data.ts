import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      component: 'Input',
      fieldName: 'documentId',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'content',
      label: '切片内容',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入切片内容',
        rows: 6,
        showCount: true,
      },
      rules: 'required',
    },
  ];
}
/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'documentId',
      label: '文档编号',
      component: 'Input',
    },
    {
      fieldName: 'status',
      label: '是否启用',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '分段编号',
    },
    {
      type: 'expand',
      width: 40,
      slots: { content: 'expand_content' },
    },
    {
      field: 'content',
      title: '切片内容',
      minWidth: 250,
    },
    {
      field: 'contentLength',
      title: '字符数',
    },
    {
      field: 'tokens',
      title: 'token 数量',
    },
    {
      field: 'retrievalCount',
      title: '召回次数',
    },
    {
      field: 'status',
      title: '是否启用',
      slots: { default: 'status' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
