import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化
// 表单校验
export const rules = reactive({
  name: [required],
  handlerName: [required],
  cronExpression: [required],
  retryCount: [required],
  retryInterval: [required]
})
// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '任务编号',
  action: true,
  actionWidth: '280px',
  columns: [
    {
      title: '任务名称',
      field: 'name',
      isSearch: true
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.INFRA_JOB_STATUS,
      dictClass: 'number',
      isForm: false,
      isSearch: true
    },
    {
      title: '处理器的名字',
      field: 'handlerName',
      isSearch: true
    },
    {
      title: '处理器的参数',
      field: 'handlerParam',
      isTable: false
    },
    {
      title: 'CRON 表达式',
      field: 'cronExpression'
    },
    {
      title: '后续执行时间',
      field: 'nextTimes',
      isTable: false,
      isForm: false
    },
    {
      title: '重试次数',
      field: 'retryCount',
      isTable: false
    },
    {
      title: '重试间隔',
      field: 'retryInterval',
      isTable: false
    },
    {
      title: '监控超时时间',
      field: 'monitorTimeout',
      isTable: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
