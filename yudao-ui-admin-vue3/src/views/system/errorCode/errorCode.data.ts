import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  applicationName: [required],
  code: [required],
  message: [required]
})

// 新增 + 修改
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '编号',
  action: true,
  columns: [
    {
      title: '错误码类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_ERROR_CODE_TYPE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '应用名',
      field: 'applicationName',
      isSearch: true
    },
    {
      title: '错误码编码',
      field: 'code',
      isSearch: true
    },
    {
      title: '错误码错误提示',
      field: 'message',
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
