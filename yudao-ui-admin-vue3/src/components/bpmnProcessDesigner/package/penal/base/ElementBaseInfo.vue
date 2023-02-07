<template>
  <div class="panel-tab__content">
    <el-form label-width="90px" :model="needProps" :rules="rules">
      <div v-if="needProps.type == 'bpmn:Process'">
        <!-- 如果是 Process 信息的时候，使用自定义表单 -->
        <el-link
          href="https://doc.iocoder.cn/bpm/#_3-%E6%B5%81%E7%A8%8B%E5%9B%BE%E7%A4%BA%E4%BE%8B"
          type="danger"
          target="_blank"
          >如何实现实现会签、或签？</el-link
        >
        <el-form-item label="流程标识" prop="id">
          <el-input
            v-model="needProps.id"
            placeholder="请输入流标标识"
            :disabled="needProps.id !== undefined && needProps.id.length > 0"
            @change="handleKeyUpdate"
          />
        </el-form-item>
        <el-form-item label="流程名称" prop="name">
          <el-input
            v-model="needProps.name"
            placeholder="请输入流程名称"
            clearable
            @change="handleNameUpdate"
          />
        </el-form-item>
      </div>
      <div v-else>
        <el-form-item label="ID">
          <el-input v-model="elementBaseInfo.id" clearable @change="updateBaseInfo('id')" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="elementBaseInfo.name" clearable @change="updateBaseInfo('name')" />
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>
<script setup lang="ts" name="ElementBaseInfo">
const props = defineProps({
  businessObject: {
    type: Object,
    default: () => {}
  },
  model: {
    type: Object,
    default: () => {}
  }
})
const needProps = ref<any>({})
const bpmnElement = ref()
const elementBaseInfo = ref<any>({})
// 流程表单的下拉框的数据
// const forms = ref([])
// 流程模型的校验
const rules = reactive({
  id: [{ required: true, message: '流程标识不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '流程名称不能为空', trigger: 'blur' }]
})

const resetBaseInfo = () => {
  console.log(window, 'window')
  console.log(bpmnElement.value, 'bpmnElement')

  bpmnElement.value = window?.bpmnInstances?.bpmnElement
  console.log(bpmnElement.value, 'resetBaseInfo11111111111')
  elementBaseInfo.value = bpmnElement.value.businessObject
  needProps.value['type'] = bpmnElement.value.businessObject.$type
  // elementBaseInfo.value['typess'] = bpmnElement.value.businessObject.$type

  // elementBaseInfo.value = JSON.parse(JSON.stringify(bpmnElement.value.businessObject))
  console.log(elementBaseInfo.value, 'elementBaseInfo22222222222')
}
const handleKeyUpdate = (value) => {
  // 校验 value 的值，只有 XML NCName 通过的情况下，才进行赋值。否则，会导致流程图报错，无法绘制的问题
  if (!value) {
    return
  }
  if (!value.match(/[a-zA-Z_][\-_.0-9a-zA-Z$]*/)) {
    console.log('key 不满足 XML NCName 规则，所以不进行赋值')
    return
  }
  console.log('key 满足 XML NCName 规则，所以进行赋值')

  // 在 BPMN 的 XML 中，流程标识 key，其实对应的是 id 节点
  elementBaseInfo.value['id'] = value

  setTimeout(() => {
    updateBaseInfo('id')
  }, 100)
}
const handleNameUpdate = (value) => {
  console.log(elementBaseInfo, 'elementBaseInfo')
  if (!value) {
    return
  }
  elementBaseInfo.value['name'] = value

  setTimeout(() => {
    updateBaseInfo('name')
  }, 100)
}
// const handleDescriptionUpdate=(value)=> {
// TODO 芋艿：documentation 暂时无法修改，后续在看看
// this.elementBaseInfo['documentation'] = value;
// this.updateBaseInfo('documentation');
// }
const updateBaseInfo = (key) => {
  console.log(key, 'key')
  // 触发 elementBaseInfo 对应的字段
  const attrObj = Object.create(null)
  // console.log(attrObj, 'attrObj')
  attrObj[key] = elementBaseInfo.value[key]
  // console.log(attrObj, 'attrObj111')
  // const attrObj = {
  //   id: elementBaseInfo.value[key]
  //   // di: { id: `${elementBaseInfo.value[key]}_di` }
  // }
  console.log(elementBaseInfo, 'elementBaseInfo11111111111')
  needProps.value = { ...elementBaseInfo.value, ...needProps.value }

  if (key === 'id') {
    console.log('jinru')
    console.log(window, 'window')
    console.log(bpmnElement.value, 'bpmnElement')
    console.log(toRaw(bpmnElement.value), 'bpmnElement')
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
      id: elementBaseInfo.value[key],
      di: { id: `${elementBaseInfo.value[key]}_di` }
    })
  } else {
    console.log(attrObj, 'attrObj')
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), attrObj)
  }
}
onMounted(() => {
  // 针对上传的 bpmn 流程图时，需要延迟 1 秒的时间，保证 key 和 name 的更新
  setTimeout(() => {
    console.log(props.model, 'props.model')
    handleKeyUpdate(props.model.key)
    handleNameUpdate(props.model.name)
    console.log(props, 'propsssssssssssssssssssss')
  }, 1000)
})

watch(
  () => props.businessObject,
  (val) => {
    console.log(val, 'val11111111111111111111')
    if (val) {
      // nextTick(() => {
      resetBaseInfo()
      // })
    }
  }
)
// watch(
//   () => ({ ...props }),
//   (oldVal, newVal) => {
//     console.log(oldVal, 'oldVal')
//     console.log(newVal, 'newVal')
//     if (newVal) {
//       needProps.value = newVal
//     }
//   },
//   {
//     immediate: true
//   }
// )
// 'model.key': {
//   immediate: false,
//   handler: function (val) {
//     this.handleKeyUpdate(val)
//   }
// }
onBeforeUnmount(() => {
  bpmnElement.value = null
})
</script>
