import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';

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
      fieldName: 'mail',
      label: '邮箱',
      component: 'Input',
      componentProps: {
        placeholder: '请输入邮箱',
      },
      rules: 'required',
    },
    {
      fieldName: 'username',
      label: '用户名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名',
      },
      rules: 'required',
    },
    {
      fieldName: 'password',
      label: '密码',
      component: 'InputPassword',
      componentProps: {
        placeholder: '请输入密码',
      },
      rules: 'required',
    },
    {
      fieldName: 'host',
      label: 'SMTP 服务器域名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 SMTP 服务器域名',
      },
      rules: 'required',
    },
    {
      fieldName: 'port',
      label: 'SMTP 服务器端口',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入 SMTP 服务器端口',
        min: 0,
        max: 65_535,
      },
      rules: 'required',
    },
    {
      fieldName: 'sslEnable',
      label: '是否开启 SSL',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.boolean().default(true),
    },
    {
      fieldName: 'starttlsEnable',
      label: '是否开启 STARTTLS',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.boolean().default(false),
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
      fieldName: 'mail',
      label: '邮箱',
      component: 'Input',
      componentProps: {
        placeholder: '请输入邮箱',
        allowClear: true,
      },
    },
    {
      fieldName: 'username',
      label: '用户名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名',
        allowClear: true,
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
      field: 'mail',
      title: '邮箱',
      minWidth: 160,
    },
    {
      field: 'username',
      title: '用户名',
      minWidth: 160,
    },
    {
      field: 'host',
      title: 'SMTP 服务器域名',
      minWidth: 150,
    },
    {
      field: 'port',
      title: 'SMTP 服务器端口',
      minWidth: 130,
    },
    {
      field: 'sslEnable',
      title: '是否开启 SSL',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'starttlsEnable',
      title: '是否开启 STARTTLS',
      minWidth: 145,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
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
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
