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
      fieldName: 'platform',
      label: '所属平台',
      component: 'Select',
      componentProps: {
        placeholder: '请选择所属平台',
        options: getDictOptions(DICT_TYPE.AI_PLATFORM, 'string'),
        allowClear: true,
      },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      component: 'Input',
      fieldName: 'apiKey',
      label: '密钥',
      rules: 'required',
      componentProps: {
        placeholder: '请输入密钥',
      },
    },
    {
      component: 'Input',
      fieldName: 'url',
      label: '自定义 API URL',
      componentProps: {
        placeholder: '请输入自定义 API URL',
      },
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
      label: '名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'platform',
      label: '平台',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择平台',
        options: getDictOptions(DICT_TYPE.AI_PLATFORM, 'string'),
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'platform',
      title: '所属平台',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_PLATFORM },
      },
      minWidth: 100,
    },
    {
      field: 'name',
      title: '名称',
      minWidth: 120,
    },
    {
      field: 'apiKey',
      title: '密钥',
      minWidth: 140,
    },
    {
      field: 'url',
      title: '自定义 API URL',
      minWidth: 180,
    },
    {
      field: 'status',
      title: '状态',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
      minWidth: 80,
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
