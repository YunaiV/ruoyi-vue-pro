import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
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
      fieldName: 'clientId',
      label: '客户端编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户端编号',
      },
      rules: 'required',
    },
    {
      fieldName: 'secret',
      label: '客户端密钥',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户端密钥',
      },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '应用名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入应用名',
      },
      rules: 'required',
    },
    {
      fieldName: 'logo',
      label: '应用图标',
      component: 'ImageUpload',
      rules: 'required',
    },
    {
      fieldName: 'description',
      label: '应用描述',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入应用描述',
      },
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
      fieldName: 'accessTokenValiditySeconds',
      label: '访问令牌的有效期',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入访问令牌的有效期，单位：秒',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'refreshTokenValiditySeconds',
      label: '刷新令牌的有效期',
      component: 'InputNumber',
      componentProps: {
        placeholder: '请输入刷新令牌的有效期，单位：秒',
        min: 0,
      },
      rules: 'required',
    },
    {
      fieldName: 'authorizedGrantTypes',
      label: '授权类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_OAUTH2_GRANT_TYPE),
        mode: 'multiple',
        placeholder: '请输入授权类型',
      },
      rules: 'required',
    },
    {
      fieldName: 'scopes',
      label: '授权范围',
      component: 'Select',
      componentProps: {
        placeholder: '请输入授权范围',
        mode: 'tags',
        allowClear: true,
      },
    },
    {
      fieldName: 'autoApproveScopes',
      label: '自动授权范围',
      component: 'Select',
      componentProps: {
        placeholder: '请输入自动授权范围',
        mode: 'multiple',
      },
      dependencies: {
        triggerFields: ['scopes'],
        componentProps: (values) => ({
          options: values.scopes
            ? values.scopes.map((scope: string) => ({
                label: scope,
                value: scope,
              }))
            : [],
        }),
      },
    },
    {
      fieldName: 'redirectUris',
      label: '可重定向的 URI 地址',
      component: 'Select',
      componentProps: {
        placeholder: '请输入可重定向的 URI 地址',
        mode: 'tags',
      },
      rules: 'required',
    },
    {
      fieldName: 'authorities',
      label: '权限',
      component: 'Select',
      componentProps: {
        placeholder: '请输入权限',
        mode: 'tags',
      },
    },
    {
      fieldName: 'resourceIds',
      label: '资源',
      component: 'Select',
      componentProps: {
        mode: 'tags',
        placeholder: '请输入资源',
      },
    },
    {
      fieldName: 'additionalInformation',
      label: '附加信息',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入附加信息，JSON 格式数据',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '应用名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入应用名',
        allowClear: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        allowClear: true,
        placeholder: '请输入状态',
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'clientId',
      title: '客户端编号',
      minWidth: 120,
    },
    {
      field: 'secret',
      title: '客户端密钥',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '应用名',
      minWidth: 120,
    },
    {
      field: 'logo',
      title: '应用图标',
      minWidth: 100,
      cellRender: {
        name: 'CellImage',
      },
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
      field: 'accessTokenValiditySeconds',
      title: '访问令牌的有效期',
      minWidth: 150,
      formatter: ({ cellValue }) => `${cellValue} 秒`,
    },
    {
      field: 'refreshTokenValiditySeconds',
      title: '刷新令牌的有效期',
      minWidth: 150,
      formatter: ({ cellValue }) => `${cellValue} 秒`,
    },
    {
      field: 'authorizedGrantTypes',
      title: '授权类型',
      minWidth: 100,
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
