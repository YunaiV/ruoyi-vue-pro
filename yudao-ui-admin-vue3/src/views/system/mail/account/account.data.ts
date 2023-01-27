import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// 表单校验
export const rules = reactive({
  mail: [required],
  username: [required],
  password: [required],
  host: [required],
  port: [required],
  sslEnable: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id', // 默认的主键 ID
  primaryTitle: '编号',
  primaryType: 'id',
  action: true,
  actionWidth: '200', // 3 个按钮默认 200，如有删减对应增减即可
  columns: [
    {
      title: '邮箱',
      field: 'mail',
      isSearch: true
    },
    {
      title: '用户名',
      field: 'username',
      isSearch: true
    },
    {
      title: '密码',
      field: 'password',
      isTable: false
    },
    {
      title: 'SMTP 服务器域名',
      field: 'host'
    },
    {
      title: 'SMTP 服务器端口',
      field: 'port',
      form: {
        component: 'InputNumber',
        value: 465
      }
    },
    {
      title: '是否开启 SSL',
      field: 'sslEnable',
      dictType: DICT_TYPE.INFRA_BOOLEAN_STRING,
      dictClass: 'boolean'
    },
    {
      title: '创建时间',
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
