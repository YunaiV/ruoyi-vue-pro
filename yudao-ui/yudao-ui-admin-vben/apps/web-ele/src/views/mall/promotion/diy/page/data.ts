import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

/** 表单配置 */
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
      fieldName: 'name',
      label: '页面名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入页面名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 4,
      },
    },
    {
      fieldName: 'previewPicUrls',
      component: 'ImageUpload',
      label: '预览图',
      componentProps: {
        maxNumber: 10,
        multiple: true,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '页面名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入页面名称',
        clearable: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        placeholder: ['开始时间', '结束时间'],
        clearable: true,
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
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
      minWidth: 80,
    },
    {
      field: 'previewPicUrls',
      title: '预览图',
      minWidth: 120,
      cellRender: {
        name: 'CellImages',
      },
    },
    {
      field: 'name',
      title: '页面名称',
      minWidth: 150,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 200,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
