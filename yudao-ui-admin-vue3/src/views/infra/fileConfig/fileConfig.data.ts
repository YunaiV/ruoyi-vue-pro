import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
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
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: 'seq',
  primaryTitle: '配置编号',
  action: true,
  actionWidth: '400px',
  columns: [
    {
      title: '配置名',
      field: 'name',
      isSearch: true
    },
    {
      title: '存储器',
      field: 'storage',
      dictType: DICT_TYPE.INFRA_FILE_STORAGE,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: '主配置',
      field: 'master',
      dictType: DICT_TYPE.INFRA_BOOLEAN_STRING,
      dictClass: 'boolean'
    },
    {
      title: t('form.remark'),
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
