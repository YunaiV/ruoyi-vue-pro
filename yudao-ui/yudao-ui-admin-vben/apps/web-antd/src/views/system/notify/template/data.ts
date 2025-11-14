import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE, UserTypeEnum } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleUserList } from '#/api/system/user';
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
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模板名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'code',
      label: '模板编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模板编码',
      },
      rules: 'required',
    },
    {
      fieldName: 'nickname',
      label: '发送人名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入发送人名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'content',
      label: '模板内容',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入模板内容',
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '模板类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
          'number',
        ),
        placeholder: '请选择模板类型',
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
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入模板名称',
      },
    },
    {
      fieldName: 'code',
      label: '模板编码',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入模板编码',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'type',
      label: '模板类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
          'number',
        ),
        allowClear: true,
        placeholder: '请选择模板类型',
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

/** 发送站内信表单 */
export function useSendNotifyFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'content',
      label: '模板内容',
      component: 'Textarea',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'templateCode',
      label: '模板编码',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'userType',
      label: '用户类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
      },
      rules: z.number().default(UserTypeEnum.MEMBER),
    },
    {
      fieldName: 'userId',
      label: '接收人 ID',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户编号',
      },
      dependencies: {
        show(values) {
          return values.userType === UserTypeEnum.MEMBER;
        },
        triggerFields: ['userType'],
      },
      rules: 'required',
    },
    {
      fieldName: 'userId',
      label: '接收人',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleUserList,
        labelField: 'nickname',
        valueField: 'id',
        placeholder: '请选择接收人',
      },
      dependencies: {
        show(values) {
          return values.userType === UserTypeEnum.ADMIN;
        },
        triggerFields: ['userType'],
      },
      rules: 'required',
    },
    {
      fieldName: 'templateParams',
      label: '模板参数',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '模板名称',
      minWidth: 120,
    },
    {
      field: 'code',
      title: '模板编码',
      minWidth: 120,
    },
    {
      field: 'nickname',
      title: '发送人名称',
      minWidth: 120,
    },
    {
      field: 'content',
      title: '模板内容',
      minWidth: 200,
    },
    {
      field: 'type',
      title: '模板类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE },
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
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
