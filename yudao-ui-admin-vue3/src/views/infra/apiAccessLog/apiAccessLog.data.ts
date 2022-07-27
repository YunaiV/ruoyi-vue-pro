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
    type: 'index'
  },
  {
    label: '链路追踪',
    field: 'traceId'
  },
  {
    label: '用户编号',
    field: 'userId',
    search: {
      show: true
    }
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
    label: '应用名',
    field: 'applicationName',
    search: {
      show: true
    }
  },
  {
    label: '请求方法名',
    field: 'requestMethod'
  },
  {
    label: '请求地址',
    field: 'requestUrl',
    search: {
      show: true
    }
  },
  {
    label: '请求时间',
    field: 'beginTime'
  },
  {
    label: '执行时长',
    field: 'duration'
  },
  {
    label: '操作结果',
    field: 'resultCode',
    search: {
      show: true
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '300px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
