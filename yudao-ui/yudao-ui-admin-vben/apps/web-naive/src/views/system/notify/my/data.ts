import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'readStatus',
      label: '是否已读',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        clearable: true,
        placeholder: '请选择是否已读',
      },
    },
    {
      fieldName: 'createTime',
      label: '发送时间',
      component: 'DatePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      title: '',
      width: 40,
      type: 'checkbox',
    },
    {
      field: 'templateNickname',
      title: '发送人',
      minWidth: 180,
    },
    {
      field: 'createTime',
      title: '发送时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'templateType',
      title: '类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE },
      },
    },
    {
      field: 'templateContent',
      title: '消息内容',
      minWidth: 300,
    },
    {
      field: 'readStatus',
      title: '是否已读',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'readTime',
      title: '阅读时间',
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

export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'templateNickname',
      label: '发送人',
    },
    {
      field: 'createTime',
      label: '发送时间',
      content: (data: SystemNotifyMessageApi.NotifyMessage) => {
        return formatDateTime(data?.createTime || '') as string;
      },
    },
    {
      field: 'templateType',
      label: '消息类型',
      content: (data: SystemNotifyMessageApi.NotifyMessage) => {
        return h(DictTag, {
          type: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
          value: data?.templateType,
        });
      },
    },
    {
      field: 'readStatus',
      label: '是否已读',
      content: (data: SystemNotifyMessageApi.NotifyMessage) => {
        return h(DictTag, {
          type: DICT_TYPE.INFRA_BOOLEAN_STRING,
          value: data?.readStatus,
        });
      },
    },
    {
      field: 'readTime',
      label: '阅读时间',
      content: (data: SystemNotifyMessageApi.NotifyMessage) => {
        return formatDateTime(data?.readTime || '') as string;
      },
    },
    {
      field: 'templateContent',
      label: '消息内容',
    },
  ];
}
