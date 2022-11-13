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
      dictType: DICT_TYPE.SYSTEM_LOGIN_TYPE,
      isSearch: true
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
      title: 'userAgent',
      field: 'userAgent'
    },
    {
      title: '登陆结果',
      field: 'result',
      dictType: DICT_TYPE.SYSTEM_LOGIN_RESULT,
      isSearch: true
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
