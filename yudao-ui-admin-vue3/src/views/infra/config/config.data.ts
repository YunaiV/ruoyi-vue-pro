import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  category: [required],
  name: [required],
  key: [required],
  value: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '参数分类',
      field: 'category'
    },
    {
      title: '参数名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '参数键名',
      field: 'key',
      isSearch: true
    },
    {
      title: '参数键值',
      field: 'value'
    },
    {
      title: '系统内置',
      field: 'type',
      dictType: DICT_TYPE.INFRA_CONFIG_TYPE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '是否可见',
      field: 'visible',
      table: {
        slots: {
          default: 'visible_default'
        }
      },
      form: {
        component: 'RadioButton',
        componentProps: {
          options: [
            { label: '是', value: true },
            { label: '否', value: false }
          ]
        }
      }
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
