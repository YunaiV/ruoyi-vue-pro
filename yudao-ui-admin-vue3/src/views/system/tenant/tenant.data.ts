import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  packageId: [required],
  contactName: [required],
  contactMobile: [required],
  accountCount: [required],
  expireTime: [required],
  domain: [required],
  status: [required]
})

// CrudSchema.
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  columns: [
    {
      title: '租户名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '租户套餐',
      field: 'packageId'
    },
    {
      title: '联系人',
      field: 'contactName',
      isSearch: true
    },
    {
      title: '联系手机',
      field: 'contactMobile',
      isSearch: true
    },
    {
      title: '用户名称',
      field: 'username',
      isTable: false,
      isDetail: false
    },
    {
      title: '用户密码',
      field: 'password',
      isTable: false,
      isDetail: false,
      form: {
        component: 'InputPassword'
      }
    },
    {
      title: '账号额度',
      field: 'accountCount',
      form: {
        component: 'InputNumber'
      }
    },
    {
      title: '过期时间',
      field: 'expireTime',
      formatter: 'formatDate',
      form: {
        show: true,
        component: 'DatePicker',
        componentProps: {
          type: 'datetime',
          valueFormat: 'x'
        }
      }
    },
    {
      title: '绑定域名',
      field: 'domain'
    },
    {
      title: '租户状态',
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: t('table.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
