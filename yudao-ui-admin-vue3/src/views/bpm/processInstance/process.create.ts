import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

// crudSchemas
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '流程名称',
      field: 'name'
    },
    {
      title: '流程分类',
      field: 'category',
      dictType: DICT_TYPE.BPM_MODEL_CATEGORY,
      dictClass: 'number'
    },
    {
      title: '流程版本',
      field: 'version',
      table: {
        slots: {
          default: 'version_default'
        }
      }
    },
    {
      title: '流程描述',
      field: 'description'
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
