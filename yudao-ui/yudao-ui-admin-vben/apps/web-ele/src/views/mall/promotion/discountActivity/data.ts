import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDate } from '@vben/utils';

/** 表单配置 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '活动名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'startTime',
      label: '开始时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择开始时间',
        showTime: false,
        valueFormat: 'x',
        format: 'YYYY-MM-DD',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '结束时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择结束时间',
        showTime: false,
        valueFormat: 'x',
        format: 'YYYY-MM-DD',
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 4,
      },
    },
    // TODO
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '活动名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动名称',
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        clearable: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
    {
      fieldName: 'activeTime',
      label: '活动时间',
      component: 'RangePicker',
      componentProps: {
        placeholder: ['开始时间', '结束时间'],
        clearable: true,
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
      title: '活动编号',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '活动名称',
      minWidth: 140,
    },
    {
      field: 'activityTime',
      title: '活动时间',
      minWidth: 210,
      formatter: ({ row }) => {
        if (!row.startTime || !row.endTime) return '';
        return `${formatDate(row.startTime, 'YYYY-MM-DD')} ~ ${formatDate(row.endTime, 'YYYY-MM-DD')}`;
      },
    },
    {
      field: 'status',
      title: '活动状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 200,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
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
