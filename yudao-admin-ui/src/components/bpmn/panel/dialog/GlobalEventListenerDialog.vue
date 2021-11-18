<template>
  <el-dialog title="自定义全局监听器" :visible.sync="dialogFormVisible" :before-close="close">
    <el-form :model="form">
      <el-form-item label="监听类型:" :label-width="formLabelWidth">
        <el-select v-model="form.type" placeholder="选择">
          <el-option label="类" value="class"></el-option>
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
  name: "GlobalEventListenerDialog",
  data() {
    return {
      activeNames: ['1'],
      listenerTable: [],
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
    element:{
      type: Object,
      required: false
    },
    globalFormTable:{
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
      const from = this.formData;
      // 获取原数组中是否存在这个class了 如果存在就退出不操作
      const filterArr = this.globalFormTable.filter(
          (item) => item.class === from.class
      )
      if (filterArr.length !== 0) {
        this.$emit('commitGlobalForm', null);
        return
      }
      if (this.element.businessObject
          && this.element.businessObject.extensionElements
          && this.element.businessObject.extensionElements.values) {
        let eventListener  = this.modeler.get("bpmnFactory").create("activiti:EventListener", from);
        let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements",
            {
              values: this.element.businessObject.extensionElements.values.concat(eventListener),
            }
        );
        this.modeler.get("modeling").updateProperties(this.element, {extensionElements});
      } else {
        let eventListener  = this.modeler.get("bpmnFactory").create("activiti:EventListener", from);
        let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements",
            {
              values: [eventListener],
            }
        );
        this.modeler.get("modeling").updateProperties(this.element, {extensionElements});
      }
      this.$emit('commitGlobalForm', this.form);
    },
    close(){
      this.$emit('commitGlobalForm', null);
    }
  }
}
</script>

<style scoped>
/deep/.el-dialog > .el-dialog__header{
  padding: 24px 20px
}
</style>