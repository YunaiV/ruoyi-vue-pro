import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  startTime: [{ required: true, message: '开始时间不能为空', trigger: 'blur' }],
  endTime: [{ required: true, message: '结束时间不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '请假类型不能为空', trigger: 'change' }],
  reason: [{ required: true, message: '请假原因不能为空', trigger: 'change' }]
})

// crudSchemas
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'id',
  primaryTitle: '申请编号',
  action: true,
  actionWidth: '260',
  columns: [
    {
      title: t('common.status'),
      field: 'result',
      dictType: DICT_TYPE.BPM_PROCESS_INSTANCE_RESULT,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: t('common.startTimeText'),
      field: 'startTime',
      formatter: 'formatDate',
      table: {
        width: 180
      }
    },
    {
      title: t('common.endTimeText'),
      field: 'endTime',
      formatter: 'formatDate',
      table: {
        width: 180
      }
    },
    {
      title: '请假类型',
      field: 'result',
      dictType: DICT_TYPE.BPM_OA_LEAVE_TYPE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '原因',
      field: 'reason',
      isSearch: true
    },
    {
      title: '申请时间',
      field: 'createTime',
      formatter: 'formatDate',
      table: {
        width: 180
      },
      isSearch: true,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
