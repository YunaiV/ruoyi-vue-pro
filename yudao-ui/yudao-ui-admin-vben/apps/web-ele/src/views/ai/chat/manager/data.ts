import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { DICT_TYPE } from '@vben/constants';

import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 关联数据 */
let userList: SystemUserApi.User[] = [];
getSimpleUserList().then((data) => (userList = data));

/** 列表的搜索表单 */
export function useGridFormSchemaConversation(): VbenFormSchema[] {
  return [
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户编号',
        clearable: true,
      },
    },
    {
      fieldName: 'title',
      label: '聊天标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入聊天标题',
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
export function useGridColumnsConversation(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '对话编号',
      fixed: 'left',
      minWidth: 180,
    },
    {
      field: 'title',
      title: '对话标题',
      minWidth: 180,
      fixed: 'left',
    },
    {
      title: '用户',
      minWidth: 180,
      field: 'userId',
      formatter: ({ cellValue }) => {
        if (cellValue === 0) {
          return '系统';
        }
        return userList.find((user) => user.id === cellValue)?.nickname || '-';
      },
    },
    {
      field: 'roleName',
      title: '角色',
      minWidth: 180,
    },
    {
      field: 'model',
      title: '模型标识',
      minWidth: 180,
    },
    {
      field: 'messageCount',
      title: '消息数',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'temperature',
      title: '温度参数',
      minWidth: 80,
    },
    {
      title: '回复数 Token 数',
      field: 'maxTokens',
      minWidth: 120,
    },
    {
      title: '上下文数量',
      field: 'maxContexts',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchemaMessage(): VbenFormSchema[] {
  return [
    {
      fieldName: 'conversationId',
      label: '对话编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入对话编号',
        clearable: true,
      },
    },
    {
      fieldName: 'userId',
      label: '用户编号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '请选择用户编号',
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
export function useGridColumnsMessage(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '消息编号',
      fixed: 'left',
      minWidth: 180,
    },
    {
      field: 'conversationId',
      title: '对话编号',
      minWidth: 180,
      fixed: 'left',
    },
    {
      title: '用户',
      minWidth: 180,
      field: 'userId',
      formatter: ({ cellValue }) =>
        userList.find((user) => user.id === cellValue)?.nickname || '-',
    },
    {
      field: 'roleName',
      title: '角色',
      minWidth: 180,
    },
    {
      field: 'type',
      title: '消息类型',
      minWidth: 100,
    },
    {
      field: 'model',
      title: '模型标识',
      minWidth: 180,
    },
    {
      field: 'content',
      title: '消息内容',
      minWidth: 300,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'replyId',
      title: '回复消息编号',
      minWidth: 180,
    },
    {
      title: '携带上下文',
      field: 'useContext',
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
      minWidth: 100,
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
