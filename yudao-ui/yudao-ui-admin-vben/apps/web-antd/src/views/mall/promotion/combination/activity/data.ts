import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDate } from '@vben/utils';

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
      label: '活动名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入活动名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'startTime',
      label: '开始时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择开始时间',
        showTime: false,
        valueFormat: 'x',
        format: 'YYYY-MM-DD',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '结束时间',
      component: 'DatePicker',
      componentProps: {
        placeholder: '请选择结束时间',
        showTime: false,
        valueFormat: 'x',
        format: 'YYYY-MM-DD',
      },
      rules: 'required',
    },
    {
      fieldName: 'userSize',
      label: '用户数量',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入用户数量',
        min: 2,
      },
      rules: 'required',
    },
    {
      fieldName: 'limitDuration',
      label: '限制时长',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入限制时长(小时)',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'totalLimitCount',
      label: '总限购数量',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入总限购数量',
        min: 0,
      },
    },
    {
      fieldName: 'singleLimitCount',
      label: '单次限购数量',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入单次限购数量',
        min: 0,
      },
    },
    {
      fieldName: 'virtualGroup',
      label: '虚拟成团',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
    },
    {
      // TODO
      fieldName: 'spuId',
      label: '拼团商品',
      component: 'Input',
    },
  ];
}

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
      field: 'activityTime',
      title: '活动时间',
      minWidth: 210,
      formatter: ({ row }) => {
        if (!row.startTime || !row.endTime) return '';
        return `${formatDate(row.startTime, 'YYYY-MM-DD')} ~ ${formatDate(row.endTime, 'YYYY-MM-DD')}`;
      },
    },
    {
      field: 'picUrl',
      title: '商品图片',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
        props: {
          height: 40,
          width: 40,
        },
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
      formatter: ({ cellValue }) => {
        return `¥${(cellValue / 100).toFixed(2)}`;
      },
    },
    {
      field: 'combinationPrice',
      title: '拼团价',
      minWidth: 100,
      formatter: ({ row }) => {
        if (!row.products || row.products.length === 0) return '';
        const combinationPrice = Math.min(
          ...row.products.map((item: any) => item.combinationPrice),
        );
        return `¥${(combinationPrice / 100).toFixed(2)}`;
      },
    },
    {
      field: 'groupCount',
      title: '开团组数',
      minWidth: 100,
    },
    {
      field: 'groupSuccessCount',
      title: '成团组数',
      minWidth: 100,
    },
    {
      field: 'recordCount',
      title: '购买次数',
      minWidth: 100,
    },
    {
      field: 'status',
      title: '活动状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
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
