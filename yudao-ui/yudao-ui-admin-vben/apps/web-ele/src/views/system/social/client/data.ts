import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import {
  CommonStatusEnum,
  DICT_TYPE,
  SystemUserSocialTypeEnum,
} from '@vben/constants';
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
      fieldName: 'name',
      label: '应用名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入应用名',
      },
      rules: 'required',
    },
    {
      fieldName: 'socialType',
      label: '社交平台',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SOCIAL_TYPE, 'number'),
        placeholder: '请选择社交平台',
      },
      rules: 'required',
    },
    {
      fieldName: 'userType',
      label: '用户类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
      },
      rules: 'required',
    },
    {
      fieldName: 'clientId',
      label: '客户端编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户端编号，对应各平台的 appKey',
      },
      rules: 'required',
    },
    {
      fieldName: 'clientSecret',
      label: '客户端密钥',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户端密钥，对应各平台的 appSecret',
      },
      rules: 'required',
    },
    {
      fieldName: 'agentId',
      label: 'agentId',
      component: 'Input',
      componentProps: {
        placeholder: '授权方的网页应用 ID，有则填',
      },
      dependencies: {
        triggerFields: ['socialType'],
        show: (values) =>
          values.socialType === SystemUserSocialTypeEnum.WECHAT_ENTERPRISE.type,
      },
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
      label: '应用名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入应用名',
        clearable: true,
      },
    },
    {
      fieldName: 'socialType',
      label: '社交平台',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SOCIAL_TYPE, 'number'),
        placeholder: '请选择社交平台',
        clearable: true,
      },
    },
    {
      fieldName: 'userType',
      label: '用户类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.USER_TYPE, 'number'),
        placeholder: '请选择用户类型',
        clearable: true,
      },
    },
    {
      fieldName: 'clientId',
      label: '客户端编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户端编号',
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
        clearable: true,
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
      title: '应用名',
      minWidth: 120,
    },
    {
      field: 'socialType',
      title: '社交平台',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SOCIAL_TYPE },
      },
    },
    {
      field: 'userType',
      title: '用户类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.USER_TYPE },
      },
    },
    {
      field: 'clientId',
      title: '客户端编号',
      minWidth: 180,
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
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
