<template>
  <Form :rules="rules" @register="register" />
</template>
<script setup lang="ts">
import { useForm } from '@/hooks/web/useForm'
import { FormSchema } from '@/types/form'
import { CodegenTableVO } from '@/api/infra/codegen/types'
import { getIntDictOptions } from '@/utils/dict'
import { listSimpleMenusApi } from '@/api/system/menu'
import { handleTree, defaultProps } from '@/utils/tree'
import { PropType } from 'vue'

const props = defineProps({
  basicInfo: {
    type: Object as PropType<Nullable<CodegenTableVO>>,
    default: () => null
  }
})

const templateTypeOptions = getIntDictOptions(DICT_TYPE.INFRA_CODEGEN_TEMPLATE_TYPE)
const sceneOptions = getIntDictOptions(DICT_TYPE.INFRA_CODEGEN_SCENE)
const menuOptions = ref<any>([]) // 树形结构
const getTree = async () => {
  const res = await listSimpleMenusApi()
  menuOptions.value = handleTree(res)
}

const rules = reactive({
  tableName: [required],
  tableComment: [required],
  className: [required],
  author: [required],
  templateType: [required],
  scene: [required],
  moduleName: [required],
  businessName: [required],
  businessPackage: [required],
  classComment: [required]
})
const schema = reactive<FormSchema[]>([
  {
    label: '上级菜单',
    field: 'parentMenuId',
    component: 'TreeSelect',
    componentProps: {
      data: menuOptions,
      props: defaultProps,
      checkStrictly: true,
      nodeKey: 'id'
    },
    labelMessage: '分配到指定菜单下，例如 系统管理',
    colProps: {
      span: 24
    }
  },
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
    label: '类名称',
    field: 'className',
    component: 'Input',
    labelMessage: '类名称（首字母大写），例如SysUser、SysMenu、SysDictData 等等',
    colProps: {
      span: 12
    }
  },
  {
    label: '生成模板',
    field: 'templateType',
    component: 'Select',
    componentProps: {
      options: templateTypeOptions
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
      options: sceneOptions
    },
    colProps: {
      span: 12
    }
  },
  {
    label: '模块名',
    field: 'moduleName',
    component: 'Input',
    labelMessage: '模块名，即一级目录，例如 system、infra、tool 等等',
    colProps: {
      span: 12
    }
  },
  {
    label: '业务名',
    field: 'businessName',
    component: 'Input',
    labelMessage: '业务名，即二级目录，例如 user、permission、dict 等等',
    colProps: {
      span: 12
    }
  },
  {
    label: '类描述',
    field: 'classComment',
    component: 'Input',
    labelMessage: '用作类描述，例如 用户',
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
      span: 24
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
// ========== 初始化 ==========
onMounted(async () => {
  await getTree()
})

defineExpose({
  elFormRef,
  getFormData: methods.getFormData
})
</script>
