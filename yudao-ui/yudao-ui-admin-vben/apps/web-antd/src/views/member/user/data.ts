import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { h } from 'vue';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { convertToInteger, formatToFraction } from '@vben/utils';

import { Tag } from 'ant-design-vue';

import { z } from '#/adapter/form';
import { getSimpleGroupList } from '#/api/member/group';
import { getSimpleLevelList } from '#/api/member/level';
import { getSimpleTagList } from '#/api/member/tag';
import { getAreaTree } from '#/api/system/area';
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
      fieldName: 'mobile',
      label: '手机号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE).optional(),
    },
    {
      fieldName: 'nickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户昵称',
      },
    },
    {
      fieldName: 'avatar',
      label: '头像',
      component: 'ImageUpload',
    },
    {
      fieldName: 'name',
      label: '真实名字',
      component: 'Input',
      componentProps: {
        placeholder: '请输入真实名字',
      },
    },
    {
      fieldName: 'sex',
      label: '用户性别',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'birthday',
      label: '出生日期',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'x',
        placeholder: '请选择出生日期',
      },
    },
    {
      fieldName: 'areaId',
      label: '所在地',
      component: 'ApiTreeSelect',
      componentProps: {
        api: getAreaTree,
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: '请选择所在地',
      },
    },
    {
      fieldName: 'tagIds',
      label: '用户标签',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleTagList,
        labelField: 'name',
        valueField: 'id',
        mode: 'multiple',
        placeholder: '请选择用户标签',
      },
    },
    {
      fieldName: 'groupId',
      label: '用户分组',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleGroupList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择用户分组',
      },
    },
    {
      fieldName: 'mark',
      label: '会员备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入会员备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'nickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户昵称',
        allowClear: true,
      },
    },
    {
      fieldName: 'mobile',
      label: '手机号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号',
        allowClear: true,
      },
    },
    {
      fieldName: 'loginDate',
      label: '登录时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '注册时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'tagIds',
      label: '用户标签',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleTagList,
        labelField: 'name',
        valueField: 'id',
        mode: 'multiple',
        placeholder: '请选择用户标签',
        allowClear: true,
      },
    },
    {
      fieldName: 'levelId',
      label: '用户等级',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleLevelList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择用户等级',
        allowClear: true,
      },
    },
    {
      fieldName: 'groupId',
      label: '用户分组',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleGroupList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择用户分组',
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      type: 'checkbox',
      width: 50,
    },
    {
      field: 'id',
      title: '用户编号',
      minWidth: 100,
    },
    {
      field: 'avatar',
      title: '头像',
      minWidth: 80,
      cellRender: {
        name: 'CellImage',
      },
    },
    {
      field: 'mobile',
      title: '手机号',
      minWidth: 120,
    },
    {
      field: 'nickname',
      title: '昵称',
      minWidth: 120,
    },
    {
      field: 'levelName',
      title: '等级',
      minWidth: 100,
    },
    {
      field: 'groupName',
      title: '分组',
      minWidth: 100,
    },
    {
      field: 'tagNames',
      title: '用户标签',
      minWidth: 150,
      slots: {
        default: ({ row }) => {
          return row.tagNames?.map((tagName: string, index: number) => {
            return h(
              Tag,
              {
                key: index,
                class: 'mr-1',
                color: 'blue',
              },
              () => tagName,
            );
          });
        },
      },
    },
    {
      field: 'point',
      title: '积分',
      minWidth: 80,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'loginDate',
      title: '登录时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '注册时间',
      minWidth: 160,
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

/** 修改用户等级 */
export function useLevelFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'nickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'levelId',
      label: '用户等级',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleLevelList,
        labelField: 'name',
        valueField: 'id',
        placeholder: '请选择用户等级',
        allowClear: true,
      },
    },
    {
      fieldName: 'reason',
      label: '修改原因',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入修改原因',
      },
      rules: 'required',
    },
  ];
}

/** 修改用户余额 */
export function useBalanceFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'nickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'balance',
      label: '变动前余额(元)',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'changeType',
      label: '变动类型',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '增加', value: 1 },
          { label: '减少', value: -1 },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'changeBalance',
      label: '变动余额(元)',
      component: 'InputNumber',
      rules: 'required',
      componentProps: {
        min: 0,
        precision: 2,
        step: 0.1,
        placeholder: '请输入变动余额',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'balanceResult',
      label: '变动后余额(元)',
      component: 'Input',
      dependencies: {
        triggerFields: ['balance', 'changeBalance', 'changeType'],
        disabled: true,
        trigger(values, form) {
          form.setFieldValue(
            'balanceResult',
            formatToFraction(
              convertToInteger(values.balance) +
                convertToInteger(values.changeBalance) * values.changeType,
            ),
          );
        },
      },
    },
  ];
}

/** 修改用户积分 */
export function usePointFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      label: '用户编号',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'nickname',
      label: '用户昵称',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'point',
      label: '变动前积分',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'changeType',
      label: '变动类型',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '增加', value: 1 },
          { label: '减少', value: -1 },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'changePoint',
      label: '变动积分',
      component: 'InputNumber',
      rules: 'required',
      componentProps: {
        min: 0,
        precision: 0,
        placeholder: '请输入变动积分',
      },
    },
    {
      fieldName: 'pointResult',
      label: '变动后积分',
      component: 'Input',
      componentProps: {
        placeholder: '',
      },
      dependencies: {
        triggerFields: ['point', 'changePoint', 'changeType'],
        disabled: true,
        trigger(values, form) {
          form.setFieldValue(
            'pointResult',
            values.point + values.changePoint * values.changeType ||
              values.point,
          );
        },
      },
      rules: z.number().min(0),
    },
  ];
}
