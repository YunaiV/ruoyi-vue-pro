import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
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
    label: '任务编号',
    field: 'jobId',
    search: {
      show: true
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
    field: 'handlerParam'
  },
  {
    label: '第几次执行',
    field: 'executeIndex'
  },
  {
    label: '开始执行时间',
    field: 'beginTime',
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'date',
        valueFormat: 'YYYY-MM-DD HH:mm:ss'
      }
    }
  },
  {
    label: '结束执行时间',
    field: 'endTime',
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'date',
        valueFormat: 'YYYY-MM-DD HH:mm:ss'
      }
    },
    table: {
      show: false
    }
  },
  {
    label: '执行时长',
    field: 'duration'
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.INFRA_JOB_LOG_STATUS,
    search: {
      show: true
    }
  },
  {
    field: 'action',
    width: '100px',
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
