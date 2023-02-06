import { VxeCrudSchema } from '@/hooks/web/useVxeCrudSchemas'

// 表单校验
export const rules = reactive({
  type: [{ required: true, message: '规则类型不能为空', trigger: 'change' }],
  roleIds: [{ required: true, message: '指定角色不能为空', trigger: 'change' }],
  deptIds: [{ required: true, message: '指定部门不能为空', trigger: 'change' }],
  postIds: [{ required: true, message: '指定岗位不能为空', trigger: 'change' }],
  userIds: [{ required: true, message: '指定用户不能为空', trigger: 'change' }],
  userGroupIds: [{ required: true, message: '指定用户组不能为空', trigger: 'change' }],
  scripts: [{ required: true, message: '指定脚本不能为空', trigger: 'change' }]
})

// CrudSchema
const crudSchemas = reactive<VxeCrudSchema>({
  primaryKey: 'id',
  primaryType: null,
  action: true,
  actionWidth: '200px',
  columns: [
    {
      title: '任务名',
      field: 'taskDefinitionName'
    },
    {
      title: '任务标识',
      field: 'taskDefinitionKey'
    },
    {
      title: '规则类型',
      field: 'type',
      dictType: DICT_TYPE.BPM_TASK_ASSIGN_RULE_TYPE,
      dictClass: 'number'
    },
    {
      title: '规则范围',
      field: 'options',
      table: {
        slots: {
          default: 'options_default'
        }
      }
    }
  ]
})
export const { allSchemas } = useVxeCrudSchemas(crudSchemas)
