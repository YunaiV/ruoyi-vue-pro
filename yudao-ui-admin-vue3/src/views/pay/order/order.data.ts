import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  merchantId: [required],
  appId: [required],
  merchantOrderId: [required],
  subject: [required],
  body: [required],
  notifyUrl: [required],
  notifyStatus: [required],
  amount: [required],
  status: [required],
  userIp: [required],
  expireTime: [required],
  refundStatus: [required],
  refundTimes: [required],
  refundAmount: [required]
})
// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '岗位编号',
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
      field: 'channelId'
    },
    {
      title: '渠道编码',
      field: 'channelCode',
      isSearch: true
    },
    {
      title: '渠道订单号',
      field: 'merchantOrderId',
      isSearch: true
    },
    {
      title: '商品标题',
      field: 'subject'
    },
    {
      title: '商品描述',
      field: 'body'
    },
    {
      title: '异步通知地址',
      field: 'notifyUrl'
    },
    {
      title: '回调状态',
      field: 'notifyStatus',
      dictType: DICT_TYPE.PAY_ORDER_NOTIFY_STATUS,
      dictClass: 'number'
    },
    {
      title: '支付金额',
      field: 'amount',
      isSearch: true
    },
    {
      title: '渠道手续费',
      field: 'channelFeeRate',
      isSearch: true
    },
    {
      title: '渠道手续金额',
      field: 'channelFeeAmount',
      isSearch: true
    },
    {
      title: '支付状态',
      field: 'status',
      dictType: DICT_TYPE.PAY_ORDER_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '用户 IP',
      field: 'userIp'
    },
    {
      title: '订单失效时间',
      field: 'expireTime',
      formatter: 'formatDate'
    },
    {
      title: '支付时间',
      field: 'successTime',
      formatter: 'formatDate'
    },
    {
      title: '支付通知时间',
      field: 'notifyTime',
      formatter: 'formatDate'
    },
    {
      title: '拓展编号',
      field: 'successExtensionId'
    },
    {
      title: '退款状态',
      field: 'refundStatus',
      dictType: DICT_TYPE.PAY_ORDER_REFUND_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '退款次数',
      field: 'refundTimes'
    },
    {
      title: '退款总金额',
      field: 'refundAmount'
    },
    {
      title: '渠道用户编号',
      field: 'channelUserId'
    },
    {
      title: '渠道订单号',
      field: 'channelOrderNo'
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
