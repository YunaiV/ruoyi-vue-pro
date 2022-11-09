import { reactive } from 'vue'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  id: [required],
  type: [required],
  remark: [required],
  status: [required],
  menuIds: [required]
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
    label: '套餐名称',
    field: 'name',
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
    }
  },
  {
    label: '菜单权限',
    field: 'menuIds',
    table: {
      show: false
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
    label: '创建时间',
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
