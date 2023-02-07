<template>
  <div class="panel-tab__content">
    <el-form label-width="90px">
      <el-form-item label="回路特性">
        <el-select v-model="loopCharacteristics" @change="changeLoopCharacteristicsType">
          <!--bpmn:MultiInstanceLoopCharacteristics-->
          <el-option label="并行多重事件" value="ParallelMultiInstance" />
          <el-option label="时序多重事件" value="SequentialMultiInstance" />
          <!--bpmn:StandardLoopCharacteristics-->
          <el-option label="循环事件" value="StandardLoop" />
          <el-option label="无" value="Null" />
        </el-select>
      </el-form-item>
      <template
        v-if="
          loopCharacteristics === 'ParallelMultiInstance' ||
          loopCharacteristics === 'SequentialMultiInstance'
        "
      >
        <el-form-item label="循环基数" key="loopCardinality">
          <el-input
            v-model="loopInstanceForm.loopCardinality"
            clearable
            @change="updateLoopCardinality"
          />
        </el-form-item>
        <el-form-item label="集合" key="collection" v-show="false">
          <el-input v-model="loopInstanceForm.collection" clearable @change="updateLoopBase" />
        </el-form-item>
        <el-form-item label="元素变量" key="elementVariable">
          <el-input v-model="loopInstanceForm.elementVariable" clearable @change="updateLoopBase" />
        </el-form-item>
        <el-form-item label="完成条件" key="completionCondition">
          <el-input
            v-model="loopInstanceForm.completionCondition"
            clearable
            @change="updateLoopCondition"
          />
        </el-form-item>
        <el-form-item label="异步状态" key="async">
          <el-checkbox
            v-model="loopInstanceForm.asyncBefore"
            label="异步前"
            @change="updateLoopAsync('asyncBefore')"
          />
          <el-checkbox
            v-model="loopInstanceForm.asyncAfter"
            label="异步后"
            @change="updateLoopAsync('asyncAfter')"
          />
          <el-checkbox
            v-model="loopInstanceForm.exclusive"
            v-if="loopInstanceForm.asyncAfter || loopInstanceForm.asyncBefore"
            label="排除"
            @change="updateLoopAsync('exclusive')"
          />
        </el-form-item>
        <el-form-item
          label="重试周期"
          prop="timeCycle"
          v-if="loopInstanceForm.asyncAfter || loopInstanceForm.asyncBefore"
          key="timeCycle"
        >
          <el-input v-model="loopInstanceForm.timeCycle" clearable @change="updateLoopTimeCycle" />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts" name="ElementMultiInstance">
const props = defineProps({
  businessObject: Object,
  type: String
})
const prefix = inject('prefix')
const loopCharacteristics = ref('')
//默认配置，用来覆盖原始不存在的选项，避免报错
const defaultLoopInstanceForm = ref({
  completionCondition: '',
  loopCardinality: '',
  extensionElements: [],
  asyncAfter: false,
  asyncBefore: false,
  exclusive: false
})
const loopInstanceForm = ref<any>({})
const bpmnElement = ref(null)
const multiLoopInstance = ref(null)

const getElementLoop = (businessObject) => {
  if (!businessObject.loopCharacteristics) {
    loopCharacteristics.value = 'Null'
    loopInstanceForm.value = {}
    return
  }
  if (businessObject.loopCharacteristics.$type === 'bpmn:StandardLoopCharacteristics') {
    loopCharacteristics.value = 'StandardLoop'
    loopInstanceForm.value = {}
    return
  }
  if (businessObject.loopCharacteristics.isSequential) {
    loopCharacteristics.value = 'SequentialMultiInstance'
  } else {
    loopCharacteristics.value = 'ParallelMultiInstance'
  }
  // 合并配置
  loopInstanceForm.value = {
    ...defaultLoopInstanceForm.value,
    ...businessObject.loopCharacteristics,
    completionCondition: businessObject.loopCharacteristics?.completionCondition?.body ?? '',
    loopCardinality: businessObject.loopCharacteristics?.loopCardinality?.body ?? ''
  }
  // 保留当前元素 businessObject 上的 loopCharacteristics 实例
  multiLoopInstance.value = window.bpmnInstances.bpmnElement.businessObject.loopCharacteristics
  // 更新表单
  if (
    businessObject.loopCharacteristics.extensionElements &&
    businessObject.loopCharacteristics.extensionElements.values &&
    businessObject.loopCharacteristics.extensionElements.values.length
  ) {
    loopInstanceForm.value['timeCycle'] =
      businessObject.loopCharacteristics.extensionElements.values[0].body
  }
}
const changeLoopCharacteristicsType = (type) => {
  // this.loopInstanceForm = { ...this.defaultLoopInstanceForm }; // 切换类型取消原表单配置
  // 取消多实例配置
  if (type === 'Null') {
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
      loopCharacteristics: null
    })
    return
  }
  // 配置循环
  if (type === 'StandardLoop') {
    const loopCharacteristicsObject = window.bpmnInstances.moddle.create(
      'bpmn:StandardLoopCharacteristics'
    )
    window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
      loopCharacteristics: loopCharacteristicsObject
    })
    multiLoopInstance.value = null
    return
  }
  // 时序
  if (type === 'SequentialMultiInstance') {
    multiLoopInstance.value = window.bpmnInstances.moddle.create(
      'bpmn:MultiInstanceLoopCharacteristics',
      { isSequential: true }
    )
  } else {
    multiLoopInstance.value = window.bpmnInstances.moddle.create(
      'bpmn:MultiInstanceLoopCharacteristics',
      { collection: '${coll_userList}' }
    )
  }
  window.bpmnInstances.modeling.updateProperties(toRaw(bpmnElement.value), {
    loopCharacteristics: toRaw(multiLoopInstance.value)
  })
}
// 循环基数
const updateLoopCardinality = (cardinality) => {
  let loopCardinality = null
  if (cardinality && cardinality.length) {
    loopCardinality = window.bpmnInstances.moddle.create('bpmn:FormalExpression', {
      body: cardinality
    })
  }
  window.bpmnInstances.modeling.updateModdleProperties(
    toRaw(bpmnElement.value),
    multiLoopInstance.value,
    {
      loopCardinality
    }
  )
}
// 完成条件
const updateLoopCondition = (condition) => {
  let completionCondition = null
  if (condition && condition.length) {
    completionCondition = window.bpmnInstances.moddle.create('bpmn:FormalExpression', {
      body: condition
    })
  }
  window.bpmnInstances.modeling.updateModdleProperties(
    toRaw(bpmnElement.value),
    multiLoopInstance.value,
    {
      completionCondition
    }
  )
}
// 重试周期
const updateLoopTimeCycle = (timeCycle) => {
  const extensionElements = window.bpmnInstances.moddle.create('bpmn:ExtensionElements', {
    values: [
      window.bpmnInstances.moddle.create(`${prefix}:FailedJobRetryTimeCycle`, {
        body: timeCycle
      })
    ]
  })
  window.bpmnInstances.modeling.updateModdleProperties(
    toRaw(bpmnElement.value),
    multiLoopInstance.value,
    {
      extensionElements
    }
  )
}
// 直接更新的基础信息
const updateLoopBase = () => {
  window.bpmnInstances.modeling.updateModdleProperties(
    toRaw(bpmnElement.value),
    multiLoopInstance.value,
    {
      collection: loopInstanceForm.value.collection || null,
      elementVariable: loopInstanceForm.value.elementVariable || null
    }
  )
}
// 各异步状态
const updateLoopAsync = (key) => {
  const { asyncBefore, asyncAfter } = loopInstanceForm.value
  let asyncAttr = Object.create(null)
  if (!asyncBefore && !asyncAfter) {
    // this.$set(this.loopInstanceForm, "exclusive", false);
    loopInstanceForm.value['exclusive'] = false
    asyncAttr = { asyncBefore: false, asyncAfter: false, exclusive: false, extensionElements: null }
  } else {
    asyncAttr[key] = loopInstanceForm.value[key]
  }
  window.bpmnInstances.modeling.updateModdleProperties(
    toRaw(bpmnElement.value),
    multiLoopInstance.value,
    asyncAttr
  )
}

onBeforeUnmount(() => {
  multiLoopInstance.value = null
  bpmnElement.value = null
})

watch(
  () => props.businessObject,
  (val) => {
    bpmnElement.value = window.bpmnInstances.bpmnElement
    getElementLoop(val)
  },
  { immediate: true }
)
</script>
