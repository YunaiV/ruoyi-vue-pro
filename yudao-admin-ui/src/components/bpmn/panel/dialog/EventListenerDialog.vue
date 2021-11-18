<template>
  <el-dialog title="自定义全局监听器" :visible.sync="dialogFormVisible" :before-close="close">
    <el-form :model="form">
      <el-form-item label="事件类型:" :label-width="formLabelWidth">
        <el-select v-model="form.type" placeholder="选择">
          <el-option label="类" value="class"></el-option>
          <el-option label="表达式" value="expression"></el-option>
          <el-option label="代理表达式" value="delegateExpression"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="事件类型:" :label-width="formLabelWidth">
        <el-select v-model="form.event" placeholder="选择">
          <el-option label="start" value="start"></el-option>
          <el-option label="take" value="take"></el-option>
          <el-option label="end" value="end"></el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="值:" :label-width="formLabelWidth">
        <el-input v-model="form.class" style="width: 50%"></el-input>
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary"  @click="commitForm()">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
export default {
  name: "EventListenerDialog",
  data() {
    return {
      activeNames: ['1'],
      formLabelWidth: "200px",
      value: "类",
      options: [
        {
          value: "类",
          label: "类"
        }
      ]
    }
  },
  props: {
    formData:{
      type: Object,
      required: true
    },
    dialogFormVisibleBool:{
      type: Boolean,
      required: false
    },
    modeler: {
      type: Object,
      required: false
    },
    nodeElement:{
      type: Object,
      required: false
    },
    listenerTable:{
      type: Array,
      required: false
    }
  },
  computed:{
    form:{
      get(){
        return this.formData;
      }
    },
    dialogFormVisible:{
      get(){
        return this.dialogFormVisibleBool
      }
    }
  },
  methods: {
    commitForm(){
      const from = this.form
      const filterArr = this.listenerTable.filter(
          (item) => (item.event === from.event && from.type === item.type && from.class === item.class)
      )
      if (filterArr.length !== 0) {
        this.$emit('commitEventForm', null);
        return
      }
      let data = {}
      data[from.type] = from.class
      data['event'] = from.event
      if (this.nodeElement.businessObject
          && this.nodeElement.businessObject.extensionElements
          && this.nodeElement.businessObject.extensionElements.values) {
        let eventListener  = this.modeler.get("bpmnFactory").create("activiti:ExecutionListener", data);
        let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements",
            {values: this.nodeElement.businessObject.extensionElements.values.concat(eventListener),});
        this.modeler.get("modeling").updateProperties(this.nodeElement, {extensionElements});
      } else {

        let eventListener  = this.modeler.get("bpmnFactory").create("activiti:ExecutionListener", data);
        let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements", {values: [eventListener],});
        this.modeler.get("modeling").updateProperties(this.nodeElement, {extensionElements});
      }
      this.$emit('commitEventForm', this.form);
    },
    close(){
      this.$emit('commitEventForm', null);
    }
  }
}
</script>

<style scoped>
/deep/.el-dialog > .el-dialog__header{
  padding: 24px 20px
}
</style>