<template>
  <div class="panel-tab__content">
    <el-form :model="flowConditionForm" label-width="90px" size="small">
      <el-form-item label="流转类型">
        <el-select v-model="flowConditionForm.type" @change="updateFlowType">
          <el-option label="普通流转路径" value="normal" />
          <el-option label="默认流转路径" value="default" />
          <el-option label="条件流转路径" value="condition" />
        </el-select>
      </el-form-item>
      <el-form-item label="条件格式" v-if="flowConditionForm.type === 'condition'" key="condition">
        <el-select v-model="flowConditionForm.conditionType">
          <el-option label="表达式" value="expression" />
          <el-option label="脚本" value="script" />
        </el-select>
      </el-form-item>
      <el-form-item
        label="表达式"
        v-if="flowConditionForm.conditionType && flowConditionForm.conditionType === 'expression'"
        key="express"
      >
        <el-input
          v-model="flowConditionForm.body"
          style="width: 192px"
          clearable
          @change="updateFlowCondition"
        />
      </el-form-item>
      <template
        v-if="flowConditionForm.conditionType && flowConditionForm.conditionType === 'script'"
      >
        <el-form-item label="脚本语言" key="language">
          <el-input v-model="flowConditionForm.language" clearable @change="updateFlowCondition" />
        </el-form-item>
        <el-form-item label="脚本类型" key="scriptType">
          <el-select v-model="flowConditionForm.scriptType">
            <el-option label="内联脚本" value="inlineScript" />
            <el-option label="外部脚本" value="externalScript" />
          </el-select>
        </el-form-item>
        <el-form-item
          label="脚本"
          v-if="flowConditionForm.scriptType === 'inlineScript'"
          key="body"
        >
          <el-input
            v-model="flowConditionForm.body"
            type="textarea"
            clearable
            @change="updateFlowCondition"
          />
        </el-form-item>
        <el-form-item
          label="资源地址"
          v-if="flowConditionForm.scriptType === 'externalScript'"
          key="resource"
        >
          <el-input v-model="flowConditionForm.resource" clearable @change="updateFlowCondition" />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts" name="FlowCondition">
const props = defineProps({
  businessObject: Object,
  type: String
})
const flowConditionForm = ref<any>({})
const bpmnElement = ref()
const bpmnElementSource = ref()
const bpmnElementSourceRef = ref()
const flowConditionRef = ref()
const resetFlowCondition = () => {
  bpmnElement.value = window.bpmnInstances.bpmnElement
  bpmnElementSource.value = bpmnElement.value.source
  bpmnElementSourceRef.value = bpmnElement.value.businessObject.sourceRef
  if (
    bpmnElementSourceRef.value &&
    bpmnElementSourceRef.value.default &&
    bpmnElementSourceRef.value.default.id === this.bpmnElement.id
  ) {
    // 默认
    flowConditionForm.value = { type: 'default' }
  } else if (!bpmnElement.value.businessObject.conditionExpression) {
    // 普通
    flowConditionForm.value = { type: 'normal' }
  } else {
    // 带条件
    const conditionExpression = bpmnElement.value.businessObject.conditionExpression
    flowConditionForm.value = { ...conditionExpression, type: 'condition' }
    // resource 可直接标识 是否是外部资源脚本
    if (flowConditionForm.value.resource) {
      // this.$set(this.flowConditionForm, "conditionType", "script");
      // this.$set(this.flowConditionForm, "scriptType", "externalScript");
      flowConditionForm.value['conditionType'] = 'script'
      flowConditionForm.value['scriptType'] = 'externalScript'
      return
    }
    if (conditionExpression.language) {
      // this.$set(this.flowConditionForm, "conditionType", "script");
      // this.$set(this.flowConditionForm, "scriptType", "inlineScript");
      flowConditionForm.value['conditionType'] = 'script'
      flowConditionForm.value['scriptType'] = 'inlineScript'

      return
    }
    // this.$set(this.flowConditionForm, "conditionType", "expression");
    flowConditionForm.value['conditionType'] = 'expression'
  }
}
const updateFlowType = (flowType) => {
  // 正常条件类
  if (flowType === 'condition') {
    flowConditionRef.value = window.bpmnInstances.moddle.create('bpmn:FormalExpression')
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
      conditionExpression: flowConditionRef.value
    })
    return
  }
  // 默认路径
  if (flowType === 'default') {
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
      conditionExpression: null
    })
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElementSource.value), {
      default: bpmnElement.value
    })
    return
  }
  // 正常路径，如果来源节点的默认路径是当前连线时，清除父元素的默认路径配置
  if (
    bpmnElementSourceRef.value.default &&
    bpmnElementSourceRef.value.default.id === bpmnElement.value.id
  ) {
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElementSource.value), {
      default: null
    })
  }
  window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
    conditionExpression: null
  })
}
const updateFlowCondition = () => {
  let { conditionType, scriptType, body, resource, language } = flowConditionForm.value
  let condition
  if (conditionType === 'expression') {
    condition = window.bpmnInstances.moddle.create('bpmn:FormalExpression', { body })
  } else {
    if (scriptType === 'inlineScript') {
      condition = window.bpmnInstances.moddle.create('bpmn:FormalExpression', { body, language })
      // this.$set(this.flowConditionForm, "resource", "");
      flowConditionForm.value['resource'] = ''
    } else {
      // this.$set(this.flowConditionForm, "body", "");
      flowConditionForm.value['body'] = ''
      condition = window.bpmnInstances.moddle.create('bpmn:FormalExpression', {
        resource,
        language
      })
    }
  }
  window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
    conditionExpression: condition
  })
}

onBeforeUnmount(() => {
  bpmnElement.value = null
  bpmnElementSource.value = null
  bpmnElementSourceRef.value = null
})

watch(
  () => props.businessObject,
  (val) => {
    if (val) {
      nextTick(() => {
        resetFlowCondition()
      })
    }
  }
)
</script>
