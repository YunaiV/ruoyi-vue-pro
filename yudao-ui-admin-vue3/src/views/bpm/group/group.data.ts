import { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  description: [required],
  memberUserIds: [required],
  status: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'id',
  primaryTitle: '编号',
  action: true,
  columns: [
    {
      title: '组名',
      field: 'name',
      isSearch: true
    },
    {
      title: '成员',
      field: 'memberUserIds',
      table: {
        slots: {
          default: 'memberUserIds_default'
        }
      }
    },
    {
      title: '描述',
      field: 'description'
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false,
      isSearch: true,
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      },
      table: {
        width: 180
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
