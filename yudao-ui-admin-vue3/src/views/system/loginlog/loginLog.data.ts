import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '日志编号',
  action: true,
  actionWidth: '100px',
  columns: [
    {
      title: '日志类型',
      field: 'logType',
      dictType: DICT_TYPE.SYSTEM_LOGIN_TYPE,
      dictClass: 'number'
    },
    {
      title: '用户名称',
      field: 'username',
      isSearch: true
    },
    {
      title: '登录地址',
      field: 'userIp',
      isSearch: true
    },
    {
      title: '浏览器',
      field: 'userAgent'
    },
    {
      title: '登陆结果',
      field: 'result',
      dictType: DICT_TYPE.SYSTEM_LOGIN_RESULT,
      dictClass: 'number'
    },
    {
      title: '登录日期',
      field: 'createTime',
      formatter: 'formatDate',
      table: {
        width: 150
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
