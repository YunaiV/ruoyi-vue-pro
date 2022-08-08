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
    label: '任务名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '所属流程',
    field: 'processInstance.name'
  },
  {
    label: '流程发起人',
    field: 'processInstance.startUserNickname'
  },
  {
    label: '结果',
    field: 'result',
    dictType: DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT
  },
  {
    label: '审批意见',
    field: 'reason'
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'datetimerange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)],
        shortcuts: [
          {
            text: '近一周',
            value: () => {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              return [start, end]
            }
          },
          {
            text: '近一个月',
            value: () => {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              return [start, end]
            }
          },
          {
            text: '近三个月',
            value: () => {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              return [start, end]
            }
          }
        ]
      }
    }
  },
  {
    label: '审批时间',
    field: 'endTime'
  },
  {
    label: '耗时',
    field: 'durationInMillis'
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '100px'
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
