import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

/** 修改的表单 */
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
      fieldName: 'nickname',
      label: '昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入昵称',
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
      fieldName: 'accountId',
      label: '公众号',
      component: 'Input',
    },
    {
      fieldName: 'openid',
      label: '用户标识',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户标识',
        clearable: true,
      },
    },
    {
      fieldName: 'nickname',
      label: '昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入昵称',
        clearable: true,
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
      field: 'openid',
      title: '用户标识',
      minWidth: 260,
    },
    {
      field: 'headImageUrl',
      title: '用户头像',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 120,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'tagIds',
      title: '标签',
      minWidth: 200,
      cellRender: {
        name: 'CellTags',
      },
    },
    {
      field: 'subscribeStatus',
      title: '订阅状态',
      minWidth: 100,
      align: 'center',
      formatter: ({ cellValue }) => {
        return cellValue === 0 ? '已订阅' : '未订阅';
      },
    },
    {
      field: 'subscribeTime',
      title: '订阅时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
