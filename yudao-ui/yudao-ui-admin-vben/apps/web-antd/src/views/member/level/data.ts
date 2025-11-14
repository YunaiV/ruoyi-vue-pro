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
      label: '等级名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入等级名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'level',
      label: '等级',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 0,
        placeholder: '请输入等级',
      },
      rules: 'required',
    },
    {
      fieldName: 'experience',
      label: '升级经验',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 0,
        placeholder: '请输入升级经验',
      },
      rules: 'required',
    },
    {
      fieldName: 'discountPercent',
      label: '享受折扣(%)',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        max: 100,
        precision: 0,
        placeholder: '请输入享受折扣',
      },
      rules: 'required',
    },
    {
      fieldName: 'icon',
      label: '等级图标',
      component: 'ImageUpload',
    },
    {
      fieldName: 'backgroundUrl',
      label: '等级背景图',
      component: 'ImageUpload',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '等级名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入等级名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
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
      title: '等级编号',
      minWidth: 80,
    },
    {
      field: 'icon',
      title: '等级图标',
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'backgroundUrl',
      title: '等级背景图',
      minWidth: 120,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'name',
      title: '等级名称',
      minWidth: 120,
    },
    {
      field: 'level',
      title: '等级',
      minWidth: 80,
    },
    {
      field: 'experience',
      title: '升级经验',
      minWidth: 100,
    },
    {
      field: 'discountPercent',
      title: '享受折扣(%)',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
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
