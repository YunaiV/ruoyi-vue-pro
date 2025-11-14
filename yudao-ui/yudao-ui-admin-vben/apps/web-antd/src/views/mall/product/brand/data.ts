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
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '品牌名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入品牌名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'picUrl',
      label: '品牌图片',
      component: 'ImageUpload',
      componentProps: {
        placeholder: '请上传品牌图片',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '品牌排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入品牌排序',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '品牌状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'description',
      label: '品牌描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入品牌描述',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '品牌名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入品牌名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '品牌状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择品牌状态',
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

/** 表格列配置 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'name',
      title: '品牌名称',
      minWidth: 180,
    },
    {
      field: 'picUrl',
      title: '品牌图片',
      minWidth: 120,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'sort',
      title: '品牌排序',
      minWidth: 100,
    },
    {
      field: 'status',
      title: '品牌状态',
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
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
