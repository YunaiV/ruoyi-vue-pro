import { required } from '@/utils/formRules'
import { reactive } from 'vue'
import { FormSchema } from '@/types/form'

// 表单校验
export const rules = reactive({
  name: [required],
  sort: [required],
  email: [required],
  phone: [
    {
      pattern:
        /^(?:(?:\+|00)86)?1(?:(?:3[\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\d])|(?:9[189]))\d{8}$/,
      trigger: 'blur',
      message: '请输入正确的手机号码'
    }
  ]
})

export const modelSchema = reactive<FormSchema[]>([
  {
    label: '上级部门',
    field: 'parentId',
    component: 'Input'
  },
  {
    label: '部门名称',
    field: 'name',
    component: 'Input',
    formItemProps: {
      rules: [required]
    }
  },
  {
    label: '负责人',
    field: 'leaderUserId',
    component: 'Input'
  },
  {
    label: '联系电话',
    field: 'phone',
    component: 'Input'
  },
  {
    label: '邮箱',
    field: 'email',
    component: 'Input'
  },
  {
    label: '显示排序',
    field: 'sort',
    component: 'InputNumber',
    value: 0
  },
  {
    label: '状态',
    field: 'status',
    component: 'RadioButton',
    componentProps: {
      options: [
        {
          label: '开启',
          value: 0
        },
        {
          label: '关闭',
          value: 1
        }
      ]
    }
  }
])
