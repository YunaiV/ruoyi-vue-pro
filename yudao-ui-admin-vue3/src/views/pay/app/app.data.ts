import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  status: [required],
  payNotifyUrl: [required],
  refundNotifyUrl: [required],
  merchantId: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '编号',
  action: true,
  columns: [
    {
      title: '应用名',
      field: 'name',
      isSearch: true
    },
    {
      title: '商户名称',
      field: 'payMerchant',
      isSearch: true
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '支付结果的回调地址',
      field: 'payNotifyUrl',
      isSearch: true
    },
    {
      title: '退款结果的回调地址',
      field: 'refundNotifyUrl',
      isSearch: true
    },
    {
      title: '商户名称',
      field: 'merchantName',
      isSearch: true
    },
    {
      title: '备注',
      field: 'remark',
      isTable: false,
      isSearch: true
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
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
