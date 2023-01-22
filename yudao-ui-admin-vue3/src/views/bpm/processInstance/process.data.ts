import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  primaryTitle: '编号',
  action: true,
  actionWidth: '200px',
  columns: [
    {
      title: '编号',
      field: 'id',
      table: {
        width: 320
      }
    },
    {
      title: '流程名',
      field: 'name',
      isSearch: true
    },
    {
      title: '所属流程',
      field: 'processDefinitionId',
      isSearch: true,
      isTable: false
    },
    {
      title: '流程分类',
      field: 'category',
      dictType: DICT_TYPE.BPM_MODEL_CATEGORY,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '当前审批任务',
      field: 'tasks',
      table: {
        width: 140,
        slots: {
          default: 'tasks_default'
        }
      }
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '结果',
      field: 'result',
      dictType: DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '提交时间',
      field: 'createTime',
      formatter: 'formatDate',
      table: {
        width: 180
      },
      isForm: false,
      isSearch: true,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '结束时间',
      field: 'endTime',
      formatter: 'formatDate',
      table: {
        width: 180
      },
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
