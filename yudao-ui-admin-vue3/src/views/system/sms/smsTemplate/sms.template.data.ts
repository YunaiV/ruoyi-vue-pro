import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  type: [required],
  name: [required],
  content: [required],
  apiTemplateId: [required],
  channelId: [required],
  code: [required],
  sort: [required],
  status: [required]
})

// CrudSchema
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
    label: '模板编码',
    field: 'code',
    search: {
      show: true
    }
  },
  {
    label: '模板名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '模板内容',
    field: 'content'
  },
  {
    label: '短信 API 的模板编号',
    field: 'apiTemplateId',
    search: {
      show: true
    }
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
    table: {
      show: false
    }
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    form: {
      show: false
    }
  },
  {
    label: t('common.createTime'),
    field: 'daterange',
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
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD'
      }
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '320px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
