import { reactive } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const dictTypeRules = reactive({
  name: [required],
  type: [required]
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
    label: '字典名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    label: '字典类型',
    field: 'type',
    search: {
      show: true
    }
  },
  {
    label: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS
  },
  {
    label: t('common.createTime'),
    field: 'createTime',
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
        type: 'datetimerange',
        valueFormat: 'YYYY-MM-DD HH:mm:ss',
        defaultTime: [new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 2, 1, 23, 59, 59)]
      }
    }
  },
  {
    label: t('form.remark'),
    field: 'remark',
    table: {
      show: false
    },
    form: {
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
    field: 'action',
    width: '180px',
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
