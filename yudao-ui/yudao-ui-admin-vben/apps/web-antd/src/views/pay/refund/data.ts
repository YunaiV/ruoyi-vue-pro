import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { erpPriceInputFormatter, formatDateTime } from '@vben/utils';

import { Tag } from 'ant-design-vue';

import { DictTag } from '#/components/dict-tag';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'appId',
      label: '应用编号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入应用编号',
      },
    },
    {
      fieldName: 'channelCode',
      label: '退款渠道',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择退款渠道',
        options: getDictOptions(DICT_TYPE.PAY_CHANNEL_CODE, 'string'),
      },
    },
    {
      fieldName: 'merchantOrderId',
      label: '商户单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入商户单号',
      },
    },
    {
      fieldName: 'merchantRefundId',
      label: '退款单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入退款单号',
      },
    },
    {
      fieldName: 'channelOrderNo',
      label: '渠道单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入渠道单号',
      },
    },
    {
      fieldName: 'channelRefundNo',
      label: '渠道退款单号',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入渠道退款单号',
      },
    },
    {
      fieldName: 'status',
      label: '退款状态',
      component: 'Select',
      componentProps: {
        allowClear: true,
        placeholder: '请选择退款状态',
        options: getDictOptions(DICT_TYPE.PAY_REFUND_STATUS, 'number'),
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
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'payPrice',
      title: '支付金额',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'refundPrice',
      title: '退款金额',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'merchantRefundId',
      title: '退款单号',
      minWidth: 240,
      slots: {
        default: 'no',
      },
    },
    {
      field: 'status',
      title: '退款状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_REFUND_STATUS },
      },
    },
    {
      field: 'channelCode',
      title: '退款渠道',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_CHANNEL_CODE },
      },
    },
    {
      field: 'successTime',
      title: '退款时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'reason',
      title: '退款原因',
      minWidth: 200,
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
    // 基本信息部分
    {
      field: 'merchantRefundId',
      label: '商户退款单号',
      render: (val) => h(Tag, {}, () => val || '-'),
    },
    {
      field: 'channelRefundNo',
      label: '渠道退款单号',
      render: (val) => h(Tag, { color: 'success' }, () => val || '-'),
    },
    {
      field: 'merchantOrderId',
      label: '商户支付单号',
      render: (val) => h(Tag, {}, () => val || '-'),
    },
    {
      field: 'channelOrderNo',
      label: '渠道支付单号',
      render: (val) => h(Tag, { color: 'success' }, () => val || '-'),
    },
    {
      field: 'appId',
      label: '应用编号',
    },
    {
      field: 'appName',
      label: '应用名称',
    },
    {
      field: 'payPrice',
      label: '支付金额',
      render: (val) =>
        h(
          Tag,
          { color: 'success' },
          () => `￥${erpPriceInputFormatter(val || 0)}`,
        ),
    },
    {
      field: 'refundPrice',
      label: '退款金额',
      render: (val) =>
        h(
          Tag,
          { color: 'danger' },
          () => `￥${erpPriceInputFormatter(val || 0)}`,
        ),
    },
    {
      field: 'status',
      label: '退款状态',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_REFUND_STATUS,
          value: val,
        }),
    },
    {
      field: 'successTime',
      label: '退款时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'createTime',
      label: '创建时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'updateTime',
      label: '更新时间',
      render: (val) => formatDateTime(val) as string,
    },
    // 渠道信息部分
    {
      field: 'channelCode',
      label: '退款渠道',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_CHANNEL_CODE,
          value: val,
        }),
    },
    {
      field: 'reason',
      label: '退款原因',
    },
    {
      field: 'userIp',
      label: '退款 IP',
    },
    {
      field: 'notifyUrl',
      label: '通知 URL',
    },
    // 错误信息部分
    {
      field: 'channelErrorCode',
      label: '渠道错误码',
    },
    {
      field: 'channelErrorMsg',
      label: '渠道错误码描述',
    },
    {
      field: 'channelNotifyData',
      label: '支付通道异步回调内容',
    },
  ];
}
