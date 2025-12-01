import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 关联数据 */
let userList: SystemUserApi.User[] = [];
getSimpleUserList().then((data) => (userList = data));

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
        placeholder: '请选择用户',
        allowClear: true,
      },
    },
    {
      fieldName: 'type',
      label: '写作类型',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择写作类型',
        options: getDictOptions(DICT_TYPE.AI_WRITE_TYPE, 'number'),
      },
    },
    {
      fieldName: 'platform',
      label: '平台',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择平台',
        options: getDictOptions(DICT_TYPE.AI_PLATFORM, 'string'),
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
      minWidth: 180,
      title: '用户',
      field: 'userId',
      formatter: ({ cellValue }) => {
        return userList.find((user) => user.id === cellValue)?.nickname || '-';
      },
    },
    {
      field: 'type',
      title: '写作类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_WRITE_TYPE },
      },
    },
    {
      field: 'platform',
      title: '平台',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_WRITE_TYPE },
      },
    },
    {
      field: 'model',
      title: '模型',
      minWidth: 180,
    },
    {
      field: 'prompt',
      title: '生成内容提示',
      minWidth: 180,
    },
    {
      field: 'generatedContent',
      title: '生成的内容',
      minWidth: 180,
    },
    {
      field: 'originalContent',
      title: '原文',
      minWidth: 180,
    },
    {
      field: 'length',
      title: '长度',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_WRITE_LENGTH },
      },
    },
    {
      field: 'format',
      title: '格式',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_WRITE_FORMAT },
      },
    },
    {
      field: 'tone',
      title: '语气',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_WRITE_TONE },
      },
    },
    {
      field: 'language',
      title: '语言',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_WRITE_LANGUAGE },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'errorMessage',
      title: '错误信息',
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
