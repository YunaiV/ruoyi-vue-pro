import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  title: [required],
  type: [required],
  status: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  actionWidth: '400px',
  columns: [
    {
      title: '表名称',
      field: 'tableName',
      isSearch: true
    },
    {
      title: '表描述',
      field: 'tableComment',
      isSearch: true
    },
    {
      title: '实体',
      field: 'className',
      isSearch: true
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
    },
    {
      title: t('common.updateTime'),
      field: 'updateTime',
      formatter: 'formatDate',
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
