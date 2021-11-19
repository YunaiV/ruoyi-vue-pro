<template>
  <div>
    <!-- 开始节点 -->
    <el-collapse v-if="localFormData.type=='bpmn:StartEvent'"  v-model="activeNames" accordion>
      <el-collapse-item name="1">
      <template slot="title">
        <div class="title">
          <i class="header-icon el-icon-setting"></i>
          <span>基本设置</span>
        </div>
      </template>
      <div>
        <el-form label-position="right" label-width="70px">
          <el-form-item label="节点类型">
            <el-input v-model="localFormData.type"  disabled></el-input>
          </el-form-item>
          <el-form-item label="ID">
            <el-input v-model="localFormData.id" @input="updateId"></el-input>
          </el-form-item>
          <el-form-item label="名称">
            <el-input v-model="localFormData.name " @input="updateName"></el-input>
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

    </el-collapse>
    <EventListenerDialog @commitEventForm="commitEventForm"
                         :dialogFormVisibleBool="showEventDialog"
                         :formData="eventFormData" :nodeElement="nodeElement" :modeler="modeler" :listenerTable="listenerTable"></EventListenerDialog>
  </div>
</template>

<script>
import EventListenerDialog from "./dialog/EventListenerDialog"

export default {
    name: "NodePropertyPanel",
    data() {
      return {
        activeNames: ['1'],
        input3: 1,
        listenerTable: [],
        showEventDialog: false,
        eventFormData: {},
        bpmnData: {
          assignees: [{
            value: "${assignee}",
            label: "表达式"
          }, {
            value: "1001",
            label: "张三"
          }, {
            value: "1002",
            label: "李四"
          }, {
            value: "1003",
            label: "王五"
          }],
          candidateUsers:[{
            value: "1001",
            label: "张三"
          }, {
            value: "1002",
            label: "李四"
          }, {
            value: "1003",
            label: "王五"
          }],
          roles: [
            {
              value: "manager",
              label: "经理"
            },
            {
              value: "personnel",
              label: "人事"
            },
            {
              value: "charge",
              label: "主管"
            }
          ]
        }
      }
    },
    components: {
      EventListenerDialog
    },
    props: {
      modeler: {
        type: Object,
        required: true
      },
      nodeElement: {
        type: Object,
        required: true
      },
      formData:{
        type: Object,
        required: true
      }
    },
    computed:{
      localFormData:{
        get(){
          return this.formData
        }
      }
    },
    watch:{
      nodeElement:{
        handler(){
          if(this.nodeElement.type==="bpmn:StartEvent"){
            this.updateName("开始");
          }
          if(this.nodeElement.type==="bpmn:EndEvent"){
            this.updateName("结束");
          }
        }
      }
    },
    mounted() {
      const that = this
      if (this.nodeElement.businessObject
          && this.nodeElement.businessObject.extensionElements
          && this.nodeElement.businessObject.extensionElements.values) {
        // 根据xml 组合当前节点的监听数据
        that.listenerTable = this.nodeElement.businessObject.extensionElements.values.filter(
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
      updateProperties(properties){
        console.log(this.nodeElement)
        console.log(properties)
        this.modeler.get("modeling").updateProperties(this.nodeElement, properties);
      },
      updateId(name) {
        this.updateProperties({id: name});
      },
      updateName(name) {
        this.updateProperties({name: name});
      },
      changeUserType() {
      },
      updateSequenceFlow(val){
        let newCondition = this.modeler.get("moddle").create('bpmn:FormalExpression', {
          body: val
        });
        this.updateProperties({conditionExpression:newCondition});
      },
      addUser(properties){
        this.updateProperties(properties);
        Object.assign(properties, {
          userType: Object.keys(properties)[0]
        });
        console.log(properties)
        this.$emit('modifyFormData',properties);
      },
      showEventDialogMethod(){
        this.showEventDialog = true;
        this.eventFormData= {
          event:"start",
          type:"class"
        }
      },
      deleteEvent(index){
        const data = this.listenerTable[index]
        if (this.nodeElement.businessObject
            && this.nodeElement.businessObject.extensionElements
            && this.nodeElement.businessObject.extensionElements.values) {
          // 排除全局监听类型 并且class为删除的class的数据。
          const filterArr = this.nodeElement.businessObject.extensionElements.values.filter(
              (item) =>  !(item.$type === 'activiti:ExecutionListener' && item.event === data.event && data.class === item[data.type])
          )
          // 刷新数据
          let extensionElements = this.modeler.get("bpmnFactory").create("bpmn:ExtensionElements", {values: filterArr});
          this.modeler.get("modeling").updateProperties(this.nodeElement, {extensionElements});
        }
        this.listenerTable.splice(index, 1);
      },
      commitEventForm(from){
        this.showEventDialog = false
        if (from != null) {
          this.listenerTable.push(from)
        }

      }
    }
  }
</script>

<style scoped>
.bpmnclass .title span{
  font-weight: bold;
  margin-left: 5px;
}
</style>
