import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDate } from '@vben/utils';

/** 新增/修改的表单 */
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
      fieldName: 'startTime',
      label: '开始时间',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择开始时间',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '结束时间',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择结束时间',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'bargainFirstPrice',
      label: '砍价起始价格(元)',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.01,
        placeholder: '请输入砍价起始价格',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'bargainMinPrice',
      label: '砍价底价(元)',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.01,
        placeholder: '请输入砍价底价',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'stock',
      label: '活动库存',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        placeholder: '请输入活动库存',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'helpMaxCount',
      label: '助力人数',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        placeholder: '请输入助力人数',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'bargainCount',
      label: '砍价次数',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        placeholder: '请输入砍价次数',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'totalLimitCount',
      label: '购买限制',
      component: 'InputNumber',
      componentProps: {
        min: 1,
        placeholder: '请输入购买限制',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'randomMinPrice',
      label: '最小砍价金额(元)',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.01,
        placeholder: '请输入最小砍价金额',
        controlsPosition: 'right',
        class: '!w-full',
      },
    },
    {
      fieldName: 'randomMaxPrice',
      label: '最大砍价金额(元)',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.01,
        placeholder: '请输入最大砍价金额',
        controlsPosition: 'right',
        class: '!w-full',
      },
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
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        clearable: true,
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
      field: 'bargainFirstPrice',
      title: '起始价格',
      minWidth: 100,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'bargainMinPrice',
      title: '砍价底价',
      minWidth: 100,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'recordUserCount',
      title: '总砍价人数',
      minWidth: 100,
    },
    {
      field: 'recordSuccessUserCount',
      title: '成功砍价人数',
      minWidth: 110,
    },
    {
      field: 'helpUserCount',
      title: '助力人数',
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
      field: 'stock',
      title: '库存',
      minWidth: 80,
    },
    {
      field: 'totalStock',
      title: '总库存',
      minWidth: 80,
    },
    {
      field: 'createTime',
      title: '创建时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
