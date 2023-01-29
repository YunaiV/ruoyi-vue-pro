import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// 表单校验
export const rules = reactive({
  name: [required],
  code: [required],
  content: [required],
  type: [required],
  status: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryTitle: '编号',
  primaryType: null,
  action: true,
  actionWidth: '260', // 3个按钮默认200，如有删减对应增减即可
  columns: [
    {
      title: '模版编码',
      field: 'code',
      isSearch: true
    },
    {
      title: '模板名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '发件人名称',
      field: 'nickname'
    },
    {
      title: '类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
      dictClass: 'number'
    },
    {
      title: '模版内容',
      field: 'content',
      table: {
        width: 300
      },
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
      title: '状态',
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '备注',
      field: 'remark'
    },
    {
      title: '创建时间',
      field: 'createTime',
      isForm: false,
      formatter: 'formatDate',
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
