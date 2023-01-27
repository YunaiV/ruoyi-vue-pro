import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// 表单校验
export const rules = reactive({
  name: [required],
  code: [required],
  accountId: [required],
  title: [required],
  content: [required],
  params: [required],
  status: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id', // 默认的主键ID
  primaryTitle: '编号', // 默认显示的值
  primaryType: null,
  action: true,
  actionWidth: '260',
  columns: [
    {
      title: '模板编码',
      field: 'code',
      isSearch: true
    },
    {
      title: '模板名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '模板标题',
      field: 'title'
    },
    {
      title: '模板内容',
      field: 'content',
      form: {
        component: 'Editor',
        colProps: {
          span: 24
        },
        componentProps: {
          valueHtml: ''
        }
      }
    },
    {
      title: '邮箱账号',
      field: 'accountId',
      isSearch: true,
      table: {
        width: 200,
        slots: {
          default: 'accountId_default'
        }
      },
      search: {
        slots: {
          default: 'accountId_search'
        }
      }
    },
    {
      title: '发送人名称',
      field: 'nickname'
    },
    {
      title: '开启状态',
      field: 'status',
      isSearch: true,
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number'
    },
    {
      title: '备注',
      field: 'remark',
      isTable: false
    },
    {
      title: '创建时间',
      field: 'createTime',
      isForm: false,
      formatter: 'formatDate',
      table: {
        width: 180
      },
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
