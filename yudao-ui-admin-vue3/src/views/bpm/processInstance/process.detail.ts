import { reactive } from 'vue'
// import { useI18n } from '@/hooks/web/useI18n'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
// const { t } = useI18n() // 国际化

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'id',
  primaryTitle: '流程名称',
  action: true,
  actionWidth: '200px',
  columns: [
    {
      title: '流程分类',
      field: 'category',
      dictType: DICT_TYPE.BPM_MODEL_CATEGORY,
      dictClass: 'number'
    },
    {
      title: '流程版本',
      field: 'processDefinition.version',
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
