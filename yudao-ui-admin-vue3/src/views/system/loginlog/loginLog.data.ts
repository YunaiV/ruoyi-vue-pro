import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
const { t } = useI18n() // 国际化
const crudSchemas = reactive<CrudSchema[]>([
  {
    label: t('common.index'),
    field: 'id',
    type: 'index',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: '日志类型',
    field: 'logType',
    dictType: DICT_TYPE.SYSTEM_LOGIN_TYPE,
    search: {
      show: true
    }
  },
  {
    label: '用户名称',
    field: 'username',
    search: {
      show: true
    }
  },
  {
    label: '登录地址',
    field: 'userIp',
    search: {
      show: true
    }
  },
  {
    label: 'userAgent',
    field: 'userAgent'
  },
  {
    label: '登陆结果',
    field: 'result',
    dictType: DICT_TYPE.SYSTEM_LOGIN_RESULT,
    search: {
      show: true
    }
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    form: {
      show: false
    },
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]
      }
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '120px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
