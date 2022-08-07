import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  storage: [required],
  config: {
    basePath: [required],
    host: [required],
    port: [required],
    username: [required],
    password: [required],
    mode: [required],
    endpoint: [required],
    bucket: [required],
    accessKey: [required],
    accessSecret: [required],
    domain: [required]
  }
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
    label: '配置名',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '存储器',
    field: 'storage',
    dictType: DICT_TYPE.INFRA_FILE_STORAGE,
    search: {
      show: true
    }
  },
  {
    label: '主配置',
    field: 'primary',
    dictType: DICT_TYPE.INFRA_BOOLEAN_STRING
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
    }
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
    form: {
      show: false
    },
    search: {
      show: true,
      component: 'DatePicker',
      componentProps: {
        type: 'datetimerange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]
      }
    }
  },
  {
    label: t('table.action'),
    field: 'action',
    width: '400px',
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
