import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';

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
      component: 'InputNumber',
      fieldName: 'day',
      label: '签到天数',
      help: '只允许设置 1-7，默认签到 7 天为一个周期',
      componentProps: {
        min: 1,
        max: 7,
        precision: 0,
        placeholder: '请输入签到天数',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: z.number().min(1).max(7, '签到天数必须在 1-7 之间'),
    },
    {
      component: 'InputNumber',
      fieldName: 'point',
      label: '获得积分',
      componentProps: {
        min: 0,
        precision: 0,
        placeholder: '请输入获得积分',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: z.number().min(0, '获得积分不能小于 0'),
    },
    {
      component: 'InputNumber',
      fieldName: 'experience',
      label: '奖励经验',
      componentProps: {
        min: 0,
        precision: 0,
        placeholder: '请输入奖励经验',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: z.number().min(0, '奖励经验不能小于 0'),
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'day',
      title: '签到天数',
      minWidth: 120,
      formatter: ({ cellValue }) => ['第', cellValue, '天'].join(' '),
    },
    {
      field: 'point',
      title: '获得积分',
      minWidth: 120,
    },
    {
      field: 'experience',
      title: '奖励经验',
      minWidth: 120,
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
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
