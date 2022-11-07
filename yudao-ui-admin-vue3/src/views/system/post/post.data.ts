import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
import { DICT_TYPE } from '@/utils/dict'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  code: [required],
  sort: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema[]>([
  {
    title: t('common.index'),
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
    title: '岗位名称',
    field: 'name',
    search: {
      show: true
    }
  },
  {
    title: '岗位编码',
    field: 'code',
    search: {
      show: true
    }
  },
  {
    title: '岗位顺序',
    field: 'sort'
  },
  {
    title: t('common.status'),
    field: 'status',
    dictType: DICT_TYPE.COMMON_STATUS,
    table: {
      slots: {
        default: 'status_default'
      }
    },
    search: {
      show: true
    }
  },
  {
    title: '备注',
    field: 'remark',
    table: {
      show: false
    }
  },
  {
    title: t('common.createTime'),
    field: 'createTime',
    formatter: 'formatDate',
    form: {
      show: false
    }
  },
  {
    title: t('table.action'),
    field: 'action',
    table: {
      width: '240px',
      slots: {
        default: 'action_default'
      }
    },
    form: {
      show: false
    },
    detail: {
      show: false
    }
  }
])
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
