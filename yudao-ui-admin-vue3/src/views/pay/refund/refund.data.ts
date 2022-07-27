import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

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
    label: '应用编号',
    field: 'appId',
    search: {
      show: true
    }
  },
  {
    label: '商户名称',
    field: 'merchantName'
  },
  {
    label: '应用名称',
    field: 'appName'
  },
  {
    label: '商品名称',
    field: 'subject'
  },
  {
    label: '商户退款单号',
    field: 'merchantRefundNo'
  },
  {
    label: '商户订单号',
    field: 'merchantOrderId'
  },
  {
    label: '交易订单号',
    field: 'tradeNo'
  },
  {
    label: '支付金额',
    field: 'payAmount'
  },
  {
    label: '退款金额',
    field: 'refundAmount'
  },
  {
    label: '渠道编码',
    field: 'channelCode',
    search: {
      show: true
    }
  },
  {
    label: '退款类型',
    field: 'type',
    dictType: DICT_TYPE.PAY_REFUND_ORDER_TYPE,
    search: {
      show: true
    }
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.PAY_REFUND_ORDER_STATUS,
    search: {
      show: true
    }
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    form: {
      show: false
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '240px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
