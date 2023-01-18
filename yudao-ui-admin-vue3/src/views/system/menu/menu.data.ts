import type { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 新增和修改的表单校验
export const rules = reactive({
  name: [required],
  sort: [required],
  path: [required],
  status: [required]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  columns: [
    {
      title: '上级菜单',
      field: 'parentId',
      isTable: false
    },
    {
      title: '菜单名称',
      field: 'name',
      isSearch: true,
      table: {
        treeNode: true,
        align: 'left',
        width: '200px',
        slots: {
          default: 'name_default'
        }
      }
    },
    {
      title: '菜单类型',
      field: 'type',
      dictType: DICT_TYPE.SYSTEM_MENU_TYPE
    },
    {
      title: '路由地址',
      field: 'path'
    },
    {
      title: '组件路径',
      field: 'component'
    },
    {
      title: '权限标识',
      field: 'permission'
    },
    {
      title: '排序',
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
      formatter: 'formatDate'
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
