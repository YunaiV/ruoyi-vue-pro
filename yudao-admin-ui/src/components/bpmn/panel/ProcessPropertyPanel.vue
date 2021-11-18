<template>
  <div>
    <el-collapse v-model="activeNames" accordion>
      <el-collapse-item name="1">
        <template slot="title">
          <div class="title">
            <i class="header-icon el-icon-setting"></i>
            <span>基本设置</span>
          </div>
        </template>
        <div>
          <el-form label-position="right" label-width="70px">
            <el-form-item label="流程ID">
              <el-input v-model="localProcessData.key" @input="updateId"></el-input>
            </el-form-item>
            <el-form-item label="流程名称">
              <el-input v-model="localProcessData.name" @input="updateName"></el-input>
            </el-form-item>
            <el-form-item label="流程描述">
              <el-input v-model="localProcessData.description" @input="updateDesc"></el-input>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
      <el-collapse-item name="2">
        <template slot="title">
          <div class="title">
            <i class="header-icon el-icon-setting"></i>
            <span>执行监听</span>
          </div>
        </template>
        <div>
          <div style="margin: 10px 3%;float: right">
            <el-button size="mini" plain @click="showEventDialogMethod">添加</el-button>
          </div>
          <el-table
              border :data="listenerTable"
              style="width: 93%;margin: 0 auto">
            <el-table-column align="center" prop="event"
                             label="事件">
            </el-table-column>
            <el-table-column align="center" prop="type" :show-overflow-tooltip="true"
                             label="类型">
            </el-table-column>
            <el-table-column align="center" prop="class" :show-overflow-tooltip="true"
                             label="实现">
            </el-table-column>
            <el-table-column align="center"
                             label="操作">
              <template slot-scope="scope">
                <div>
                  <i class="el-icon-delete" @click="deleteEvent(scope.$index)"></i>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-collapse-item>
      <el-collapse-item name="3">
        <template slot="title">
          <div class="title">
            <i class="header-icon el-icon-setting"></i>
            <span>全局监听</span>
          </div>
        </template>
        <div>
          <div style="margin: 10px 3%;float: right">
            <el-button size="mini"  plain @click="showGlobalDialogMethod">添加</el-button>
          </div>
          <el-table
              border :data="globalFormTable"
              style="width: 93%;margin: 0 auto">
            <el-table-column align="center" prop="class" :show-overflow-tooltip="true"
                             label="值">
            </el-table-column>
            <el-table-column align="center"
                             label="操作">
              <template slot-scope="scope">
                <div>
                  <i class="el-icon-delete" @click="deleteGlobalEvent(scope.$index)"></i>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-collapse-item>
    </el-collapse>
    <GlobalEventListenerDialog @commitGlobalForm="commitGlobalForm"
                               :dialogFormVisibleBool="showGlobalDialog"
                               :formData="globalFormData" :element="element" :modeler="modeler" :globalFormTable="globalFormTable"></GlobalEventListenerDialog>
    <EventListenerDialog @commitEventForm="commitEventForm"
                               :dialogFormVisibleBool="showEventDialog"
                               :formData="eventFormData" :nodeElement="element" :modeler="modeler" :listenerTable="listenerTable"></EventListenerDialog>
  </div>
</template>

<script>
  import GlobalEventListenerDialog from "./dialog/GlobalEventListenerDialog"
  import EventListenerDialog from "./dialog/EventListenerDialog"
  export default {
    name: "ProcessProperty",
    data() {
      return {
        activeNames: ['1'],
        showGlobalDialog: false,
        showEventDialog: false,
        listenerTable: [

        ],
        globalFormTable: [

        ],
        globalFormData: {
          type:"start"
        },
        eventFormData: {
          event:"class",
          type:"start"
        },
        localProcessData:this.processData
      }
    },
    components: {
      GlobalEventListenerDialog,EventListenerDialog
    },
    props: {
      processData: {
        type: Object,
        required: true
      },
      modeler: {
        type: Object,
        required: true
      },
      element: {
        type: Object,
        required: true
      }
    },
    mounted() {
      var that = this
      if (this.element.businessObject
          && this.element.businessObject.extensionElements
          && this.element.businessObject.extensionElements.values) {
        that.globalFormTable = this.element.businessObject.extensionElements.values.filter(
            (item) => item.$type === 'activiti:EventListener'
        )
        that.listenerTable = this.element.businessObject.extensionElements.values.filter(
            (item) => {
              if (item.$type === 'activiti:ExecutionListener') {
                item.type = Object.keys(item)[1]
                item.class = item[Object.keys(item)[1]]

                return true;
              }
            }
        )
      }
    },
    methods: {
      deleteGlobalEvent(index){
        const data = this.globalFormTable[index]
        if (this.element.businessObject
            && this.element.businessObject.extensionElements
            && this.element.businessObject.extensionElements.values) {
          // 排除全局监听类型 并且class为删除的class的数据。
          const filterArr = this.element.businessObject.extensionElements.values.filter(
              (item) =>  !(item.$type === 'activiti:EventListener' && item.class === data.class)
          )
          // 刷新数据
          let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements", {values: filterArr});
          this.modeler.get("modeling").updateProperties(this.element, {extensionElements});
        }
        this.globalFormTable.splice(index, 1);
      },
      deleteEvent(index){
        const data = this.listenerTable[index]
        if (this.element.businessObject
            && this.element.businessObject.extensionElements
            && this.element.businessObject.extensionElements.values) {
          // 排除全局监听类型 并且class为删除的class的数据。
          const filterArr = this.element.businessObject.extensionElements.values.filter(
              (item) =>  !(item.$type === 'activiti:ExecutionListener' && item.event === data.event && data.class === item[data.type])
          )
          // 刷新数据
          let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements", {values: filterArr});
          this.modeler.get("modeling").updateProperties(this.element, {extensionElements});
        }
        this.listenerTable.splice(index, 1);
      },
      showGlobalDialogMethod(){
        this.showGlobalDialog = true;
        this.globalFormData={
          type:"class"
        }
      },
      showEventDialogMethod(){
        this.showEventDialog = true;
        this.eventFormData= {
          event:"start",
          type:"class"
        }
      },
      updateId(name) {
        this.modeler.get("modeling").updateProperties(this.element, {id: name});
      },
      updateName(name) {
        this.modeler.get("modeling").updateProperties(this.element, {name: name});
      },
      updateDesc(name) {
        let doc = this.modeler.get("bpmnFactory").create("bpmn:Documentation", {text: name});
        this.modeler.get("modeling").updateProperties(this.element, {documentation: [doc]});
        console.log( this.modeler.get("modeling"))
      },
      commitEventForm(from){
        this.showEventDialog = false
        if (from != null) {
          this.listenerTable.push(from)
        }

      },
      commitGlobalForm(from){
        this.showGlobalDialog = false
        if (from != null) {

          this.globalFormTable.push(from)

        }
      }
    }
  }
</script>

<style scoped>
.title span{
  font-weight: bold;
  margin-left: 5px;
}
.el-tooltip__popper{
  font-size: 14px;
  max-width:50%;
  backgroud: #68859a !important;  /*背景色　　!important优先级*/
}
</style>
