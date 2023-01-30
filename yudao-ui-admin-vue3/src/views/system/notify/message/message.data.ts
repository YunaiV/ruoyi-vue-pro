import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id', // 默认的主键ID
  primaryTitle: '编号', // 默认显示的值
  primaryType: 'seq', // 默认为seq，序号模式
  action: true,
  actionWidth: '200', // 3个按钮默认200，如有删减对应增减即可
  columns: [
    {
      title: '用户编号',
      field: 'userId',
      isSearch: true
    },
    {
      title: '用户类型',
      field: 'userType',
      dictType: DICT_TYPE.USER_TYPE,
      dictClass: 'string',
      isSearch: true,
      table: {
        width: 80
      }
    },
    {
      title: '模版编号',
      field: 'templateId'
    },
    {
      title: '模板编码',
      field: 'templateCode',
      isSearch: true,
      table: {
        width: 80
      }
    },
    {
      title: '发送人名称',
      field: 'templateNickname',
      table: {
        width: 120
      }
    },
    {
      title: '模版内容',
      field: 'templateContent',
      table: {
        width: 200
      }
    },
    {
      title: '模版类型',
      field: 'templateType',
      dictType: DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE,
      dictClass: 'number',
      isSearch: true,
      table: {
        width: 80
      }
    },
    {
      title: '模版参数',
      field: 'templateParams',
      isTable: false
    },
    {
      title: '是否已读',
      field: 'readStatus',
      dictType: DICT_TYPE.INFRA_BOOLEAN_STRING,
      dictClass: 'boolean',
      table: {
        width: 80
      }
    },
    {
      title: '阅读时间',
      field: 'readTime',
      formatter: 'formatDate',
      table: {
        width: 180
      }
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
