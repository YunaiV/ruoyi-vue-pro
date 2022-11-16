import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  columns: [
    {
      title: '手机号',
      field: 'mobile',
      isSearch: true
    },
    {
      title: '短信内容',
      field: 'templateContent'
    },
    {
      title: '短信渠道',
      field: 'channelId',
      dictType: DICT_TYPE.SYSTEM_SMS_CHANNEL_CODE,
      dictData: 'number',
      isSearch: true
    },
    {
      title: '发送状态',
      field: 'sendStatus',
      dictType: DICT_TYPE.SYSTEM_SMS_SEND_STATUS,
      dictData: 'number',
      isSearch: true
    },
    {
      title: '接收状态',
      field: 'receiveStatus',
      dictType: DICT_TYPE.SYSTEM_SMS_RECEIVE_STATUS,
      dictData: 'number',
      isSearch: true
    },
    {
      title: '模板编号',
      field: 'templateId',
      isSearch: true
    },
    {
      title: '短信类型',
      field: 'templateType',
      dictType: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE,
      dictData: 'number',
      isSearch: true
    },
    {
      title: '接收时间',
      field: 'receiveTime'
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
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
