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
  primaryTitle: '模板编号',
  action: true,
  actionWidth: '280',
  columns: [
    {
      title: '模板编码',
      field: 'code',
      isSearch: true
    },
    {
      title: '模板名称',
      field: 'name',
      isSearch: true
    },
    {
      title: '模板内容',
      field: 'content'
    },
    {
      title: '短信 API 的模板编号',
      field: 'apiTemplateId',
      isSearch: true
    },
    {
      title: '短信类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE,
      dictClass: 'number',
      isSearch: true,
      table: {
        width: 80
      }
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true,
      table: {
        width: 80
      }
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
