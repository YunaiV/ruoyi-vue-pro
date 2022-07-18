<script setup lang="ts">
import { PropType, reactive, watch } from 'vue'
import { required } from '@/utils/formRules'
import { CodegenTableVO } from '@/api/infra/codegen/types'
import { Form } from '@/components/Form'
import { useForm } from '@/hooks/web/useForm'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
const props = defineProps({
  currentRow: {
    type: Object as PropType<Nullable<CodegenTableVO>>,
    default: () => null
  }
})
const rules = reactive({
  templateType: [required],
  scene: [required],
  moduleName: [required],
  businessName: [required],
  businessPackage: [required],
  className: [required],
  classComment: [required]
})
const schema = reactive<FormSchema[]>([
  {
    label: '生成模板',
    field: 'templateType',
    component: 'Select',
    componentProps: {
      options: getDictOptions(DICT_TYPE.INFRA_CODEGEN_TEMPLATE_TYPE)
    },
    colProps: {
      span: 12
    }
  },
  {
    label: '生成场景',
    field: 'scene',
    component: 'Select',
    componentProps: {
      options: getDictOptions(DICT_TYPE.INFRA_CODEGEN_SCENE)
    },
    colProps: {
      span: 12
    }
  },
  {
    label: '模块名',
    field: 'moduleName',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '业务名',
    field: 'businessName',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '类名称',
    field: 'className',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '类描述',
    field: 'classComment',
    component: 'Input',
    colProps: {
      span: 12
    }
  },
  {
    label: '上级菜单',
    field: 'parentMenuId',
    component: 'Input',
    colProps: {
      span: 12
    }
  }
])
const { register, methods, elFormRef } = useForm({
  schema
})
watch(
  () => props.currentRow,
  (currentRow) => {
    if (!currentRow) return
    const { setValues } = methods
    setValues(currentRow)
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
