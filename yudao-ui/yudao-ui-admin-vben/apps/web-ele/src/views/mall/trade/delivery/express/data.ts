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
      component: 'Input',
      fieldName: 'code',
      label: '公司编码',
      componentProps: {
        placeholder: '请输入快递编码',
      },
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'name',
      label: '公司名称',
      componentProps: {
        placeholder: '请输入快递名称',
      },
      rules: 'required',
    },
    {
      component: 'ImageUpload',
      fieldName: 'logo',
      label: '公司 logo',
      rules: 'required',
      help: '推荐 180x180 图片分辨率',
    },
    {
      fieldName: 'sort',
      label: '显示顺序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入显示顺序',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
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
      label: '快递公司名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入快递公司名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'code',
      label: '快递公司编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入快递公司编号',
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
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'code',
      title: '公司编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '公司名称',
      minWidth: 150,
    },
    {
      field: 'logo',
      title: '公司 logo',
      minWidth: 120,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'sort',
      title: '显示顺序',
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
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
