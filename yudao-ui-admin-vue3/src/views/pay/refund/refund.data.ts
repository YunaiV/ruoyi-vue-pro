import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '序号',
  action: true,
  columns: [
    {
      title: '商户编号',
      field: 'merchantId',
      isSearch: true
    },
    {
      title: '应用编号',
      field: 'appId',
      isSearch: true
    },
    {
      title: '渠道编号',
      field: 'channelId',
      isSearch: true
    },
    {
      title: '渠道编码',
      field: 'channelCode',
      dictType: DICT_TYPE.PAY_CHANNEL_CODE_TYPE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '支付订单编号',
      field: 'orderId',
      isSearch: true
    },
    {
      title: '交易订单号',
      field: 'tradeNo',
      isSearch: true
    },
    {
      title: '商户订单号',
      field: 'merchantOrderId',
      isSearch: true
    },
    {
      title: '商户退款单号',
      field: 'merchantRefundNo',
      isSearch: true
    },
    {
      title: '回调地址',
      field: 'notifyUrl',
      isSearch: true
    },
    {
      title: '回调状态',
      field: 'notifyStatus',
      dictType: DICT_TYPE.PAY_ORDER_NOTIFY_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '退款类型',
      field: 'type',
      dictType: DICT_TYPE.PAY_REFUND_ORDER_TYPE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.PAY_REFUND_ORDER_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '支付金额',
      field: 'payAmount',
      formatter: 'formatAmount',
      isSearch: true
    },
    {
      title: '退款金额',
      field: 'refundAmount',
      formatter: 'formatAmount',
      isSearch: true
    },
    {
      title: '退款原因',
      field: 'reason',
      isSearch: true
    },
    {
      title: '用户IP',
      field: 'userIp',
      isSearch: true
    },
    {
      title: '渠道订单号',
      field: 'channelOrderNo',
      isSearch: true
    },
    {
      title: '渠道退款单号',
      field: 'channelRefundNo',
      isSearch: true
    },
    {
      title: '渠道调用报错时',
      field: 'channelErrorCode'
    },
    {
      title: '渠道调用报错时',
      field: 'channelErrorMsg'
    },
    {
      title: '支付渠道的额外参数',
      field: 'channelExtras'
    },
    {
      title: '退款失效时间',
      field: 'expireTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '退款成功时间',
      field: 'successTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '退款通知时间',
      field: 'notifyTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
