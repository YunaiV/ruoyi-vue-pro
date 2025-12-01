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
      label: '套餐名称',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入套餐名称',
      },
    },
    {
      fieldName: 'payPrice',
      label: '支付金额(元)',
      component: 'InputNumber',
      rules: z.number().min(0, '支付金额不能小于0'),
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.01,
        placeholder: '请输入支付金额',
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'bonusPrice',
      label: '赠送金额(元)',
      component: 'InputNumber',
      rules: z.number().min(0, '赠送金额不能小于0'),
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.01,
        placeholder: '请输入赠送金额',
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
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
      label: '套餐名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入套餐名称',
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        clearable: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '套餐编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '套餐名称',
      minWidth: 150,
    },
    {
      field: 'payPrice',
      title: '支付金额',
      minWidth: 120,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'bonusPrice',
      title: '赠送金额',
      minWidth: 120,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
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
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
