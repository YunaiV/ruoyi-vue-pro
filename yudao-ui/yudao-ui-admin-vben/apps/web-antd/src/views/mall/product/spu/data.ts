import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSpuApi } from '#/api/mall/product/spu';

import { handleTree } from '@vben/utils';

import { getCategoryList } from '#/api/mall/product/category';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '商品名称',
      component: 'Input',
    },
    {
      fieldName: 'categoryId',
      label: '商品分类',
      component: 'ApiTreeSelect',
      componentProps: {
        api: async () => {
          const res = await getCategoryList({});
          return handleTree(res, 'id', 'parentId', 'children');
        },
        fieldNames: { label: 'name', value: 'id', children: 'children' },
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  onStatusChange?: (
    newStatus: number,
    row: MallSpuApi.Spu,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'expand',
      width: 80,
      slots: { content: 'expand_content' },
      fixed: 'left',
    },
    {
      field: 'id',
      title: '商品编号',
      fixed: 'left',
    },
    {
      field: 'name',
      title: '商品名称',
      fixed: 'left',
      minWidth: 200,
    },
    {
      field: 'picUrl',
      title: '商品图片',
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'price',
      title: '价格',
      formatter: 'formatAmount2',
    },
    {
      field: 'salesCount',
      title: '销量',
    },
    {
      field: 'stock',
      title: '库存',
    },
    {
      field: 'sort',
      title: '排序',
    },
    {
      field: 'status',
      title: '销售状态',
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: 'CellSwitch',
        props: {
          checkedValue: 1,
          checkedChildren: '上架',
          unCheckedValue: 0,
          unCheckedChildren: '下架',
        },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 300,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
