<script setup lang="ts">
import { PropType, reactive, watch } from 'vue'
import { required } from '@/utils/formRules'
import { CodegenTableVO } from '@/api/infra/codegen/types'
import { Form } from '@/components/Form'
import { useForm } from '@/hooks/web/useForm'
import { FormSchema } from '@/types/form'

const props = defineProps({
  basicInfo: {
    type: Object as PropType<Nullable<CodegenTableVO>>,
    default: () => null
  }
})
const rules = reactive({
  tableName: [required],
  tableComment: [required],
  className: [required],
  author: [required]
})
const schema = reactive<FormSchema[]>([
  {
    label: '表名称',
    field: 'tableName',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '表描述',
    field: 'tableComment',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '实体类名称',
    field: 'className',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '作者',
    field: 'author',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '备注',
    field: 'remark',
    component: 'Input',
    componentProps: {
      type: 'textarea',
      rows: 4
    },
    colProps: {
      span: 12
    }
  }
])
const { register, methods, elFormRef } = useForm({
  schema
})
watch(
  () => props.basicInfo,
  (basicInfo) => {
    if (!basicInfo) return
    const { setValues } = methods
    setValues(basicInfo)
  },
  {
    deep: true,
    immediate: true
  }
)

defineExpose({
  elFormRef,
  getFormData: methods.getFormData
})
</script>
<template>
  <Form :rules="rules" @register="register" />
</template>
