import { required } from '@/utils/formRules'
import { reactive } from 'vue'

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
    field: 'email',
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
    component: 'Input'
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
