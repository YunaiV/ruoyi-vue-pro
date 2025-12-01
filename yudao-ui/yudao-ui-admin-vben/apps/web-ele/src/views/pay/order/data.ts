import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { erpPriceInputFormatter, formatDateTime } from '@vben/utils';

import { ElTag } from 'element-plus';

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
        clearable: true,
        placeholder: '请输入应用编号',
      },
    },
    {
      fieldName: 'channelCode',
      label: '支付渠道',
      component: 'Select',
      componentProps: {
        clearable: true,
        placeholder: '请选择支付渠道',
        options: getDictOptions(DICT_TYPE.PAY_CHANNEL_CODE, 'string'),
      },
    },
    {
      fieldName: 'merchantOrderId',
      label: '商户单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入商户单号',
      },
    },
    {
      fieldName: 'no',
      label: '支付单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入支付单号',
      },
    },
    {
      fieldName: 'channelOrderNo',
      label: '渠道单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入渠道单号',
      },
    },
    {
      fieldName: 'status',
      label: '支付状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        placeholder: '请选择支付状态',
        options: getDictOptions(DICT_TYPE.PAY_ORDER_STATUS, 'number'),
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
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
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'price',
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
      field: 'channelFeePrice',
      title: '手续金额',
      minWidth: 120,
      formatter: 'formatAmount2',
    },
    {
      field: 'no',
      title: '订单号',
      minWidth: 240,
      slots: {
        default: 'no',
      },
    },
    {
      field: 'status',
      title: '支付状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_ORDER_STATUS },
      },
    },
    {
      field: 'channelCode',
      title: '支付渠道',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.PAY_CHANNEL_CODE },
      },
    },
    {
      field: 'successTime',
      title: '支付时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'appName',
      title: '支付应用',
      minWidth: 150,
    },
    {
      field: 'subject',
      title: '商品标题',
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
    {
      field: 'merchantOrderId',
      label: '商户单号',
    },
    {
      field: 'no',
      label: '支付单号',
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
      field: 'status',
      label: '支付状态',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_ORDER_STATUS,
          value: val,
        }),
    },
    {
      field: 'price',
      label: '支付金额',
      render: (val) => `￥${erpPriceInputFormatter(val)}`,
    },
    {
      field: 'channelFeePrice',
      label: '手续费',
      render: (val) => `￥${erpPriceInputFormatter(val)}`,
    },
    {
      field: 'channelFeeRate',
      label: '手续费比例',
      render: (val) => `${erpPriceInputFormatter(val)}%`,
    },
    {
      field: 'successTime',
      label: '支付时间',
      render: (val) => formatDateTime(val) as string,
    },
    {
      field: 'expireTime',
      label: '失效时间',
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
    {
      field: 'subject',
      label: '商品标题',
    },
    {
      field: 'body',
      label: '商品描述',
    },
    {
      field: 'channelCode',
      label: '支付渠道',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_CHANNEL_CODE,
          value: val,
        }),
    },
    {
      field: 'userIp',
      label: '支付 IP',
    },
    {
      field: 'channelOrderNo',
      label: '渠道单号',
      render: (val) => (val ? h(ElTag, { color: 'green' }, () => val) : ''),
    },
    {
      field: 'channelUserId',
      label: '渠道用户',
    },
    {
      field: 'refundPrice',
      label: '退款金额',
      render: (val) => `￥${erpPriceInputFormatter(val)}`,
    },
    {
      field: 'notifyUrl',
      label: '通知 URL',
    },
    {
      field: 'channelNotifyData',
      label: '支付通道异步回调内容',
    },
  ];
}
