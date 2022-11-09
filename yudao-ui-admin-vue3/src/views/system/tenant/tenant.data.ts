import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
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
    label: '租户名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '租户套餐',
    field: 'packageId'
  },
  {
    label: '联系人',
    field: 'contactName',
    search: {
      show: true
    }
  },
  {
    label: '联系手机',
    field: 'contactMobile',
    search: {
      show: true
    }
  },
  {
    label: '用户名称',
    field: 'username',
    table: {
      show: false
    },
    detail: {
      show: false
    }
  },
  {
    label: '用户密码',
    field: 'password',
    table: {
      show: false
    },
    detail: {
      show: false
    },
    form: {
      component: 'InputPassword'
    }
  },
  {
    label: '账号额度',
    field: 'accountCount',
    form: {
      component: 'InputNumber',
      value: 0
    }
  },
  {
    label: '过期时间',
    field: 'expireTime',
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
    label: '绑定域名',
    field: 'domain'
  },
  {
    label: '租户状态',
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS,
    search: {
      show: true
    }
  },
  {
    label: t('table.createTime'),
    field: 'createTime',
    form: {
      show: false
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '240px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
