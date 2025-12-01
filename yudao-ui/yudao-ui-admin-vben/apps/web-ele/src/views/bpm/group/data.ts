import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemUserApi } from '#/api/system/user';

import { h } from 'vue';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElTag } from 'element-plus';

import { z } from '#/adapter/form';
import { getSimpleUserList } from '#/api/system/user';
import { getRangePickerDefaultProps } from '#/utils';

/** 关联数据 */
let userList: SystemUserApi.User[] = [];
getSimpleUserList().then((data) => (userList = data));

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
      label: '组名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入组名',
      },
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入描述',
      },
    },
    {
      fieldName: 'userIds',
      label: '成员',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请选择成员',
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        mode: 'tags',
      },
      rules: z.array(z.number()).min(1, '请选择成员').default([]),
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
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '组名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入组名',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
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
      minWidth: 100,
    },
    {
      field: 'name',
      title: '组名',
      minWidth: 200,
    },
    {
      field: 'description',
      title: '描述',
      minWidth: 200,
    },
    {
      field: 'userIds',
      title: '成员',
      minWidth: 200,
      slots: {
        default: ({ row }) => {
          const userIds = row.userIds || [];
          return userIds.map((userId: number) =>
            h(
              ElTag,
              {
                type: 'primary',
                class: 'mr-1',
              },
              () => userList.find((u) => u.id === userId)?.nickname,
            ),
          );
        },
      },
    },
    {
      field: 'status',
      title: '状态',
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
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
