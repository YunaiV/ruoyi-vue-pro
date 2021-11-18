<template>
  <div>
    <!-- 流程属性 -->
    <el-collapse  v-model="activeNames" accordion>
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
            <el-form-item v-if="localFormData.type==='bpmn:SequenceFlow'" label="分支条件">
              <el-input v-model="localFormData.sequenceFlow" @input="updateSequenceFlow"></el-input>
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
      <el-collapse-item name="3" v-if="localFormData.type=='bpmn:UserTask'" >
        <template slot="title">
          <div class="title">
            <i class="header-icon el-icon-setting"></i>
            <span>权限设置</span>
          </div>
        </template>
        <div>
          <el-form style="margin-top: 25px" label-position="right" label-width="90px">
            <el-form-item label="用户类型:">
              <el-select v-model="localFormData.userType" placeholder="请选择" @change="changeUserType" style="width: 90%">
                <el-option value="assignee" label="指定人员"></el-option>
                <el-option value="candidateUsers" label="候选人员"></el-option>
                <el-option value="candidateGroups" label="角色/岗位"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="指定人员" v-if="localFormData.userType === 'assignee'">
              <el-input placeholder="请输入内容" v-model="localFormData.assignee" @input="(value) => addUser({assignee: value})" style="width: 90%">
                <template slot="append">
                  <div class="icon-div">
                    <i class="el-icon-user" @click="showUserDialogMethod" />
<!--                    <div role="separator" class="ant-divider ant-divider-vertical"></div>-->
<!--                    <i class="el-icon-c-scale-to-original" />-->
                  </div>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="候选人员" v-else-if="localFormData.userType === 'candidateUsers'">
              <el-input placeholder="请输入内容" v-model="localFormData.candidateUsers" @input="(value) => addUser({candidateUsers: value})"  style="width: 90%">
                <template slot="append">
                  <div class="icon-div">
                    <i class="el-icon-user" @click="showUserDialogMethod" />
<!--                    <div role="separator" class="ant-divider ant-divider-vertical"></div>-->
<!--                    <i class="el-icon-c-scale-to-original" />-->
                  </div>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="角色/岗位" v-else-if="localFormData.userType === 'candidateGroups'">
              <el-input placeholder="请输入内容" v-model="localFormData.candidateGroups" @input="(value) => addUser({candidateGroups: value})"  style="width: 90%">
                <template slot="append">
                  <div class="icon-div">
                    <i class="el-icon-user" @click="showUserDialogMethod" />
<!--                    <div role="separator" class="ant-divider ant-divider-vertical"></div>-->
<!--                    <i class="el-icon-c-scale-to-original" />-->
                  </div>
                </template>
              </el-input>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
    </el-collapse>
    <EventListenerDialog @commitEventForm="commitEventForm"
                         :dialogFormVisibleBool="showEventDialog"
                         :formData="eventFormData" :nodeElement="nodeElement" :modeler="modeler" :listenerTable="listenerTable"></EventListenerDialog>
    <UserSelectDialog @commitUserForm="commitUserForm"
                      :type="localFormData.userType"
                      :dialogFormVisibleBool="showUserDialog"
                      :formData="userFormData"
                      :nodeElement="nodeElement"
                      :modeler="modeler"></UserSelectDialog>
  </div>
</template>

<script>
import EventListenerDialog from "./dialog/EventListenerDialog"
import UserSelectDialog from "./dialog/UserSelectDialog"
  export default {
    name: "NodePropertyPanel",
    data() {
      return {
        activeNames: ['1'],
        listenerTable: [],
        showEventDialog: false,
        showUserDialog: false,
        eventFormData: {},
        userFormData: {}
      }
    },
    components: {
      EventListenerDialog,UserSelectDialog
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
    methods: {
      updateProperties(properties){
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
      showUserDialogMethod(){
        this.showUserDialog = true;
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
          // 筛选原来数组里是不是有跟刚提交的数据一模一样的数据
          this.listenerTable.push(from)
        }
      },
      commitUserForm(from){
        this.showUserDialog = false
        if (from != null) {
          console.log(from)
          // 筛选原来数组里是不是有跟刚提交的数据一模一样的数据
          // this.listenerTable.push(from)
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
/deep/.el-select .el-input .el-select__caret{
  margin-top: 5px;
}
/deep/.el-input__icon{
  height: 20px !important;
  line-height: 20px !important;
}
/deep/.el-input-group__append{
  padding: 0 5px !important;
}
.icon-div{
  font-size: 1.2em;
  font-weight: bold !important;
  width: 100%;
}
.icon-div i{
  padding: 5px;
}
.icon-div i:hover{
  cursor:pointer;
}
.ant-divider-vertical {
  position: relative;
  top: -0.08em;
  display: inline-block;
  width: 2px;
  height: 0.9em;
  /*margin: 0 8px;*/
  vertical-align: middle;
}
.ant-divider {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
  color: rgba(0, 0, 0, 0.65);
  font-size: 14px;
  font-variant: tabular-nums;
  line-height: 1.5;
  list-style: none;
  /*font-feature-settings: "tnum";*/
  background: #e8e8e8;
}
</style>
