import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';

import { z } from '#/adapter/form';
import { getSimpleProductCategoryList } from '#/api/iot/product/category';

/** 新增/修改产品分类的表单 */
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
      label: '分类名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名称',
      },
      rules: z
        .string()
        .min(1, '分类名称不能为空')
        .max(64, '分类名称长度不能超过 64 个字符'),
    },
    {
      fieldName: 'parentId',
      label: '父级分类',
      component: 'ApiTreeSelect',
      componentProps: {
        api: getSimpleProductCategoryList,
        fieldNames: {
          label: 'name',
          value: 'id',
        },
        placeholder: '请选择父级分类',
        allowClear: true,
      },
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入排序',
        class: 'w-full',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      defaultValue: 1,
      componentProps: {
        options: [
          { label: '开启', value: 1 },
          { label: '关闭', value: 0 },
        ],
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入分类描述',
        rows: 3,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '分类名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        placeholder: ['开始日期', '结束日期'],
        allowClear: true,
        class: 'w-full',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'seq',
      title: 'ID',
      width: 80,
    },
    {
      field: 'name',
      title: '名字',
      minWidth: 200,
      treeNode: true,
    },
    {
      field: 'sort',
      title: '排序',
      width: 100,
    },
    {
      field: 'status',
      title: '状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'description',
      title: '描述',
      minWidth: 200,
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
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
