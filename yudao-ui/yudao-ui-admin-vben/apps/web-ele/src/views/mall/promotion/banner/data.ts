import type { VxeTableGridOptions } from '@vben/plugins/vxe-table';

import type { VbenFormSchema } from '#/adapter/form';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getRangePickerDefaultProps } from '#/utils';

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
      fieldName: 'title',
      label: 'Banner 标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 Banner 标题',
      },
      rules: 'required',
    },
    {
      fieldName: 'picUrl',
      label: '图片地址',
      component: 'ImageUpload',
      componentProps: {
        placeholder: '请上传图片',
      },
      rules: 'required',
    },
    {
      fieldName: 'position',
      label: '定位',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.PROMOTION_BANNER_POSITION, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'url',
      label: '跳转地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入跳转地址',
      },
      rules: 'required',
    },
    {
      fieldName: 'sort',
      label: '排序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入排序',
        controlsPosition: 'right',
        class: '!w-full',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'memo',
      label: '描述',
      component: 'Textarea',
      componentProps: {
        rows: 4,
        placeholder: '请输入描述',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'title',
      label: 'Banner 标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 Banner 标题',
      },
    },
    {
      fieldName: 'status',
      label: '活动状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择活动状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: 'Banner标题',
      field: 'title',
      minWidth: 100,
    },
    {
      title: '图片',
      field: 'picUrl',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      title: '状态',
      field: 'status',
      minWidth: 150,
      cellRender: {
        name: 'CellDict',
        props: {
          type: DICT_TYPE.COMMON_STATUS,
        },
      },
    },
    {
      title: '定位',
      field: 'position',
      minWidth: 150,
      cellRender: {
        name: 'CellDict',
        props: {
          type: DICT_TYPE.PROMOTION_BANNER_POSITION,
        },
      },
    },
    {
      title: '跳转地址',
      field: 'url',
      minWidth: 200,
    },
    {
      title: '创建时间',
      field: 'createTime',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '排序',
      field: 'sort',
      minWidth: 100,
    },
    {
      title: '描述',
      field: 'memo',
      minWidth: 150,
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
