<script setup lang="ts">
import { onMounted, PropType, reactive, ref, watch } from 'vue'
import { required } from '@/utils/formRules'
import { Form } from '@/components/Form'
import { handleTree } from '@/utils/tree'
import { ElTreeSelect } from 'element-plus'
import { useForm } from '@/hooks/web/useForm'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { listSimpleMenusApi } from '@/api/system/menu'
import { CodegenTableVO } from '@/api/infra/codegen/types'
import { FormSchema } from '@/types/form'
const props = defineProps({
  genInfo: {
    type: Object as PropType<Nullable<CodegenTableVO>>,
    default: () => null
  }
})
const menuProps = {
  checkStrictly: true,
  children: 'children',
  label: 'name',
  value: 'id'
}
const rules = reactive({
  templateType: [required],
  scene: [required],
  moduleName: [required],
  businessName: [required],
  businessPackage: [required],
  className: [required],
  classComment: [required]
})
const templateTypeOptions = getIntDictOptions(DICT_TYPE.INFRA_CODEGEN_TEMPLATE_TYPE)
const sceneOptions = getIntDictOptions(DICT_TYPE.INFRA_CODEGEN_SCENE)
const treeRef = ref<InstanceType<typeof ElTreeSelect>>()
const menuOptions = ref<any>([]) // 树形结构
const getTree = async () => {
  const res = await listSimpleMenusApi()
  menuOptions.value = handleTree(res)
}
const schema = reactive<FormSchema[]>([
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
    label: '类名称',
    field: 'className',
    component: 'Input',
    labelMessage: '类名称（首字母大写），例如SysUser、SysMenu、SysDictData 等等',
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
    label: '上级菜单',
    field: 'parentMenuId',
    componentProps: {
      optionsSlot: true
    },
    labelMessage: '分配到指定菜单下，例如 系统管理',
    colProps: {
      span: 12
    }
  }
])
const { register, methods, elFormRef } = useForm({
  schema
})
const parentMenuId = ref<number>()
//子组件像父组件传值
const emit = defineEmits(['menu'])
const handleNodeClick = () => {
  emit('menu', parentMenuId.value)
}

// ========== 初始化 ==========
onMounted(async () => {
  await getTree()
})
watch(
  () => props.genInfo,
  (genInfo) => {
    if (!genInfo) return
    const { setValues } = methods
    setValues(genInfo)
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
  <Form :rules="rules" @register="register">
    <template #parentMenuId>
      <el-tree-select
        v-model="parentMenuId"
        ref="treeRef"
        node-key="id"
        :props="menuProps"
        :data="menuOptions"
        check-strictly
        @node-click="handleNodeClick"
      />
    </template>
  </Form>
</template>
