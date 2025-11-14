import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayNotifyApi } from '#/api/pay/notify';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { formatDateTime } from '@vben/utils';

import { getAppList } from '#/api/pay/app';
import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'appId',
      label: '应用编号',
      component: 'ApiSelect',
      componentProps: {
        api: async () => {
          const data = await getAppList();
          return data.map((item) => ({
            label: item.name,
            value: item.id,
          }));
        },
        autoSelect: 'first',
        placeholder: '请选择应用编号',
      },
    },
    {
      fieldName: 'type',
      label: '通知类型',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.PAY_NOTIFY_TYPE, 'number'),
        placeholder: '请选择通知类型',
      },
    },
    {
      fieldName: 'dataId',
      label: '关联编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入关联编号',
      },
    },
    {
      fieldName: 'status',
      label: '通知状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.PAY_NOTIFY_STATUS, 'number'),
        placeholder: '请选择通知状态',
      },
    },
    {
      fieldName: 'merchantOrderId',
      label: '商户订单编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商户订单编号',
      },
    },
    {
      fieldName: 'merchantRefundId',
      label: '商户退款编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商户退款编号',
      },
    },
    {
      fieldName: 'merchantTransferId',
      label: '商户转账编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商户转账编号',
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
        placeholder: ['开始日期', '结束日期'],
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '任务编号',
      minWidth: 100,
    },
    {
      field: 'appName',
      title: '应用名称',
      minWidth: 150,
    },
    {
      field: 'merchantInfo',
      title: '商户单信息',
      minWidth: 240,
      slots: {
        default: 'merchantInfo',
      },
    },
    {
      field: 'type',
      title: '通知类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_NOTIFY_TYPE },
      },
    },
    {
      field: 'dataId',
      title: '关联编号',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '通知状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_NOTIFY_STATUS },
      },
    },
    {
      field: 'lastExecuteTime',
      title: '最后通知时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'nextNotifyTime',
      title: '下次通知时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'notifyTimes',
      title: '通知次数',
      minWidth: 120,
      formatter: ({ row }) => `${row.notifyTimes} / ${row.maxNotifyTimes}`,
    },
    {
      title: '操作',
      width: 80,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 详情的字段 */
export function useDetailSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'appId',
      label: '应用编号',
    },
    {
      field: 'appName',
      label: '应用名称',
    },
    {
      field: 'type',
      label: '通知类型',
      content: (data: PayNotifyApi.NotifyTask) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_NOTIFY_TYPE,
          value: data?.type,
        }),
    },
    {
      field: 'dataId',
      label: '关联编号',
    },
    {
      field: 'status',
      label: '通知状态',
      content: (data: PayNotifyApi.NotifyTask) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_NOTIFY_STATUS,
          value: data?.status,
        }),
    },
    {
      field: 'merchantOrderId',
      label: '商户订单编号',
    },
    {
      field: 'lastExecuteTime',
      label: '最后通知时间',
      content: (data: PayNotifyApi.NotifyTask) =>
        formatDateTime(data?.lastExecuteTime) as string,
    },
    {
      field: 'nextNotifyTime',
      label: '下次通知时间',
      content: (data: PayNotifyApi.NotifyTask) =>
        formatDateTime(data?.nextNotifyTime) as string,
    },
    {
      field: 'notifyTimes',
      label: '通知次数',
    },
    {
      field: 'maxNotifyTimes',
      label: '最大通知次数',
    },
    {
      field: 'createTime',
      label: '创建时间',
      content: (data: PayNotifyApi.NotifyTask) =>
        formatDateTime(data?.createTime) as string,
    },
    {
      field: 'updateTime',
      label: '更新时间',
      content: (data: PayNotifyApi.NotifyTask) =>
        formatDateTime(data?.updateTime) as string,
    },
  ];
}

/** 详情的日志字段 */
export function useDetailLogColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'id',
      title: '日志编号',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '通知状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_NOTIFY_STATUS },
      },
    },
    {
      field: 'notifyTimes',
      title: '通知次数',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '通知时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'response',
      title: '响应结果',
      minWidth: 200,
    },
  ];
}
