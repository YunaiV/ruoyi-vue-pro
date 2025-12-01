import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

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
      label: '数据源名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入数据源名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'url',
      label: '数据源连接',
      component: 'Input',
      componentProps: {
        placeholder: '请输入数据源连接',
      },
      rules: 'required',
    },
    {
      fieldName: 'username',
      label: '用户名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名',
      },
      rules: 'required',
    },
    {
      fieldName: 'password',
      label: '密码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入密码',
        type: 'password',
      },
      rules: 'required',
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '主键编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '数据源名称',
      minWidth: 150,
    },
    {
      field: 'url',
      title: '数据源连接',
      minWidth: 300,
    },
    {
      field: 'username',
      title: '用户名',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
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
