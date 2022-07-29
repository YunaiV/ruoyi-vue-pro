import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  name: [required],
  handlerName: [required],
  cronExpression: [required],
  retryCount: [required],
  retryInterval: [required]
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
    label: '任务名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.INFRA_JOB_STATUS,
    form: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: '处理器的名字',
    field: 'handlerName',
    search: {
      show: true
    }
  },
  {
    label: '处理器的参数',
    field: 'handlerParam',
    table: {
      show: false
    }
  },
  {
    label: 'CRON 表达式',
    field: 'cronExpression'
  },
  {
    label: '重试次数',
    field: 'retryCount',
    table: {
      show: false
    }
  },
  {
    label: '重试间隔',
    field: 'retryInterval',
    table: {
      show: false
    }
  },
  {
    label: '监控超时时间',
    field: 'monitorTimeout',
    table: {
      show: false
    }
  },
  {
    field: 'action',
    width: '500px',
    label: t('table.action'),
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])

export const { allSchemas } = useCrudSchemas(crudSchemas)
