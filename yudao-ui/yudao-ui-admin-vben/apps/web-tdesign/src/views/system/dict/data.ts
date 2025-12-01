import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleDictTypeList } from '#/api/system/dict/type';

// ============================== 字典类型 ==============================

/** 类型新增/修改的表单 */
export function useTypeFormSchema(): VbenFormSchema[] {
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
      label: '字典名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入字典名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '字典类型',
      component: 'Input',
      componentProps: (values) => {
        return {
          placeholder: '请输入字典类型',
          disabled: !!values.id,
        };
      },
      rules: 'required',
      dependencies: {
        triggerFields: [''],
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
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 类型列表的搜索表单 */
export function useTypeGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '字典名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入字典名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'type',
      label: '字典类型',
      component: 'Input',
      componentProps: {
        placeholder: '请输入字典类型',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        allowClear: true,
      },
    },
  ];
}

/** 类型列表的字段 */
export function useTypeGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '字典编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '字典名称',
      minWidth: 200,
    },
    {
      field: 'type',
      title: '字典类型',
      minWidth: 220,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      minWidth: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ============================== 字典数据 ==============================

// TODO @芋艿：后续针对 antd，增加
/**
 * 颜色选项
 */
const colorOptions = [
  { value: '', label: '无' },
  { value: 'processing', label: '主要' },
  { value: 'success', label: '成功' },
  { value: 'default', label: '默认' },
  { value: 'warning', label: '警告' },
  { value: 'error', label: '危险' },
  { value: 'pink', label: 'pink' },
  { value: 'red', label: 'red' },
  { value: 'orange', label: 'orange' },
  { value: 'green', label: 'green' },
  { value: 'cyan', label: 'cyan' },
  { value: 'blue', label: 'blue' },
  { value: 'purple', label: 'purple' },
];

/** 数据新增/修改的表单 */
export function useDataFormSchema(): VbenFormSchema[] {
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
      fieldName: 'dictType',
      label: '字典类型',
      component: 'ApiSelect',
      componentProps: (values) => {
        return {
          api: getSimpleDictTypeList,
          placeholder: '请输入字典类型',
          labelField: 'name',
          valueField: 'type',
          disabled: !!values.id,
        };
      },
      rules: 'required',
      dependencies: {
        triggerFields: [''],
      },
    },
    {
      fieldName: 'label',
      label: '数据标签',
      component: 'Input',
      componentProps: {
        placeholder: '请输入数据标签',
      },
      rules: 'required',
    },
    {
      fieldName: 'value',
      label: '数据键值',
      component: 'Input',
      componentProps: {
        placeholder: '请输入数据键值',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '显示排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入显示排序',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'colorType',
      label: '颜色类型',
      component: 'Select',
      componentProps: {
        options: colorOptions,
        placeholder: '请选择颜色类型',
      },
    },
    {
      fieldName: 'cssClass',
      label: 'CSS Class',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 CSS Class',
      },
      help: '输入 hex 模式的颜色, 例如 #108ee9',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 字典数据列表搜索表单 */
export function useDataGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'label',
      label: '字典标签',
      component: 'Input',
      componentProps: {
        placeholder: '请输入字典标签',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        allowClear: true,
      },
    },
  ];
}

/** 字典数据表格列 */
export function useDataGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '字典编码',
      minWidth: 100,
    },
    {
      field: 'label',
      title: '字典标签',
      minWidth: 180,
    },
    {
      field: 'value',
      title: '字典键值',
      minWidth: 100,
    },
    {
      field: 'sort',
      title: '字典排序',
      minWidth: 100,
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
      field: 'colorType',
      title: '颜色类型',
      minWidth: 120,
      slots: { default: 'colorType' },
    },
    {
      field: 'cssClass',
      title: 'CSS Class',
      minWidth: 120,
      slots: { default: 'cssClass' },
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      minWidth: 120,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
