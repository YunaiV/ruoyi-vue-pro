import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  type: [required],
  status: [required],
  code: [required],
  name: [required],
  content: [required],
  apiTemplateId: [required],
  channelId: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  action: true,
  columns: [
    {
      label: '模板编码',
      field: 'code',
      isSearch: true
    },
    {
      label: '模板名称',
      field: 'name',
      isSearch: true
    },
    {
      label: '模板内容',
      field: 'content'
    },
    {
      label: '短信 API 的模板编号',
      field: 'apiTemplateId',
      isSearch: true
    },
    {
      label: '短信类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE
    },
    {
      label: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS
    },
    {
      label: t('form.remark'),
      field: 'remark',
      isTable: false
    },
    {
      title: t('common.createTime'),
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
