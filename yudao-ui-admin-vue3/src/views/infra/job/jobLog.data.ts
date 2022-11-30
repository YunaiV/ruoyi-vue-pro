import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
// 国际化
const { t } = useI18n()
// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '日志编号',
  action: true,
  columns: [
    {
      title: '任务编号',
      field: 'jobId',
      isSearch: true
    },
    {
      title: '处理器的名字',
      field: 'handlerName',
      isSearch: true
    },
    {
      title: '处理器的参数',
      field: 'handlerParam'
    },
    {
      title: '第几次执行',
      field: 'executeIndex'
    },
    {
      title: '开始执行时间',
      field: 'beginTime',
      formatter: 'formatDate',
      table: {
        slots: {
          default: 'beginTime_default'
        }
      },
      search: {
        show: true,
        itemRender: {
          name: 'XDataPicker'
        }
      }
    },
    {
      title: '结束执行时间',
      field: 'endTime',
      formatter: 'formatDate',
      isTable: false,
      search: {
        show: true,
        itemRender: {
          name: 'XDataPicker'
        }
      }
    },
    {
      title: '执行时长',
      field: 'duration',
      table: {
        slots: {
          default: 'duration_default'
        }
      }
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.INFRA_JOB_LOG_STATUS,
      dictClass: 'number',
      isSearch: true
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
