import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  actionWidth: '80px',
  columns: [
    {
      title: '日志类型',
      field: 'logType',
      dictType: DICT_TYPE.SYSTEM_LOGIN_TYPE
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
      field: 'userAgent' // TODO 星语：调宽一点，UA 稍微多展示一点，虽然最终都会缩略
    },
    {
      title: '登陆结果',
      field: 'result',
      dictType: DICT_TYPE.SYSTEM_LOGIN_RESULT
    },
    {
      title: '登录日期', // TODO 星语：有点窄，看看咋调宽一点，避免日期展示不全
      field: 'createTime',
      formatter: 'formatDate',
      isSearch: true,
      search: {
        itemRender: {
          name: 'XDataTimePicker'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
