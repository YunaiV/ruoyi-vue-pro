import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '日志编号',
  action: true,
  actionWidth: '300',
  columns: [
    {
      title: '链路追踪',
      field: 'traceId',
      isTable: false
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
      title: '异常发生时间',
      field: 'exceptionTime',
      formatter: 'formatDate',
      search: {
        show: true,
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    },
    {
      title: '异常名',
      field: 'exceptionName'
    },
    {
      title: '处理状态',
      field: 'processStatus',
      dictType: DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '处理人',
      field: 'processUserId',
      isTable: false
    },
    {
      title: '处理时间',
      field: 'processTime',
      formatter: 'formatDate',
      isTable: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
