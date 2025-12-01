import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSpuApi } from '#/api/mall/product/spu';

import { fenToYuan, handleTree, treeToString } from '@vben/utils';

import { getCategoryList } from '#/api/mall/product/category';
import { getRangePickerDefaultProps } from '#/utils';

/** 关联数据 */
let categoryList: any[] = [];
getCategoryList({}).then((data) => {
  categoryList = handleTree(data, 'id', 'parentId', 'children');
});

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '商品名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入商品名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'categoryId',
      label: '商品分类',
      component: 'ApiTreeSelect',
      componentProps: {
        placeholder: '请选择商品分类',
        allowClear: true,
        options: categoryList,
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
      field: 'id',
      title: '商品编号',
      fixed: 'left',
      minWidth: 100,
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
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'categoryId',
      title: '商品分类',
      minWidth: 150,
      formatter: ({ row }) => {
        return treeToString(categoryList, row.categoryId);
      },
    },
    {
      field: 'status',
      title: '销售状态',
      minWidth: 100,
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
      field: 'price',
      title: '价格',
      minWidth: 100,
      formatter: 'formatAmount2',
    },
    {
      field: 'marketPrice',
      title: '市场价',
      minWidth: 100,
      formatter: ({ row }) => {
        return `${fenToYuan(row.marketPrice)} 元`;
      },
    },
    {
      field: 'costPrice',
      title: '成本价',
      minWidth: 100,
      formatter: ({ row }) => {
        return `${fenToYuan(row.costPrice)} 元`;
      },
    },
    {
      field: 'salesCount',
      title: '销量',
      minWidth: 80,
    },
    {
      field: 'virtualSalesCount',
      title: '虚拟销量',
      minWidth: 100,
    },
    {
      field: 'stock',
      title: '库存',
      minWidth: 80,
    },
    {
      field: 'browseCount',
      title: '浏览量',
      minWidth: 100,
    },
    {
      field: 'sort',
      title: '排序',
      minWidth: 80,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
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
