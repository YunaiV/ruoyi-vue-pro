<template>
  <div class="app-container">

    <!-- 流程设计器，负责绘制流程等 -->
    <my-process-viewer key="designer" v-model="xmlString" v-bind="controlForm" keyboard ref="processDesigner" />

  </div>
</template>

<script>
import {getModel} from "@/api/bpm/model";
export default {
  name: "App",
  components: { },
  data() {
    return {
      xmlString: "", // BPMN XML
      controlForm: {
        prefix: "activiti"
      },
    };
  },
  created() {
    // 如果 modelId 非空，说明是修改流程模型
    const modelId = this.$route.query && this.$route.query.modelId
    if (modelId) {
      getModel(modelId).then(response => {
        this.xmlString = response.data.bpmnXml
      })
    }
  }
};
</script>

<style lang="scss">

.my-process-designer {
  height: calc(100vh - 84px);
}

</style>
