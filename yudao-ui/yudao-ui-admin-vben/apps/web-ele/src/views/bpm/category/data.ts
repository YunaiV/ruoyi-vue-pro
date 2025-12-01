import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改的表单 */
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
      label: '分类名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名',
      },
      rules: 'required',
    },
    {
      label: '分类标志',
      fieldName: 'code',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类标志',
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '分类描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入分类描述',
      },
    },
    {
      fieldName: 'status',
      label: '分类状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'sort',
      label: '分类排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入分类排序',
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
  ];
}

/** 重命名的表单 */
export function useRenameFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '分类名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名',
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '分类名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名',
        allowClear: true,
      },
    },
    {
      fieldName: 'code',
      label: '分类标志',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类标志',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '分类状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择分类状态',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '分类编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '分类名',
      minWidth: 200,
    },
    {
      field: 'code',
      title: '分类标志',
      minWidth: 200,
    },
    {
      field: 'description',
      title: '分类描述',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '分类状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'sort',
      title: '分类排序',
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
