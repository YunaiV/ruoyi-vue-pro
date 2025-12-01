import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { getSimpleSmsChannelList } from '#/api/system/sms/channel';
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
      fieldName: 'type',
      label: '短信类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE, 'number'),
        placeholder: '请选择短信类型',
      },
      rules: 'required',
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
      fieldName: 'channelId',
      label: '短信渠道',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleSmsChannelList,
        labelField: 'signature',
        valueField: 'id',
        placeholder: '请选择短信渠道',
      },
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
      fieldName: 'content',
      label: '模板内容',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入模板内容',
        rows: 4,
      },
      rules: 'required',
    },
    {
      fieldName: 'apiTemplateId',
      label: '短信 API 的模板编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入短信 API 的模板编号',
      },
      rules: 'required',
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
      fieldName: 'type',
      label: '短信类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE, 'number'),
        allowClear: true,
        placeholder: '请选择短信类型',
      },
    },
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
      fieldName: 'channelId',
      label: '短信渠道',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleSmsChannelList,
        labelField: 'signature',
        valueField: 'id',
        allowClear: true,
        placeholder: '请选择短信渠道',
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

/** 发送短信表单 */
export function useSendSmsFormSchema(): VbenFormSchema[] {
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
      fieldName: 'mobile',
      label: '手机号码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入手机号码',
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
      field: 'type',
      title: '短信类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE },
      },
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
      field: 'content',
      title: '模板内容',
      minWidth: 200,
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
      field: 'apiTemplateId',
      title: '短信 API 的模板编号',
      minWidth: 180,
    },
    {
      field: 'channelCode',
      title: '短信渠道',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE },
      },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
