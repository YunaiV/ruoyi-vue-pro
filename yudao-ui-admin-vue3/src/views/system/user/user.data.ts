import { reactive } from 'vue'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { DICT_TYPE } from '@/utils/dict'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  nickname: [required],
  status: [required]
})
// crudSchemas
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
    label: '用户账号',
    field: 'username',
    form: {
      show: false
    },
    search: {
      show: true
    }
  },
  {
    label: '用户昵称',
    field: 'nickname'
  },
  {
    label: '用户邮箱',
    field: 'email'
  },
  {
    label: '手机号码',
    field: 'mobile',
    search: {
      show: true
    }
  },
  {
    label: '用户性别',
    field: 'sex',
    dictType: DICT_TYPE.SYSTEM_USER_SEX
  },
  {
    label: '部门',
    field: 'deptId',
    table: {
      show: false
    }
  },
  {
    label: '岗位',
    field: 'postIds',
    table: {
      show: false
    }
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS,
    search: {
      show: true
    }
  },
  {
    label: '最后登录时间',
    field: 'loginDate',
    form: {
      show: false
    }
  },
  {
    label: '最后登录IP',
    field: 'loginIp',
    table: {
      show: false
    },
    form: {
      show: false
    }
  },
  {
    label: t('form.remark'),
    field: 'remark',
    table: {
      show: false
    }
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    table: {
      show: false
    },
    form: {
      show: false
    },
    detail: {
      show: false
    },
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'datetimerange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)],
        shortcuts: [
          {
            text: '近一周',
            value: () => {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
              return [start, end]
            }
          },
          {
            text: '近一个月',
            value: () => {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
              return [start, end]
            }
          },
          {
            text: '近三个月',
            value: () => {
              const end = new Date()
              const start = new Date()
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
              return [start, end]
            }
          }
        ]
      }
    }
  },
  {
    field: 'action',
    width: '400px',
    label: t('table.action'),
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
