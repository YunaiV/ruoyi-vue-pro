import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

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
      fieldName: 'prompt',
      label: '提示词',
      component: 'Input',
      componentProps: {
        placeholder: '请输入提示词',
        clearable: true,
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
      field: 'userId',
      title: '用户',
      minWidth: 180,
      formatter: ({ cellValue }) =>
        userList.find((user) => user.id === cellValue)?.nickname || '-',
    },
    {
      field: 'prompt',
      title: '提示词',
      minWidth: 180,
    },
    {
      field: 'generatedContent',
      title: '思维导图',
      minWidth: 300,
    },
    {
      field: 'model',
      title: '模型',
      minWidth: 180,
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
