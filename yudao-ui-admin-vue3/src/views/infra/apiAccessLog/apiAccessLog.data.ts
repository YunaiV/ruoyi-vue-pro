import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '日志编号',
  action: true,
  actionWidth: '80px',
  columns: [
    {
      title: '链路追踪',
      field: 'traceId'
    },
    {
      title: '用户编号',
      field: 'userId',
      isSearch: true
    },
    {
      title: '用户类型',
      field: 'userType',
      dictType: DICT_TYPE.USER_TYPE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '应用名',
      field: 'applicationName',
      isSearch: true
    },
    {
      title: '请求方法名',
      field: 'requestMethod'
    },
    {
      title: '请求地址',
      field: 'requestUrl',
      isSearch: true
    },
    {
      title: '请求时间',
      field: 'beginTime',
      formatter: 'formatDate',
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '执行时长',
      field: 'duration',
      table: {
        slots: {
          default: 'duration_default'
        }
      }
    },
    {
      title: '操作结果',
      field: 'resultCode',
      isSearch: true,
      table: {
        slots: {
          default: 'resultCode_default'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
