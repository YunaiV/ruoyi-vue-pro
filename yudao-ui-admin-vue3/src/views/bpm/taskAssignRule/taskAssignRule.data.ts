import { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  actionWidth: '200px',
  columns: [
    {
      title: '任务名',
      field: 'taskDefinitionName'
    },
    {
      title: '任务标识',
      field: 'taskDefinitionKey'
    },
    {
      title: '规则类型',
      field: 'category',
      dictType: DICT_TYPE.BPM_TASK_ASSIGN_RULE_TYPE,
      dictClass: 'number'
    },
    {
      title: '规则范围',
      field: 'options',
      table: {
        slots: {
          default: 'options_default'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
