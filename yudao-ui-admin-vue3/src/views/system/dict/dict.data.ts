import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const dictDataRules = reactive({
  label: [required],
  value: [required],
  sort: [required]
})
// crudSchemas
export const crudSchemas = reactive<CrudSchema[]>([
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
    label: '字典类型',
    field: 'dictType',
    table: {
      show: false
    },
    form: {
      show: false
    }
  },
  {
    label: '数据标签',
    field: 'label',
    search: {
      show: true
    }
  },
  {
    label: '数据键值',
    field: 'value'
  },
  {
    label: '颜色类型',
    field: 'colorType',
    form: {
      component: 'Select',
      componentProps: {
        options: [
          {
            label: 'default',
            value: ''
          },
          {
            label: 'success',
            value: 'success'
          },
          {
            label: 'info',
            value: 'info'
          },
          {
            label: 'warning',
            value: 'warning'
          },
          {
            label: 'danger',
            value: 'danger'
          }
        ]
      }
    },
    table: {
      show: false
    }
  },
  {
    label: 'CSS Class',
    field: 'cssClass',
    table: {
      show: false
    }
  },
  {
    label: '显示排序',
    field: 'sort',
    table: {
      show: false
    }
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS
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
    label: t('table.action'),
    field: 'action',
    width: '180px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])

export const { allSchemas } = useCrudSchemas(crudSchemas)
