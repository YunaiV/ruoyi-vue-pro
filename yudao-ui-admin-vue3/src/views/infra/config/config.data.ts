import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  category: [required],
  name: [required],
  key: [required],
  value: [required]
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
    label: '参数分类',
    field: 'category'
  },
  {
    label: '参数名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '参数键名',
    field: 'key',
    search: {
      show: true
    }
  },
  {
    label: '参数键值',
    field: 'value'
  },
  {
    label: '系统内置',
    field: 'type',
    dictType: DICT_TYPE.INFRA_CONFIG_TYPE,
    search: {
      show: true
    }
  },
  {
    label: '是否可见',
    field: 'visible',
    form: {
      component: 'RadioButton',
      componentProps: {
        options: [
          { label: '是', value: true },
          { label: '否', value: false }
        ]
      }
    }
  },
  {
    label: t('form.remark'),
    field: 'remark',
    form: {
      component: 'Input',
      componentProps: {
        type: 'textarea',
        rows: 4
      },
      colProps: {
        span: 24
      }
    },
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
