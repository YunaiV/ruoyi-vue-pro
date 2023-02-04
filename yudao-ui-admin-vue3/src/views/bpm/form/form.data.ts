import { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '表单编号',
  action: true,
  columns: [
    {
      title: '表单名',
      field: 'name',
      isSearch: true
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number'
    },
    {
      title: '备注',
      field: 'remark'
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false,
      table: {
        width: 180
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
