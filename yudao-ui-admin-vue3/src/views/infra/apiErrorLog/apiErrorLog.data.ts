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
    label: '异常发生时间',
    field: 'exceptionTime',
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
    label: '异常名',
    field: 'exceptionName'
  },
  {
    label: '处理状态',
    field: 'processStatus',
    dictType: DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '处理人',
    field: 'processUserId'
  },
  {
    label: '处理时间',
    field: 'processTime'
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
