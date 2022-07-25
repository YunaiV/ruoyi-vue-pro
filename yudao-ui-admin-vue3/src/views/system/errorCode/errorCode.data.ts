import { reactive } from 'vue'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { DICT_TYPE } from '@/utils/dict'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  applicationName: [required],
  code: [required],
  message: [required]
})
// 新增 + 修改
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
    label: '错误码类型',
    field: 'type',
    component: 'InputNumber',
    dictType: DICT_TYPE.SYSTEM_ERROR_CODE_TYPE,
    search: {
      show: true
    }
  },
  {
    label: '应用名',
    field: 'applicationName',
    search: {
      show: true
    }
  },
  {
    label: '错误码编码',
    field: 'code',
    search: {
      show: true
    }
  },
  {
    label: '错误码错误提示',
    field: 'message'
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
        type: 'daterange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]
      }
    }
  },
  {
    field: 'action',
    width: '240px',
    label: t('table.action'),
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useCrudSchemas(crudSchemas)
