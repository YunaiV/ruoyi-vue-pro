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
    label: '手机号',
    field: 'mobile',
    search: {
      show: true
    }
  },
  {
    label: '短信内容',
    field: 'templateContent'
  },
  {
    label: '短信渠道',
    field: 'channelId',
    dictType: DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE,
    search: {
      show: true
    }
  },
  {
    label: '发送状态',
    field: 'sendStatus',
    dictType: DICT_TYPE.SYSTEM_SMS_SEND_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '接收状态',
    field: 'receiveTime',
    dictType: DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '模板编号',
    field: 'templateId',
    search: {
      show: true
    }
  },
  {
    label: '短信类型',
    field: 'channelId',
    dictType: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE,
    search: {
      show: true
    }
  },
  {
    label: '接收时间',
    field: 'receiveTime',
    form: {
      show: false
    }
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
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]
      }
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '80px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
