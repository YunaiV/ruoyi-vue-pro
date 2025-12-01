import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { useUserStore } from '@vben/stores';
import { handleTree } from '@vben/utils';

import { z } from '#/adapter/form';
import { getProductCategoryList } from '#/api/crm/product/category';
import { getSimpleUserList } from '#/api/system/user';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  const userStore = useUserStore();
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
      label: '产品名称',
      rules: 'required',
      componentProps: {
        placeholder: '请输入产品名称',
        allowClear: true,
      },
    },
    {
      component: 'ApiSelect',
      fieldName: 'ownerUserId',
      label: '负责人',
      rules: 'required',
      dependencies: {
        triggerFields: ['id'],
        disabled: (values) => values.id,
      },
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '请选择负责人',
        allowClear: true,
      },
      defaultValue: userStore.userInfo?.id,
    },
    {
      component: 'Input',
      fieldName: 'no',
      label: '产品编码',
      rules: 'required',
      componentProps: {
        placeholder: '请输入产品编码',
        allowClear: true,
      },
    },
    {
      component: 'ApiTreeSelect',
      fieldName: 'categoryId',
      label: '产品类型',
      rules: 'required',
      componentProps: {
        api: async () => {
          const data = await getProductCategoryList();
          return handleTree(data);
        },
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: '请选择产品类型',
        allowClear: true,
      },
    },
    {
      fieldName: 'unit',
      label: '产品单位',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_PRODUCT_UNIT, 'number'),
        placeholder: '请选择产品单位',
        allowClear: true,
      },
      rules: 'required',
    },
    {
      component: 'InputNumber',
      fieldName: 'price',
      label: '价格（元）',
      rules: 'required',
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.1,
        placeholder: '请输入产品价格',
      },
    },
    {
      component: 'Textarea',
      fieldName: 'description',
      label: '产品描述',
      componentProps: {
        placeholder: '请输入产品描述',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '上架状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.CRM_PRODUCT_STATUS, 'number'),
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
      label: '产品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入产品名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '上架状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择上架状态',
        options: getDictOptions(DICT_TYPE.CRM_PRODUCT_STATUS, 'number'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '产品编号',
      visible: false,
    },
    {
      field: 'name',
      title: '产品名称',
      minWidth: 240,
      slots: { default: 'name' },
    },
    {
      field: 'categoryName',
      title: '产品类型',
      minWidth: 120,
    },
    {
      field: 'unit',
      title: '产品单位',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_PRODUCT_UNIT },
      },
    },
    {
      field: 'no',
      title: '产品编码',
      minWidth: 120,
    },
    {
      field: 'price',
      title: '价格（元）',
      formatter: 'formatAmount2',
      minWidth: 120,
    },
    {
      field: 'description',
      title: '产品描述',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '上架状态',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.CRM_PRODUCT_STATUS },
      },
      minWidth: 120,
    },
    {
      field: 'ownerUserName',
      title: '负责人',
      minWidth: 120,
    },
    {
      field: 'updateTime',
      title: '更新时间',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      field: 'creatorName',
      title: '创建人',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
