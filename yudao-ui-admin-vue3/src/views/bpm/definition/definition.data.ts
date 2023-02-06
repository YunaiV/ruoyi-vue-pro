import { reactive } from 'vue'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

import { DICT_TYPE } from '@/utils/dict'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '定义编号',
      field: 'id',
      table: {
        width: 360
      }
    },
    {
      title: '定义名称',
      field: 'name',
      table: {
        width: 120,
        slots: {
          default: 'name_default'
        }
      }
    },
    {
      title: '流程分类',
      field: 'category',
      dictType: DICT_TYPE.BPM_MODEL_CATEGORY,
      dictClass: 'number'
    },
    {
      title: '表单信息',
      field: 'formId',
      table: {
        width: 120,
        slots: {
          default: 'formId_default'
        }
      }
    },
    {
      title: '流程版本',
      field: 'version',
      table: {
        width: 80,
        slots: {
          default: 'version_default'
        }
      }
    },
    {
      title: '激活状态',
      field: 'suspensionState',
      table: {
        width: 80,
        slots: {
          default: 'suspensionState_default'
        }
      }
    },
    {
      title: '部署时间',
      field: 'deploymentTime',
      isForm: false,
      formatter: 'formatDate',
      table: {
        width: 180
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
