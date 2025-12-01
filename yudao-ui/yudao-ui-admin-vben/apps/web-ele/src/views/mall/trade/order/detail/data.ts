import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { fenToYuan, formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';

/** 订单基础信息 schema */
export function useOrderInfoSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'no',
      label: '订单号',
    },
    {
      field: 'user.nickname',
      label: '买家',
    },
    {
      field: 'type',
      label: '订单类型',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_ORDER_TYPE,
          value: val,
        }),
    },
    {
      field: 'terminal',
      label: '订单来源',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.TERMINAL,
          value: val,
        }),
    },
    {
      field: 'userRemark',
      label: '买家留言',
    },
    {
      field: 'remark',
      label: '商家备注',
    },
    {
      field: 'payOrderId',
      label: '支付单号',
    },
    {
      field: 'payChannelCode',
      label: '付款方式',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_CHANNEL_CODE,
          value: val,
        }),
    },
    {
      field: 'brokerageUser.nickname',
      label: '推广用户',
    },
  ];
}

/** 订单状态信息 schema */
export function useOrderStatusSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'status',
      label: '订单状态',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_ORDER_STATUS,
          value: val,
        }),
    },
    {
      field: 'reminder',
      label: '提醒',
      render: () =>
        h('div', { class: 'space-y-1' }, [
          h('div', '买家付款成功后，货款将直接进入您的商户号（微信、支付宝）'),
          h('div', '请及时关注你发出的包裹状态，确保可以配送至买家手中'),
          h(
            'div',
            '如果买家表示没收到货或货物有问题，请及时联系买家处理，友好协商',
          ),
        ]),
    },
  ];
}

/** 订单金额信息 schema */
export function useOrderPriceSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'totalPrice',
      label: '商品总额',
      render: (val) => `${fenToYuan(val ?? 0)} 元`,
    },
    {
      field: 'deliveryPrice',
      label: '运费金额',
      render: (val) => `${fenToYuan(val ?? 0)} 元`,
    },
    {
      field: 'adjustPrice',
      label: '订单调价',
      render: (val) => `${fenToYuan(val ?? 0)} 元`,
    },
    {
      field: 'couponPrice',
      label: '优惠劵优惠',
      render: (val) =>
        h('span', { class: 'text-red-500' }, `${fenToYuan(val ?? 0)} 元`),
    },
    {
      field: 'vipPrice',
      label: 'VIP 优惠',
      render: (val) =>
        h('span', { class: 'text-red-500' }, `${fenToYuan(val ?? 0)} 元`),
    },
    {
      field: 'discountPrice',
      label: '活动优惠',
      render: (val) =>
        h('span', { class: 'text-red-500' }, `${fenToYuan(val ?? 0)} 元`),
    },
    {
      field: 'pointPrice',
      label: '积分抵扣',
      render: (val) =>
        h('span', { class: 'text-red-500' }, `${fenToYuan(val ?? 0)} 元`),
    },
    {
      field: 'payPrice',
      label: '应付金额',
      render: (val) => `${fenToYuan(val ?? 0)} 元`,
    },
  ];
}

/** 收货信息 schema */
export function useDeliveryInfoSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'deliveryType',
      label: '配送方式',
      render: (val) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_DELIVERY_TYPE,
          value: val,
        }),
    },
    {
      field: 'receiverName',
      label: '收货人',
    },
    {
      field: 'receiverMobile',
      label: '联系电话',
    },
    {
      field: 'receiverAddress',
      label: '收货地址',
      render: (val, data) => `${data?.receiverAreaName} ${val}`.trim(),
    },
    {
      field: 'deliveryTime',
      label: '发货时间',
      render: (val) => formatDateTime(val) as string,
    },
  ];
}

/** 商品信息 columns */
export function useProductColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'spuName',
      title: '商品',
      minWidth: 300,
      slots: { default: 'spuName' },
    },
    {
      field: 'price',
      title: '商品原价',
      width: 150,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'count',
      title: '数量',
      width: 100,
    },
    {
      field: 'payPrice',
      title: '合计',
      width: 150,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'afterSaleStatus',
      title: '售后状态',
      width: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.TRADE_ORDER_ITEM_AFTER_SALE_STATUS },
      },
    },
  ];
}

/** 物流详情 columns */
export function useExpressTrackColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'time',
      title: '时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'content',
      title: '物流状态',
      minWidth: 300,
    },
  ];
}

/** 操作日志 columns */
export function useOperateLogColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'createTime',
      title: '操作时间',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'userType',
      title: '操作人',
      width: 100,
      slots: { default: 'userType' },
    },
    {
      field: 'content',
      title: '操作内容',
      minWidth: 200,
    },
  ];
}
