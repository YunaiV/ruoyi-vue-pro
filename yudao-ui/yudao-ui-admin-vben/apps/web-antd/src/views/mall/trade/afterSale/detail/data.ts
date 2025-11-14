import type { MallAfterSaleApi } from '#/api/mall/trade/afterSale';
import type { DescriptionItemSchema } from '#/components/description';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { fenToYuan, formatDate } from '@vben/utils';

import { Image } from 'ant-design-vue';

import { DictTag } from '#/components/dict-tag';

/** 订单信息 schema */
export function useOrderInfoSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'orderNo',
      label: '订单号',
    },
    {
      field: 'order.deliveryType',
      label: '配送方式',
      content: (data: MallAfterSaleApi.AfterSale) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_DELIVERY_TYPE,
          value: data?.order?.deliveryType,
        }),
    },
    {
      field: 'order.type',
      label: '订单类型',
      content: (data: MallAfterSaleApi.AfterSale) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_ORDER_TYPE,
          value: data?.order?.type,
        }),
    },
    {
      field: 'order.receiverName',
      label: '收货人',
    },
    {
      field: 'order.userRemark',
      label: '买家留言',
    },
    {
      field: 'order.terminal',
      label: '订单来源',
      content: (data: MallAfterSaleApi.AfterSale) =>
        h(DictTag, {
          type: DICT_TYPE.TERMINAL,
          value: data?.order?.terminal,
        }),
    },
    {
      field: 'order.receiverMobile',
      label: '联系电话',
    },
    {
      field: 'order.remark',
      label: '商家备注',
    },
    {
      field: 'order.payOrderId',
      label: '支付单号',
    },
    {
      field: 'order.payChannelCode',
      label: '付款方式',
      content: (data: MallAfterSaleApi.AfterSale) =>
        h(DictTag, {
          type: DICT_TYPE.PAY_CHANNEL_CODE,
          value: data?.order?.payChannelCode,
        }),
    },
    {
      field: 'user.nickname',
      label: '买家',
    },
  ];
}

/** 售后信息 schema */
export function useAfterSaleInfoSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'no',
      label: '退款编号',
    },
    {
      field: 'auditTime',
      label: '申请时间',
      content: (data: MallAfterSaleApi.AfterSale) =>
        formatDate(data?.auditTime) as string,
    },
    {
      field: 'type',
      label: '售后类型',
      content: (data: MallAfterSaleApi.AfterSale) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_AFTER_SALE_TYPE,
          value: data?.type,
        }),
    },
    {
      field: 'way',
      label: '售后方式',
      content: (data: MallAfterSaleApi.AfterSale) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_AFTER_SALE_WAY,
          value: data?.way,
        }),
    },
    {
      field: 'refundPrice',
      label: '退款金额',
      content: (data: MallAfterSaleApi.AfterSale) =>
        fenToYuan(data?.refundPrice ?? 0),
    },
    {
      field: 'applyReason',
      label: '退款原因',
    },
    {
      field: 'applyDescription',
      label: '补充描述',
    },
    {
      field: 'applyPicUrls',
      label: '凭证图片',
      content: (data) => {
        const images = data?.applyPicUrls || [];
        return h(
          'div',
          { class: 'flex gap-10px' },
          images.map((url: string, index: number) =>
            h(Image, {
              key: index,
              src: url,
              width: 60,
              height: 60,
            }),
          ),
        );
      },
    },
  ];
}

/** 退款状态 schema */
export function useRefundStatusSchema(): DescriptionItemSchema[] {
  return [
    {
      field: 'status',
      label: '退款状态',
      content: (data) =>
        h(DictTag, {
          type: DICT_TYPE.TRADE_AFTER_SALE_STATUS,
          value: data?.status,
        }),
    },
    {
      field: 'reminder',
      label: '提醒',
      content: () =>
        h('div', { class: 'text-red-500 mb-10px' }, [
          h('div', '如果未发货，请点击同意退款给买家。'),
          h('div', '如果实际已发货，请主动与买家联系。'),
          h('div', '如果订单整体退款后，优惠券和余额会退还给买家.'),
        ]),
    },
  ];
}

/** 商品信息 columns */
export function useProductColumns() {
  return [
    {
      field: 'spuName',
      title: '商品信息',
      minWidth: 300,
      slots: { default: 'spuName' },
    },
    {
      field: 'price',
      title: '商品原价',
      minWidth: 150,
      formatter: 'formatFenToYuanAmount',
    },
    {
      field: 'count',
      title: '数量',
      minWidth: 100,
    },
    {
      field: 'payPrice',
      title: '合计',
      minWidth: 150,
      formatter: 'formatFenToYuanAmount',
    },
  ];
}

/** 操作日志 columns */
export function useOperateLogSchema() {
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
    },
  ];
}
