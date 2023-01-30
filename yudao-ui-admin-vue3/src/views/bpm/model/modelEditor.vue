<template>
  <div class="app-container">
    <!-- 流程设计器，负责绘制流程等 -->
    <!-- <myProcessDesigner -->
    <my-process-designer
      :key="`designer-${reloadIndex}`"
      v-if="xmlString !== undefined"
      v-model="xmlString"
      :value="xmlString"
      v-bind="controlForm"
      keyboard
      ref="processDesigner"
      @init-finished="initModeler"
      :additionalModel="controlForm.additionalModel"
      @save="save"
    />
    <!-- 流程属性器，负责编辑每个流程节点的属性 -->
    <!-- <MyProcessPalette -->
    <my-properties-panel
      :key="`penal-${reloadIndex}`"
      :bpmnModeler="modeler"
      :prefix="controlForm.prefix"
      class="process-panel"
      :model="model"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
// import { translations } from '@/components/bpmnProcessDesigner/src/translations'
// 自定义元素选中时的弹出菜单（修改 默认任务 为 用户任务）
import CustomContentPadProvider from '@/components/bpmnProcessDesigner/package/designer/plugins/content-pad'
// 自定义左侧菜单（修改 默认任务 为 用户任务）
import CustomPaletteProvider from '@/components/bpmnProcessDesigner/package/designer/plugins/palette'
// import xmlObj2json from "./utils/xml2json";
// import myProcessDesigner from '@/components/bpmnProcessDesigner/package/designer/ProcessDesigner.vue'
// import MyProcessPalette from '@/components/bpmnProcessDesigner/package/palette/ProcessPalette.vue'
import { createModelApi, getModelApi, updateModelApi } from '@/api/bpm/model'

import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
const router = useRouter()

// 自定义侧边栏
// import MyProcessPanel from "../package/process-panel/ProcessPanel";

const xmlString = ref(undefined) // BPMN XML
const modeler = ref(null)
const reloadIndex = ref(0)
// const controlDrawerVisible = ref(false)
// const translationsSelf = translations
const controlForm = ref({
  simulation: true,
  labelEditing: false,
  labelVisible: false,
  prefix: 'flowable',
  headerButtonSize: 'mini',
  additionalModel: [CustomContentPadProvider, CustomPaletteProvider]
})
// const addis = ref({
//   CustomContentPadProvider,
//   CustomPaletteProvider
// })
// 流程模型的信息
const model = ref({})
onMounted(() => {
  // 如果 modelId 非空，说明是修改流程模型
  const modelId = router.currentRoute.value.query && router.currentRoute.value.query.modelId
  console.log(modelId, 'modelId')
  if (modelId) {
    // let data = '4b4909d8-97e7-11ec-8e20-862bc1a4a054'
    getModelApi(modelId).then((data) => {
      console.log(data, 'response')
      xmlString.value = data.bpmnXml
      model.value = {
        ...data,
        bpmnXml: undefined // 清空 bpmnXml 属性
      }
      // this.controlForm.processId = data.key

      // xmlString.value =
      //   '<?xml version="1.0" encoding="UTF-8"?>\n<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="diagram_Process_1645980650311" targetNamespace="http://activiti.org/bpmn"><bpmn2:process id="flowable_01" name="flowable测试" isExecutable="true"><bpmn2:startEvent id="Event_1iruxim"><bpmn2:outgoing>Flow_0804gmo</bpmn2:outgoing></bpmn2:startEvent><bpmn2:userTask id="task01" name="task01"><bpmn2:incoming>Flow_0804gmo</bpmn2:incoming><bpmn2:outgoing>Flow_0cx479x</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="Flow_0804gmo" sourceRef="Event_1iruxim" targetRef="task01" /><bpmn2:endEvent id="Event_1mdsccz"><bpmn2:incoming>Flow_0cx479x</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="Flow_0cx479x" sourceRef="task01" targetRef="Event_1mdsccz" /></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="flowable_01_di" bpmnElement="flowable_01"><bpmndi:BPMNEdge id="Flow_0cx479x_di" bpmnElement="Flow_0cx479x"><di:waypoint x="440" y="350" /><di:waypoint x="492" y="350" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id="Flow_0804gmo_di" bpmnElement="Flow_0804gmo"><di:waypoint x="288" y="350" /><di:waypoint x="340" y="350" /></bpmndi:BPMNEdge><bpmndi:BPMNShape id="Event_1iruxim_di" bpmnElement="Event_1iruxim"><dc:Bounds x="252" y="332" width="36" height="36" /></bpmndi:BPMNShape><bpmndi:BPMNShape id="task01_di" bpmnElement="task01"><dc:Bounds x="340" y="310" width="100" height="80" /></bpmndi:BPMNShape><bpmndi:BPMNShape id="Event_1mdsccz_di" bpmnElement="Event_1mdsccz"><dc:Bounds x="492" y="332" width="36" height="36" /></bpmndi:BPMNShape></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>'

      // model.value = {
      //   key: 'flowable_01',
      //   name: 'flowable测试',
      //   description: 'ooxx',
      //   category: '1',
      //   formType: 10,
      //   formId: 11,
      //   formCustomCreatePath: null,
      //   formCustomViewPath: null,
      //   id: '4b4909d8-97e7-11ec-8e20-862bc1a4a054',
      //   createTime: 1645978019795,
      //   bpmnXml: undefined // 清空 bpmnXml 属性
      // }
      // console.log(modeler.value, 'modeler11111111')
    })
  }
})
const initModeler = (item) => {
  setTimeout(() => {
    modeler.value = item
    console.log(item, 'initModeler方法modeler')
    console.log(modeler.value, 'initModeler方法modeler')
    // controlForm.value.prefix = '2222'
  }, 10)
}

const save = (bpmnXml) => {
  const data = {
    ...model.value,
    bpmnXml: bpmnXml // this.bpmnXml 只是初始化流程图，后续修改无法通过它获得
  }
  console.log(data, 'data')

  // 修改的提交
  if (data.id) {
    updateModelApi(data).then((response) => {
      console.log(response, 'response')
      // this.$modal.msgSuccess("修改成功")
      ElMessage.success('修改成功')

      // 跳转回去
      close()
    })
    return
  }
  // 添加的提交
  createModelApi(data).then((response) => {
    console.log(response, 'response1')
    // this.$modal.msgSuccess("保存成功")
    ElMessage.success('保存成功')
    // 跳转回去
    close()
  })
}
/** 关闭按钮 */
const close = () => {
  // this.$tab.closeOpenPage({ path: "/bpm/manager/model" })
  router.push({ path: '/bpm/manager/model' })
}
</script>

<style lang="scss">
//body {
//  overflow: hidden;
//  margin: 0;
//  box-sizing: border-box;
//}
//.app {
//  width: 100%;
//  height: 100%;
//  box-sizing: border-box;
//  display: inline-grid;
//  grid-template-columns: 100px auto max-content;
//}
.demo-control-bar {
  position: fixed;
  right: 8px;
  bottom: 8px;
  z-index: 1;
  .open-control-dialog {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 4px;
    font-size: 32px;
    background: rgba(64, 158, 255, 1);
    color: #ffffff;
    cursor: pointer;
  }
}

// TODO 芋艿：去掉多余的 faq
//.info-tip {
//  position: fixed;
//  top: 40px;
//  right: 500px;
//  z-index: 10;
//  color: #999999;
//}

.control-form {
  .el-radio {
    width: 100%;
    line-height: 32px;
  }
}
.element-overlays {
  box-sizing: border-box;
  padding: 8px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 4px;
  color: #fafafa;
}

.my-process-designer {
  height: calc(100vh - 84px);
}
.process-panel__container {
  position: absolute;
  right: 0;
  top: 55px;
  height: calc(100vh - 84px);
}
</style>
