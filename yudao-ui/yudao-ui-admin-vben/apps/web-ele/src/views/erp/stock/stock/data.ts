import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { getProductSimpleList } from '#/api/erp/product/product';
import { getWarehouseSimpleList } from '#/api/erp/stock/warehouse';

/** 搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'productId',
      label: '产品',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择产品',
        allowClear: true,
        showSearch: true,
        api: getProductSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
    },
    {
      fieldName: 'warehouseId',
      label: '仓库',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择仓库',
        allowClear: true,
        showSearch: true,
        api: getWarehouseSimpleList,
        labelField: 'name',
        valueField: 'id',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'productName',
      title: '产品名称',
      minWidth: 150,
    },
    {
      field: 'unitName',
      title: '产品单位',
      minWidth: 100,
    },
    {
      field: 'categoryName',
      title: '产品分类',
      minWidth: 120,
    },
    {
      field: 'count',
      title: '库存量',
      minWidth: 100,
      formatter: 'formatAmount3',
    },
    {
      field: 'warehouseName',
      title: '仓库',
      minWidth: 120,
    },
  ];
}
