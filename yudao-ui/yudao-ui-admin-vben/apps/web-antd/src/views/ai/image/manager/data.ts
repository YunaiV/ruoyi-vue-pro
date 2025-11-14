import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
      },
    },
    {
      fieldName: 'platform',
      label: '平台',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.AI_PLATFORM, 'string'),
      },
    },
    {
      fieldName: 'status',
      label: '绘画状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.AI_IMAGE_STATUS, 'number'),
      },
    },
    {
      fieldName: 'publicStatus',
      label: '是否发布',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
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
      field: 'id',
      title: '编号',
      minWidth: 180,
      fixed: 'left',
    },
    {
      title: '图片',
      minWidth: 110,
      fixed: 'left',
      slots: { default: 'picUrl' },
    },
    {
      minWidth: 180,
      title: '用户',
      slots: { default: 'userId' },
    },
    {
      field: 'platform',
      title: '平台',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_PLATFORM },
      },
    },
    {
      field: 'model',
      title: '模型',
      minWidth: 180,
    },
    {
      field: 'status',
      title: '绘画状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_IMAGE_STATUS },
      },
    },
    {
      minWidth: 100,
      title: '是否发布',
      slots: { default: 'publicStatus' },
    },
    {
      field: 'prompt',
      title: '提示词',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'width',
      title: '宽度',
      minWidth: 180,
    },
    {
      field: 'height',
      title: '高度',
      minWidth: 180,
    },
    {
      field: 'errorMessage',
      title: '错误信息',
      minWidth: 180,
    },
    {
      field: 'taskId',
      title: '任务编号',
      minWidth: 180,
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
