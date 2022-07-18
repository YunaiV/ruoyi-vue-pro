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
    label: '用户编号',
    field: 'userId',
    search: {
      show: true
    }
  },
  {
    label: '访问令牌',
    field: 'accessToken'
  },
  {
    label: '刷新令牌',
    field: 'refreshToken'
  },
  {
    label: '用户类型',
    field: 'userType',
    dictType: DICT_TYPE.USER_TYPE,
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
    label: '过期时间',
    field: 'expiresTime',
    form: {
      show: false
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '200px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
