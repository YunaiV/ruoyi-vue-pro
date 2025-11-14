import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ErpProductCategoryApi } from '#/api/erp/product/category';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getProductCategoryList } from '#/api/erp/product/category';

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
          const data = await getProductCategoryList();
          data.unshift({
            id: 0,
            name: '顶级分类',
          });
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
      fieldName: 'code',
      label: '分类编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类编码',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '显示顺序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入显示顺序',
      },
      rules: 'required',
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

/** 查询表单 */
export function useQueryFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'name',
      label: '分类名称',
      componentProps: {
        placeholder: '请输入分类名称',
        allowClear: true,
      },
    },
    {
      component: 'Select',
      fieldName: 'status',
      label: '开启状态',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择开启状态',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<ErpProductCategoryApi.ProductCategory>['columns'] {
  return [
    {
      field: 'name',
      title: '分类名称',
      align: 'left',
      treeNode: true,
    },
    {
      field: 'code',
      title: '分类编码',
    },
    {
      field: 'sort',
      title: '显示顺序',
    },
    {
      field: 'status',
      title: '分类状态',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
