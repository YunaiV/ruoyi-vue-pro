import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  username: [required],
  nickname: [required],
  email: [required],
  status: [required],
  mobile: [
    {
      len: 11,
      trigger: 'blur',
      message: '请输入正确的手机号码'
    }
  ]
})
// crudSchemas
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '用户编号',
  action: true,
  actionWidth: '200px',
  columns: [
    {
      title: '用户账号',
      field: 'username',
      isSearch: true
    },
    {
      title: '用户密码',
      field: 'password',
      isDetail: false,
      isTable: false,
      form: {
        component: 'InputPassword'
      }
    },
    {
      title: '用户昵称',
      field: 'nickname'
    },
    {
      title: '用户邮箱',
      field: 'email'
    },
    {
      title: '手机号码',
      field: 'mobile',
      isSearch: true
    },
    {
      title: '部门',
      field: 'deptId',
      isTable: false
    },
    {
      title: '岗位',
      field: 'postIds',
      isTable: false
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true,
      table: {
        slots: {
          default: 'status_default'
        }
      }
    },
    {
      title: '最后登录时间',
      field: 'loginDate',
      formatter: 'formatDate',
      isForm: false
    },
    {
      title: '最后登录IP',
      field: 'loginIp',
      isTable: false,
      isForm: false
    },
    {
      title: t('form.remark'),
      field: 'remark',
      isTable: false
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isTable: false,
      isForm: false,
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
