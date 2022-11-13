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
      title: '日志类型',
      field: 'logType',
      dictType: DICT_TYPE.SYSTEM_LOGIN_TYPE
    },
    {
      title: '用户类型',
      field: 'userType'
    },
    {
      title: '用户名称',
      field: 'username',
      isSearch: true
    },
    {
      title: '登录地址',
      field: 'userIp',
      isSearch: true
    },
    {
      title: '浏览器',
      field: 'userAgent'
    },
    {
      title: '登陆结果',
      field: 'result',
      dictType: DICT_TYPE.SYSTEM_LOGIN_RESULT
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
