import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { getPropertySimpleList } from '#/api/mall/product/property';
import { getRangePickerDefaultProps } from '#/utils';

// ============================== 属性 ==============================

/** 属性新增/修改的表单 */
export function usePropertyFormSchema(): VbenFormSchema[] {
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
      label: '属性名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入属性名称',
      },
      rules: 'required',
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

/** 属性列表的搜索表单 */
export function usePropertyGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '属性名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入属性名称',
        clearable: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 属性列表的字段 */
export function usePropertyGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '属性编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '属性名称',
      minWidth: 200,
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

// ============================== 属性值 ==============================

/** 属性值新增/修改的表单 */
export function useValueFormSchema(): VbenFormSchema[] {
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
      fieldName: 'propertyId',
      label: '属性',
      component: 'ApiSelect',
      componentProps: (values) => {
        return {
          api: getPropertySimpleList,
          placeholder: '请选择属性',
          labelField: 'name',
          valueField: 'id',
          disabled: !!values.id,
        };
      },
      rules: 'required',
      dependencies: {
        triggerFields: [''],
      },
    },
    {
      fieldName: 'name',
      label: '属性值名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入属性值名称',
      },
      rules: 'required',
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

/** 属性值列表搜索表单 */
export function useValueGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'propertyId',
      label: '属性项',
      component: 'ApiSelect',
      componentProps: {
        api: getPropertySimpleList,
        placeholder: '请选择属性项',
        labelField: 'name',
        valueField: 'id',
        disabled: true,
        clearable: false,
      },
    },
    {
      fieldName: 'name',
      label: '属性值名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入属性值名称',
        clearable: true,
      },
    },
  ];
}

/** 属性值表格列 */
export function useValueGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '属性值编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '属性值名称',
      minWidth: 180,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 180,
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
