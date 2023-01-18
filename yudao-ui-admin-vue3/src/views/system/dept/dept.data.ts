import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  sort: [required],
  email: [required],
  phone: [
    {
      len: 11,
      trigger: 'blur',
      message: '请输入正确的手机号码'
    }
  ]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '上级部门',
      field: 'parentId',
      isTable: false
    },
    {
      title: '部门名称',
      field: 'name',
      isSearch: true,
      table: {
        treeNode: true,
        align: 'left'
      }
    },
    {
      title: '负责人',
      field: 'leaderUserId',
      table: {
        slots: {
          default: 'leaderUserId_default'
        }
      }
    },
    {
      title: '联系电话',
      field: 'phone'
    },
    {
      title: '邮箱',
      field: 'email',
      isTable: false
    },
    {
      title: '显示排序',
      field: 'sort'
    },
    {
      title: t('common.status'),
      field: 'status',
      dictType: DICT_TYPE.COMMON_STATUS,
      dictClass: 'number',
      isSearch: true
    },
    {
      title: t('common.createTime'),
      field: 'createTime',
      formatter: 'formatDate',
      isForm: false
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
