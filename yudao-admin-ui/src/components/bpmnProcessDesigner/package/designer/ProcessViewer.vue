<template>
  <div class="my-process-designer">
    <div class="my-process-designer__container">
      <div class="my-process-designer__canvas" ref="bpmn-canvas"></div>
    </div>
  </div>
</template>

<script>
import BpmnViewer from "bpmn-js/lib/Viewer";
import DefaultEmptyXML from "./plugins/defaultEmpty";

export default {
  name: "MyProcessViewer",
  componentName: "MyProcessViewer",
  props: {
    value: String, // xml 字符串
    prefix: {
      type: String,
      default: "camunda"
    }
  },
  data() {
    return {};
  },
  mounted() {
    this.initBpmnModeler();
    this.createNewDiagram(this.value);
    this.$once("hook:beforeDestroy", () => {
      if (this.bpmnModeler) this.bpmnModeler.destroy();
      this.$emit("destroy", this.bpmnModeler);
      this.bpmnModeler = null;
    });
  },
  watch: {
    value: function (newValue) { // 在 xmlString 发生变化时，重新创建，从而绘制流程图
      this.createNewDiagram(newValue);
    }
  },
  methods: {
    initBpmnModeler() {
      if (this.bpmnModeler) return;
      this.bpmnModeler = new BpmnViewer({
        container: this.$refs["bpmn-canvas"]
      })
    },
    /* 创建新的流程图 */
    async createNewDiagram(xml) {
      // 将字符串转换成图显示出来
      let newId = `Process_${new Date().getTime()}`;
      let newName = `业务流程_${new Date().getTime()}`;
      let xmlString = xml || DefaultEmptyXML(newId, newName, this.prefix);
      try {
        console.log(this.bpmnModeler.importXML);
        let { warnings } = await this.bpmnModeler.importXML(xmlString);
        if (warnings && warnings.length) {
          warnings.forEach(warn => console.warn(warn));
        }
      } catch (e) {
        console.error(`[Process Designer Warn]: ${e?.message || e}`);
      }
    }
  }
};
</script>
