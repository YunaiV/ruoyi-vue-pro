import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getProductCategorySimpleList } from '#/api/erp/product/category';
import { getProductUnitSimpleList } from '#/api/erp/product/unit';

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
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'barCode',
      label: '条码',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入条码',
      },
    },
    {
      fieldName: 'categoryId',
      label: '分类',
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => {
          const data = await getProductCategorySimpleList();
          return handleTree(data);
        },

        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择分类',
        treeDefaultExpandAll: true,
      },
      rules: 'required',
    },
    {
      fieldName: 'unitId',
      label: '单位',
      component: 'ApiSelect',
      componentProps: {
        api: getProductUnitSimpleList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择单位',
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
    {
      fieldName: 'standard',
      label: '规格',
      component: 'Input',
      componentProps: {
        placeholder: '请输入规格',
      },
    },
    {
      fieldName: 'expiryDay',
      label: '保质期天数',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入保质期天数',
      },
    },
    {
      fieldName: 'weight',
      label: '重量（kg）',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入重量（kg）',
      },
    },
    {
      fieldName: 'purchasePrice',
      label: '采购价格',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入采购价格，单位：元',
        precision: 2,
        min: 0,
        step: 0.01,
      },
    },
    {
      fieldName: 'salePrice',
      label: '销售价格',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入销售价格，单位：元',
        precision: 2,
        min: 0,
        step: 0.01,
      },
    },
    {
      fieldName: 'minPrice',
      label: '最低价格',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入最低价格，单位：元',
        precision: 2,
        min: 0,
        step: 0.01,
      },
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
      fieldName: 'categoryId',
      label: '分类',
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => {
          const data = await getProductCategorySimpleList();
          return handleTree(data);
        },

        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择分类',
        treeDefaultExpandAll: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'barCode',
      title: '条码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名称',
      minWidth: 200,
    },
    {
      field: 'standard',
      title: '规格',
      minWidth: 100,
    },
    {
      field: 'categoryName',
      title: '分类',
      minWidth: 120,
    },
    {
      field: 'unitName',
      title: '单位',
      minWidth: 100,
    },
    {
      field: 'purchasePrice',
      title: '采购价格',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'salePrice',
      title: '销售价格',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'minPrice',
      title: '最低价格',
      minWidth: 100,
      formatter: 'formatAmount2',
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
