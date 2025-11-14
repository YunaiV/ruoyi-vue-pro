import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getProductSimpleList } from '#/api/erp/product/product';
import { getWarehouseSimpleList } from '#/api/erp/stock/warehouse';
import { getRangePickerDefaultProps } from '#/utils';

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
    {
      fieldName: 'bizType',
      label: '类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择类型',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.ERP_STOCK_RECORD_BIZ_TYPE, 'number'),
      },
    },
    {
      fieldName: 'bizNo',
      label: '业务单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入业务单号',
        allowClear: true,
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
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'productName',
      title: '产品名称',
      minWidth: 150,
    },
    {
      field: 'categoryName',
      title: '产品分类',
      width: 120,
    },
    {
      field: 'unitName',
      title: '产品单位',
      width: 100,
    },
    {
      field: 'warehouseName',
      title: '仓库',
      width: 120,
    },
    {
      field: 'bizType',
      title: '类型',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.ERP_STOCK_RECORD_BIZ_TYPE },
      },
    },
    {
      field: 'bizNo',
      title: '出入库单号',
      width: 200,
      showOverflow: 'tooltip',
    },
    {
      field: 'createTime',
      title: '出入库日期',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'count',
      title: '出入库数量',
      width: 120,
      formatter: 'formatAmount3',
    },
    {
      field: 'totalCount',
      title: '库存量',
      width: 100,
      formatter: 'formatAmount3',
    },
    {
      field: 'creatorName',
      title: '操作人',
      width: 100,
    },
  ];
}
