import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
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
const crudSchemas = reactive<CrudSchema[]>([
  {
    label: t('common.index'),
    field: 'id',
    type: 'index',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: '商户编号',
    field: 'merchantId',
    search: {
      show: true
    }
  },
  {
    label: '应用编号',
    field: 'appId',
    search: {
      show: true
    }
  },
  {
    label: '渠道编号',
    field: 'channelId'
  },
  {
    label: '渠道编码',
    field: 'channelCode',
    search: {
      show: true
    }
  },
  {
    label: '渠道订单号',
    field: 'merchantOrderId',
    search: {
      show: true
    }
  },
  {
    label: '商品标题',
    field: 'subject'
  },
  {
    label: '商品描述',
    field: 'body'
  },
  {
    label: '异步通知地址',
    field: 'notifyUrl'
  },
  {
    label: '回调商户状态',
    field: 'notifyStatus',
    dictType: DICT_TYPE.PAY_ORDER_NOTIFY_STATUS
  },
  {
    label: '支付金额',
    field: 'amount',
    search: {
      show: true
    }
  },
  {
    label: '渠道手续费',
    field: 'channelFeeRate',
    search: {
      show: true
    }
  },
  {
    label: '渠道手续金额',
    field: 'channelFeeAmount',
    search: {
      show: true
    }
  },
  {
    label: '支付状态',
    field: 'status',
    dictType: DICT_TYPE.PAY_ORDER_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '用户 IP',
    field: 'userIp'
  },
  {
    label: '订单失效时间',
    field: 'expireTime'
  },
  {
    label: '订单支付成功时间',
    field: 'successTime'
  },
  {
    label: '订单支付通知时间',
    field: 'notifyTime'
  },
  {
    label: '支付成功的订单拓展单编号',
    field: 'successExtensionId'
  },
  {
    label: '退款状态',
    field: 'refundStatus',
    dictType: DICT_TYPE.PAY_ORDER_REFUND_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '退款次数',
    field: 'refundTimes'
  },
  {
    label: '退款总金额',
    field: 'refundAmount'
  },
  {
    label: '渠道用户编号',
    field: 'channelUserId'
  },
  {
    label: '渠道订单号',
    field: 'channelOrderNo'
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    form: {
      show: false
    },
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'datetimerange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]
      }
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '270px',
    form: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
