import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleSeckillConfigList } from '#/api/mall/promotion/seckill/seckillConfig';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '活动名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        allowClear: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
}

/** 新增/编辑的表单 */
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
      label: '秒杀活动名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动名称',
      },
      rules: 'required',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'startTime',
      label: '活动开始时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择活动开始时间',
        showTime: false,
        format: 'YYYY-MM-DD',
        valueFormat: 'x',
        class: 'w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '活动结束时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择活动结束时间',
        showTime: false,
        format: 'YYYY-MM-DD',
        valueFormat: 'x',
        class: 'w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'configIds',
      label: '秒杀时段',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择秒杀时段',
        mode: 'multiple',
        api: getSimpleSeckillConfigList,
        labelField: 'name',
        valueField: 'id',
        class: 'w-full',
      },
      rules: 'required',
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'totalLimitCount',
      label: '总限购数量',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入总限购数量',
        min: 0,
        class: 'w-full',
      },
      rules: z.number().min(0).default(0),
    },
    {
      fieldName: 'singleLimitCount',
      label: '单次限购数量',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入单次限购数量',
        min: 0,
        class: 'w-full',
      },
      rules: z.number().min(0).default(0),
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入排序',
        min: 0,
        class: 'w-full',
      },
      rules: z.number().min(0).default(0),
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
        rows: 4,
      },
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '活动编号',
      minWidth: 80,
    },
    {
      field: 'name',
      title: '活动名称',
      minWidth: 140,
    },
    {
      field: 'configIds',
      title: '秒杀时段',
      minWidth: 220,
      slots: { default: 'configIds' },
    },
    {
      field: 'startTime',
      title: '活动时间',
      minWidth: 210,
      slots: { default: 'timeRange' },
    },
    {
      field: 'picUrl',
      title: '商品图片',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'spuName',
      title: '商品标题',
      minWidth: 300,
    },
    {
      field: 'marketPrice',
      title: '原价',
      minWidth: 100,
      formatter: ({ row }) => `￥${(row.marketPrice / 100).toFixed(2)}`,
    },
    {
      field: 'seckillPrice',
      title: '秒杀价',
      minWidth: 100,
      formatter: ({ row }) => {
        if (!(row.products || row.products.length === 0)) {
          return '￥0.00';
        }
        const seckillPrice = Math.min(
          ...row.products.map((item: any) => item.seckillPrice),
        );
        return `￥${(seckillPrice / 100).toFixed(2)}`;
      },
    },
    {
      field: 'status',
      title: '活动状态',
      align: 'center',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'stock',
      title: '库存',
      align: 'center',
      minWidth: 80,
    },
    {
      field: 'totalStock',
      title: '总库存',
      align: 'center',
      minWidth: 80,
    },
    {
      field: 'createTime',
      title: '创建时间',
      align: 'center',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      align: 'center',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
