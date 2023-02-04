import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryTitle: '编号',
  primaryType: 'id',
  action: true,
  actionWidth: '70',
  columns: [
    {
      title: '发送时间',
      field: 'sendTime',
      table: {
        width: 180
      },
      formatter: 'formatDate',
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '接收邮箱',
      field: 'toMail',
      isSearch: true,
      table: {
        width: 180,
        slots: {
          default: 'toMail_default'
        }
      }
    },
    {
      title: '用户编号',
      field: 'userId',
      isSearch: true,
      isTable: false
    },
    {
      title: '用户类型',
      field: 'userType',
      dictType: DICT_TYPE.USER_TYPE,
      dictClass: 'number',
      isSearch: true,
      isTable: false
    },
    {
      title: '邮件标题',
      field: 'templateTitle'
    },
    {
      title: '邮件内容',
      field: 'templateContent',
      isTable: false
    },
    {
      title: '邮箱参数',
      field: 'templateParams',
      isTable: false
    },
    {
      title: '发送状态',
      field: 'sendStatus',
      dictType: DICT_TYPE.SYSTEM_MAIL_SEND_STATUS,
      dictClass: 'string',
      isSearch: true
    },
    {
      title: '邮箱账号',
      field: 'accountId',
      isSearch: true,
      isTable: false,
      search: {
        slots: {
          default: 'accountId_search'
        }
      }
    },
    {
      title: '发送邮箱地址',
      field: 'fromMail',
      table: {
        title: '邮箱账号'
      }
    },
    {
      title: '模板编号',
      field: 'templateId',
      isSearch: true
    },
    {
      title: '模板编码',
      field: 'templateCode',
      isTable: false
    },
    {
      title: '模版发送人名称',
      field: 'templateNickname',
      isTable: false
    },
    {
      title: '发送返回的消息编号',
      field: 'sendMessageId',
      isTable: false
    },
    {
      title: '发送异常',
      field: 'sendException',
      isTable: false
    },
    {
      title: '创建时间',
      field: 'createTime',
      isTable: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
