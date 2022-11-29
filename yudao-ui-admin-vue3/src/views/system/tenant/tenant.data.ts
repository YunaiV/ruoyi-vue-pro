import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
import { getTenantPackageList, TenantPackageVO } from '@/api/system/tenantPackage'
import { ComponentOptions } from '@/types/components'
const { t } = useI18n() // 国际化
export const tenantPackageOption: ComponentOptions[] = []
const getTenantPackageOptions = async () => {
  const res = await getTenantPackageList()
  res.forEach((tenantPackage: TenantPackageVO) => {
    tenantPackageOption.push({
      key: tenantPackage.id,
      value: tenantPackage.id,
      label: tenantPackage.name
    })
  })

  return tenantPackageOption
}
getTenantPackageOptions()

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
  primaryTitle: '租户编号',
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
      field: 'packageId',
      table: {
        slots: {
          default: 'packageId_default'
        }
      },
      form: {
        component: 'Select',
        componentProps: {
          options: tenantPackageOption
        }
      }
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
      table: {
        slots: {
          default: 'accountCount_default'
        }
      },
      form: {
        component: 'InputNumber'
      }
    },
    {
      title: '过期时间',
      field: 'expireTime',
      formatter: 'formatDate',
      form: {
        component: 'DatePicker',
        componentProps: {
          type: 'datetime'
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
