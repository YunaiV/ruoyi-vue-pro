import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  actionWidth: '80px',
  columns: [
    {
      title: '操作模块',
      field: 'module',
      isSearch: true
    },
    {
      title: '操作名',
      field: 'name'
    },
    {
      title: '操作类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_OPERATE_TYPE,
      isSearch: true
    },
    {
      title: '请求方法名',
      field: 'requestMethod'
    },
    {
      title: '请求地址',
      field: 'requestUrl'
    },
    {
      title: '操作人员',
      field: 'userNickname',
      isSearch: true
    },
    {
      title: '操作明细',
      field: 'content',
      isTable: false
    },
    {
      title: '用户 IP',
      field: 'userIp',
      isTable: false
    },
    {
      title: 'userAgent',
      field: 'userAgent'
    },
    {
      title: '操作结果',
      field: 'resultCode',
      table: {
        slots: {
          default: 'resultCode'
        }
      }
    },
    {
      title: '操作日期',
      field: 'startTime',
      formatter: 'formatDate',
      isForm: false,
      search: {
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
          default: 'duration'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
