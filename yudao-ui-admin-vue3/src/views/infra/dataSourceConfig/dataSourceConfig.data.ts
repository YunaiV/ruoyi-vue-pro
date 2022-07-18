import { reactive } from 'vue'
import { required } from '@/utils/formRules'
import { useI18n } from '@/hooks/web/useI18n'
import { CrudSchema, useCrudSchemas } from '@/hooks/web/useCrudSchemas'
// 国际化
const { t } = useI18n()
// 表单校验
export const rules = reactive({
  name: [required],
  url: [required],
  username: [required],
  password: [required]
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
    label: '数据源名称',
    field: 'name'
  },
  {
    label: '数据源连接',
    field: 'url',
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
    label: '用户名',
    field: 'username'
  },
  {
    label: '密码',
    field: 'password',
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
