import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  tags: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '敏感词编号',
  action: true,
  columns: [
    {
      title: '敏感词',
      field: 'name',
      isSearch: true
    },
    {
      title: '标签',
      field: 'tag',
      isTable: false,
      isForm: false,
      isDetail: false,
      isSearch: true
    },
    {
      title: '标签',
      field: 'tags',
      table: {
        slots: {
          default: 'tags_default'
        }
      }
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '描述',
      field: 'description',
      form: {
        component: 'Input',
        componentProps: {
          type: 'textarea',
          rows: 4
        },
        colProps: {
          span: 24
        }
      }
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false,
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
