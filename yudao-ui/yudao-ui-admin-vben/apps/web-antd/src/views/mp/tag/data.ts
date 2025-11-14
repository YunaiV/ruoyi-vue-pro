import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';

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
      fieldName: 'accountId',
      label: '公众号',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '标签名称',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridPropTypes.Columns {
  return [
    {
      title: '编号',
      field: 'id',
    },
    {
      title: '标签名称',
      field: 'name',
    },
    {
      title: '粉丝数',
      field: 'count',
    },
    {
      title: '创建时间',
      field: 'createTime',
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
