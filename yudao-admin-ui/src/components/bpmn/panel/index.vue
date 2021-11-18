<template>
  <div class="bpmn-panel">
    <el-container>
<!--      <el-header height="45px">-->
<!--        <div class="config-tab" :class="{active: configTab=='node'}" @click="handleConfigSelect('node')">节点属性</div>-->
<!--        <div class="config-tab" :class="{active: configTab=='process'}" @click="handleConfigSelect('process')">流程属性</div>-->
<!--      </el-header>-->
      <el-main>
<!--        节点的控制面板-->
        <node-property-panel v-if="configTab==='node'" :modeler="modeler" @modifyConfigTab="modifyConfigTab"
                             :nodeElement="nodeElement" :formData="formData" @modifyFormData="modifyFormData"></node-property-panel>
        <start-event-node-property-panel v-if="configTab==='start-node'" :modeler="modeler" @modifyConfigTab="modifyConfigTab"
                                         :nodeElement="nodeElement" :formData="formData" @modifyFormData="modifyFormData"></start-event-node-property-panel>
<!--        流程的控制面板-->
        <process-property-panel v-if="configTab==='process'" :modeler="modeler" :process-data="process"
                                :element="element"></process-property-panel>
      </el-main>
    </el-container>
  </div>
</template>

<script>
  import NodePropertyPanel from "./NodePropertyPanel";
  import ProcessPropertyPanel from "./ProcessPropertyPanel";
  import StartEventNodePropertyPanel from "./StartEventNodePropertyPanel";
  export default {
    name: "index",
    data() {
      return {
        configTab: 'process',
        panelIndex: 8,
        element: {},
        nodeElement: {},
        formData: {}
      }
    },
    props: {
      modeler: {
        type: Object,
        required: true
      },
      process: {
        type: Object,
        required: true
      }
    },
    mounted() {
      this.handleModeler();
    },
    methods: {
      handleConfigSelect(value) {
        this.configTab = value;
      },
      handleModeler() {
        const _this = this;
        this.modeler.on("root.added", e => {
          let element = e.element;
          if (this.isImplicitRoot(element)) {
            return;
          }
          this.element = element;
        });
        // 数据变化
        this.modeler.on("commandStack.changed", () => {
          _this.modeler.saveXML({format: true}, function (err, xml) {
            _this.$emit('updateXml', xml)
          });
        })
        //数据变化
        this.modeler.on("selection.changed", e => {
          const element = e.newSelection[0];
          if (!element) {
            return;
          }
          console.log("selection.changed: element", element)
          this.modifyConfigTab(element);
          this.handleFormData(element);
          this.changeFormData(element)
        })
        this.modeler.on("element.changed", e => {
          const {element} = e;
          if (!element) {
            return;
          }
          console.log("element.changed: element", element)
          this.handleFormData(element);
          this.changeFormData(element)
        });
        this.modeler.on("element.click", e => {
          const {element} = e;
          console.log("click: element", element)
          this.handleFormData(element)
          if (element.type == this.modeler._definitions.rootElements[0].$type) {
            this.modifyConfigTab(element)
          } else {
            this.modifyConfigTab(element)
            this.changeFormData(element)
          }
        })
      },
      changeFormData(element){
        const _this = this;
        if(element.type === "bpmn:UserTask"){
          let _businessObject = element.businessObject;
          if(_businessObject.assignee){
            _this.formData.userType = "assignee";
            _this.formData.assignee = _businessObject.assignee;
          }
          if (_this.formData.assignee) {
            _this.formData.userType = "assignee"
          }
          if (_this.formData.candidateGroups) {
            _this.formData.userType = "candidateGroups"
          }
          if (_this.formData.candidateUsers) {
            _this.formData.userType = "candidateUsers"
          }
        }
      },
      isImplicitRoot(element) {
        return element.id === '__implicitroot';
      },
      modifyConfigTab(element) {
        let configTab = 'node'
        if (element !== undefined
            && element.type !== undefined
            && element.type==="bpmn:Process") {
          configTab = 'process'
        }
        if (element !== undefined
            && element.type !== undefined
            && element.type==='bpmn:StartEvent')  {
          configTab = 'start-node'
        }
        console.log("configTab:" + configTab)
        this.configTab = configTab
      },
      handleFormData(element) {
        if (!element.id) {
          return;
        }
        let businessObject = element.businessObject;
        this.formData = {
          type: element.type,
          id: businessObject.id,
          name: businessObject.name,
          userType: businessObject.$attrs.userType ? businessObject.$attrs.userType : businessObject.userType,
          assignee: businessObject.$attrs.assignee ? businessObject.$attrs.assignee : businessObject.assignee,
          candidateGroups: businessObject.$attrs.candidateGroups ? businessObject.$attrs.candidateGroups : businessObject.candidateGroups,
          candidateUsers: businessObject.$attrs.candidateUsers ? businessObject.$attrs.candidateUsers : businessObject.candidateUsers,
          sequenceFlow: businessObject.conditionExpression ? businessObject.conditionExpression.body : '',
          extensionElements: businessObject.extensionElements ? businessObject.extensionElements.values : null
        }
        console.log("formData",this.formData)
        this.nodeElement = element;
      },
      modifyFormData(data){
        this.formData.assignee = data.assignee;
        this.formData.userType = data.userType;
      }
    },
    components: {
      NodePropertyPanel, ProcessPropertyPanel, StartEventNodePropertyPanel
    }
  }
</script>

<style scoped>
  .el-main{
    padding: 0 !important;
  }

  /deep/.el-collapse .el-input__inner{
    height: 30px !important;
    line-height: 30px !important;
  }
  /deep/.el-collapse .el-form-item__content{
    height: 30px !important;
    line-height: 30px !important;
  }
  /deep/.el-collapse .el-form-item__label{
    height: 30px !important;
    line-height: 30px !important;
  }
  /deep/.el-collapse .el-form-item__label{
    font-size: 12px;
  }
  /deep/.el-collapse .el-form{
    margin-top: 10px;
  }
  /deep/.el-collapse .is-active{
    border-bottom: 1px solid #EBEEF5
  }
  .bpmn-panel {
    /*width: 350px;*/
    border: 1px solid #eeeeee;
    padding: 0 5px;
  }

  .el-header {
    border-bottom: solid 2px #e4e7ed;
    padding: 0;
  }

  .config-tab {
    height: 43px;
    line-height: 43px;
    display: inline-block;
    width: 50%;
    text-align: center;
    font-size: 14px;
    font-weight: 500;
    position: relative;
    cursor: pointer;
  }

  .config-tab.active {
    border-bottom: solid 2px #409EFF;
  }
</style>
