import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '配置名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入配置名',
      },
      rules: 'required',
    },
    {
      fieldName: 'storage',
      label: '存储器',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_FILE_STORAGE, 'number'),
        placeholder: '请选择存储器',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['id'],
        disabled: (formValues) => formValues.id,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
    // DB / Local / FTP / SFTP
    {
      fieldName: 'config.basePath',
      label: '基础路径',
      component: 'Input',
      componentProps: {
        placeholder: '请输入基础路径',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) =>
          formValues.storage >= 10 && formValues.storage <= 12,
      },
    },
    {
      fieldName: 'config.host',
      label: '主机地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入主机地址',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) =>
          formValues.storage >= 11 && formValues.storage <= 12,
      },
    },
    {
      fieldName: 'config.port',
      label: '主机端口',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入主机端口',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) =>
          formValues.storage >= 11 && formValues.storage <= 12,
      },
    },
    {
      fieldName: 'config.username',
      label: '用户名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入用户名',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) =>
          formValues.storage >= 11 && formValues.storage <= 12,
      },
    },
    {
      fieldName: 'config.password',
      label: '密码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入密码',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) =>
          formValues.storage >= 11 && formValues.storage <= 12,
      },
    },
    {
      fieldName: 'config.mode',
      label: '连接模式',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '主动模式', value: 'Active' },
          { label: '被动模式', value: 'Passive' },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 11,
      },
    },
    // S3
    {
      fieldName: 'config.endpoint',
      label: '节点地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入节点地址',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 20,
      },
    },
    {
      fieldName: 'config.bucket',
      label: '存储 bucket',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 bucket',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 20,
      },
    },
    {
      fieldName: 'config.accessKey',
      label: 'accessKey',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 accessKey',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 20,
      },
    },
    {
      fieldName: 'config.accessSecret',
      label: 'accessSecret',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 accessSecret',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 20,
      },
    },
    {
      fieldName: 'config.enablePathStyleAccess',
      label: '是否 Path Style',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '启用', value: true },
          { label: '禁用', value: false },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 20,
      },
      defaultValue: false,
    },
    {
      fieldName: 'config.enablePublicAccess',
      label: '公开访问',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '公开', value: true },
          { label: '私有', value: false },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => formValues.storage === 20,
      },
      defaultValue: false,
    },
    // 通用
    {
      fieldName: 'config.domain',
      label: '自定义域名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入自定义域名',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['storage'],
        show: (formValues) => !!formValues.storage,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '配置名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入配置名',
        allowClear: true,
      },
    },
    {
      fieldName: 'storage',
      label: '存储器',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_FILE_STORAGE, 'number'),
        placeholder: '请选择存储器',
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
    { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'name',
      title: '配置名',
      minWidth: 120,
    },
    {
      field: 'storage',
      title: '存储器',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_FILE_STORAGE },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 150,
    },
    {
      field: 'master',
      title: '主配置',
      minWidth: 100,
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
      width: 240,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
