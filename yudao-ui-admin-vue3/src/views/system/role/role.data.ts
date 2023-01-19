import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  name: [required],
  code: [required],
  sort: [required]
})
// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryTitle: '角色编号',
  primaryType: 'seq',
  action: true,
  actionWidth: '400px',
  columns: [
    {
      title: '角色名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '角色类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_ROLE_TYPE,
      dictClass: 'number',
      isForm: false
    },
    {
      title: '角色标识',
      field: 'code',
      isSearch: true
    },
    {
      title: '显示顺序',
      field: 'sort'
    },
    {
      title: t('form.remark'),
      field: 'remark',
      isTable: false,
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
