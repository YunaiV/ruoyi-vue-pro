import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { getSimpleSmsChannelList } from '#/api/system/sms/channel';
import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'mobile',
      label: '手机号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入手机号',
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
      fieldName: 'templateId',
      label: '模板编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入模板编号',
      },
    },
    {
      fieldName: 'sendStatus',
      label: '发送状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SMS_SEND_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择发送状态',
      },
    },
    {
      fieldName: 'sendTime',
      label: '发送时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
    {
      fieldName: 'receiveStatus',
      label: '接收状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS, 'number'),
        allowClear: true,
        placeholder: '请选择接收状态',
      },
    },
    {
      fieldName: 'receiveTime',
      label: '接收时间',
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
      field: 'mobile',
      title: '手机号',
      minWidth: 120,
    },
    {
      field: 'templateContent',
      title: '短信内容',
      minWidth: 300,
    },
    {
      field: 'sendStatus',
      title: '发送状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SMS_SEND_STATUS },
      },
    },
    {
      field: 'sendTime',
      title: '发送时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'receiveStatus',
      title: '接收状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS },
      },
    },
    {
      field: 'receiveTime',
      title: '接收时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'channelCode',
      title: '短信渠道',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE },
      },
    },
    {
      field: 'templateId',
      title: '模板编号',
      minWidth: 100,
    },
    {
      field: 'templateType',
      title: '短信类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE },
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
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 详情页的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'createTime',
      label: '创建时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'mobile',
      label: '手机号',
    },
    {
      field: 'channelCode',
      label: '短信渠道',
    },
    {
      field: 'templateId',
      label: '模板编号',
    },
    {
      field: 'templateType',
      label: '模板类型',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE,
          value: val,
        });
      },
    },
    {
      field: 'templateContent',
      label: '短信内容',
    },
    {
      field: 'sendStatus',
      label: '发送状态',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_SMS_SEND_STATUS,
          value: val,
        });
      },
    },
    {
      field: 'sendTime',
      label: '发送时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'apiSendCode',
      label: 'API 发送编码',
    },
    {
      field: 'apiSendMsg',
      label: 'API 发送消息',
    },
    {
      field: 'receiveStatus',
      label: '接收状态',
      render: (val) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS,
          value: val,
        });
      },
    },
    {
      field: 'receiveTime',
      label: '接收时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'apiReceiveCode',
      label: 'API 接收编码',
    },
    {
      field: 'apiReceiveMsg',
      label: 'API 接收消息',
      span: 2,
    },
    {
      field: 'apiRequestId',
      label: 'API 请求 ID',
    },
    {
      field: 'apiSerialNo',
      label: 'API 序列号',
    },
  ];
}
