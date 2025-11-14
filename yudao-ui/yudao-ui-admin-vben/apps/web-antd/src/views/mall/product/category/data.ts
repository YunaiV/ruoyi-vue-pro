import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallCategoryApi } from '#/api/mall/product/category';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getCategoryList } from '#/api/mall/product/category';

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
      fieldName: 'parentId',
      label: '上级分类',
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: async () => {
          const data = await getCategoryList({ parentId: 0 });
          data.unshift({
            id: 0,
            name: '顶级分类',
          } as MallCategoryApi.Category);
          return handleTree(data);
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择上级分类',
        treeDefaultExpandAll: true,
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'name',
      label: '分类名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'picUrl',
      label: '移动端分类图',
      component: 'ImageUpload',
      componentProps: {
        placeholder: '请上传移动端分类图',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '分类排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入分类排序',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '开启状态',
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
      label: '分类名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名称',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MallCategoryApi.Category>['columns'] {
  return [
    {
      field: 'name',
      title: '分类名称',
      minWidth: 200,
      align: 'left',
      fixed: 'left',
      treeNode: true,
    },
    {
      field: 'picUrl',
      title: '移动端分类图',
      minWidth: 120,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'sort',
      title: '分类排序',
      minWidth: 100,
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
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 240,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
