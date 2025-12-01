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
        placeholder: '请选择用户编号',
        clearable: true,
      },
    },
    {
      fieldName: 'title',
      label: '音乐名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入音乐名称',
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '绘画状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择绘画状态',
        clearable: true,
        options: getDictOptions(DICT_TYPE.AI_MUSIC_STATUS, 'number'),
      },
    },
    {
      fieldName: 'generateMode',
      label: '生成模式',
      component: 'Select',
      componentProps: {
        placeholder: '请选择生成模式',
        clearable: true,
        options: getDictOptions(DICT_TYPE.AI_GENERATE_MODE, 'number'),
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
    {
      fieldName: 'publicStatus',
      label: '是否发布',
      component: 'Select',
      componentProps: {
        placeholder: '请选择是否发布',
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  onPublicStatusChange?: (
    newStatus: boolean,
    row: any,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 180,
      fixed: 'left',
    },
    {
      title: '音乐名称',
      minWidth: 180,
      fixed: 'left',
      field: 'title',
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
      field: 'status',
      title: '音乐状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_MUSIC_STATUS },
      },
    },
    {
      field: 'model',
      title: '模型',
      minWidth: 180,
    },
    {
      title: '内容',
      minWidth: 180,
      slots: { default: 'content' },
    },
    {
      field: 'duration',
      title: '时长（秒）',
      minWidth: 100,
    },
    {
      field: 'prompt',
      title: '提示词',
      minWidth: 180,
    },
    {
      field: 'lyric',
      title: '歌词',
      minWidth: 180,
    },
    {
      field: 'gptDescriptionPrompt',
      title: '描述',
      minWidth: 180,
    },
    {
      field: 'generateMode',
      title: '生成模式',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.AI_GENERATE_MODE },
      },
    },
    {
      field: 'tags',
      title: '风格标签',
      minWidth: 180,
      cellRender: {
        name: 'CellTags',
      },
    },
    {
      minWidth: 100,
      title: '是否发布',
      field: 'publicStatus',
      align: 'center',
      cellRender: {
        attrs: { beforeChange: onPublicStatusChange },
        name: 'CellSwitch',
        props: {
          activeValue: true,
          inactiveValue: false,
        },
      },
    },
    {
      field: 'taskId',
      title: '任务编号',
      minWidth: 180,
    },
    {
      field: 'errorMessage',
      title: '错误信息',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
