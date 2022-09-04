import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  title: [required],
  type: [required]
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
    label: '公告标题',
    field: 'title',
    search: {
      show: true
    }
  },
  {
    label: '公告类型',
    field: 'type',
    dictType: DICT_TYPE.SYSTEM_NOTICE_TYPE,
    search: {
      show: true
    }
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS,
    search: {
      show: true
    },
    form: {
      component: 'RadioButton'
    }
  },
  {
    label: '公告内容',
    field: 'content',
    form: {
      component: 'Editor',
      colProps: {
        span: 24
      },
      componentProps: {
        valueHtml: ''
      }
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
