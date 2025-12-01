import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MallSeckillConfigApi } from '#/api/mall/promotion/seckill/seckillConfig';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

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
      fieldName: 'name',
      label: '秒杀时段名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入秒杀时段名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'startTime',
      label: '开始时间点',
      component: 'TimePicker',
      componentProps: {
        format: 'HH:mm',
        valueFormat: 'HH:mm',
        placeholder: '请选择开始时间点',
      },
      rules: 'required',
    },
    {
      fieldName: 'endTime',
      label: '结束时间点',
      component: 'TimePicker',
      componentProps: {
        format: 'HH:mm',
        valueFormat: 'HH:mm',
        placeholder: '请选择结束时间点',
      },
      rules: 'required',
    },
    {
      fieldName: 'sliderPicUrls',
      label: '秒杀轮播图',
      component: 'ImageUpload',
      componentProps: {
        multiple: true,
        maxNumber: 5,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '秒杀时段名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入秒杀时段名称',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(
  onStatusChange?: (
    newStatus: number,
    row: MallSeckillConfigApi.SeckillConfig,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      title: '秒杀时段名称',
      field: 'name',
      minWidth: 200,
    },
    {
      title: '开始时间点',
      field: 'startTime',
      minWidth: 120,
    },
    {
      title: '结束时间点',
      field: 'endTime',
      minWidth: 120,
    },
    {
      title: '秒杀轮播图',
      field: 'sliderPicUrls',
      minWidth: 100,
      cellRender: {
        name: 'CellImages',
      },
    },
    {
      title: '活动状态',
      field: 'status',
      minWidth: 100,
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: 'CellSwitch',
        props: {
          checkedValue: 1,
          checkedChildren: '启用',
          unCheckedValue: 0,
          unCheckedChildren: '禁用',
        },
      },
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
