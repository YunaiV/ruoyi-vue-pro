import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  key: [required],
  name: [required],
  category: [required],
  formType: [required],
  formId: [required],
  formCustomCreatePath: [required],
  formCustomViewPath: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'key',
  primaryType: null,
  action: true,
  actionWidth: '540px',
  columns: [
    {
      title: '流程标识',
      field: 'key',
      isSearch: true,
      table: {
        width: 120
      }
    },
    {
      title: '流程名称',
      field: 'name',
      isSearch: true,
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
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '表单信息',
      field: 'formId',
      table: {
        width: 180,
        slots: {
          default: 'formId_default'
        }
      }
    },
    {
      title: '最新部署的流程定义',
      field: 'processDefinition',
      isForm: false,
      table: {
        children: [
          {
            title: '流程版本',
            field: 'version',
            slots: {
              default: 'version_default'
            },
            width: 80
          },
          {
            title: '激活状态',
            field: 'status',
            slots: {
              default: 'status_default'
            },
            width: 80
          },
          {
            title: '部署时间',
            field: 'processDefinition.deploymentTime',
            formatter: 'formatDate',
            width: 180
          }
        ]
      }
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      isForm: false,
      formatter: 'formatDate',
      table: {
        width: 180
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
