import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleMailAccountList } from '#/api/system/mail/account';
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
      fieldName: 'accountId',
      label: '邮箱账号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleMailAccountList,
        labelField: 'mail',
        valueField: 'id',
        placeholder: '请选择邮箱账号',
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
    },
    {
      fieldName: 'title',
      label: '模板标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入模板标题',
      },
      rules: 'required',
    },
    {
      fieldName: 'content',
      label: '模板内容',
      component: 'RichTextarea',
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '开启状态',
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

/** 发送邮件表单 */
export function useSendMailFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'templateParams',
      label: '模板参数',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'content',
      label: '模板内容',
      component: 'RichTextarea',
      componentProps: {
        options: {
          readonly: true,
        },
      },
    },
    {
      fieldName: 'toMails',
      label: '收件邮箱',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        allowClear: true,
        placeholder: '请输入收件邮箱，按 Enter 添加',
      },
    },
    {
      fieldName: 'ccMails',
      label: '抄送邮箱',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        allowClear: true,
        placeholder: '请输入抄送邮箱，按 Enter 添加',
      },
    },
    {
      fieldName: 'bccMails',
      label: '密送邮箱',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        allowClear: true,
        placeholder: '请输入密送邮箱，按 Enter 添加',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择开启状态',
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
      fieldName: 'name',
      label: '模板名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入模板名称',
      },
    },
    {
      fieldName: 'accountId',
      label: '邮箱账号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleMailAccountList,
        labelField: 'mail',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择邮箱账号',
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
export function useGridColumns(
  getAccountMail?: (accountId: number) => string | undefined,
): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'code',
      title: '模板编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '模板名称',
      minWidth: 120,
    },
    {
      field: 'title',
      title: '模板标题',
      minWidth: 120,
    },
    {
      field: 'accountId',
      title: '邮箱账号',
      minWidth: 120,
      formatter: ({ cellValue }) => getAccountMail?.(cellValue) || '-',
    },
    {
      field: 'nickname',
      title: '发送人名称',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '开启状态',
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
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
