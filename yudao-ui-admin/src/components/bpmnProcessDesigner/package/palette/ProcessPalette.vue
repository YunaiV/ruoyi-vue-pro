<template>
  <div class="my-process-palette">
    <div class="test-button" @click="addTask" @mousedown="addTask">测试任务</div>
    <div class="test-container" id="palette-container">1</div>
  </div>
</template>

<script>
import { assign } from "min-dash";

export default {
  name: "MyProcessPalette",
  data() {
    return {};
  },
  mounted() {},
  methods: {
    addTask(event, options = {}) {
      const ElementFactory = window.bpmnInstances.elementFactory;
      const create = window.bpmnInstances.modeler.get("create");

      console.log(ElementFactory, create);

      const shape = ElementFactory.createShape(assign({ type: "bpmn:UserTask" }, options));

      if (options) {
        shape.businessObject.di.isExpanded = options.isExpanded;
      }

      create.start(event, shape);
    }
  }
};
</script>

<style scoped lang="scss">
.my-process-palette {
  box-sizing: border-box;
  padding: 80px 20px 20px 20px;
  .test-button {
    box-sizing: border-box;
    padding: 8px 16px;
    border-radius: 4px;
    border: 1px solid rgba(24, 144, 255, 0.8);
    cursor: pointer;
  }
}
</style>
