import { reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { DICT_TYPE } from '@/utils/dict'
import { VxeCrudSchema, useVxeCrudSchemas } from '@/hooks/web/useVxeCrudSchemas'
const { t } = useI18n() // 国际化

// 表单校验
export const rules = reactive({
  name: [required],
  sort: [required],
  email: [required],
  phone: [
    {
      pattern:
        /^(?:(?:\+|00)86)?1(?:(?:3[\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\d])|(?:9[189]))\d{8}$/, // TODO @星语：前端只校验长度，格式交给后端；因为号码格式不断在变的
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
      field: 'email'
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
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
