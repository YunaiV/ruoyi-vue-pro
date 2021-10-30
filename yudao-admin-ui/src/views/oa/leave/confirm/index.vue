<template>
  <div class="app-container">
    <el-tabs type="border-card">
      <el-tab-pane label="任务处理">
        <el-form ref="form" :model="form"  label-width="80px">
          <el-row :gutter="20">
            <el-col :span="6"><el-form-item label="申请人" >{{form.userId}}</el-form-item></el-col>
            <el-col :span="6">
              <el-form-item label="请假类型" prop="leaveType">
                {{ getDictDataLabel(DICT_TYPE.OA_LEAVE_TYPE, form.leaveType) }}
              </el-form-item>
            </el-col>
            <el-col :span="6"><el-form-item label="原因" prop="reason">{{form.reason}}</el-form-item></el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6"><el-form-item label="开始时间" >{{ parseTime(form.startTime) }}</el-form-item></el-col>
            <el-col :span="6"><el-form-item label="结束时间" prop="endTime">{{ parseTime(form.endTime) }}</el-form-item></el-col>
            <el-form-item label="申请时间" prop="applyTime">{{ parseTime(form.applyTime) }}</el-form-item>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="6"><el-button type="primary" @click="submitForm">确 定</el-button></el-col>
          </el-row>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="历史跟踪">
        <el-steps :active="stepActive" simple finish-status="success">
          <el-step :title="stepTitle(item)" icon="el-icon-edit" v-for="(item) in handleTask.historyTask" ></el-step>
        </el-steps>
        <br/>
        <el-steps direction="vertical" :active="stepActive"  space="65px">
          <el-step :title="stepTitle(item)" :description="stepDes(item)" v-for="(item) in handleTask.historyTask" ></el-step>
        </el-steps>
      </el-tab-pane>
      <el-tab-pane label="流程图">流程图-TODO</el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getLeave } from "@/api/oa/leave"
import { completeTask,taskSteps } from "@/api/oa/todo";
import { getDictDataLabel, getDictDatas, DICT_TYPE } from '@/utils/dict'
export default {
  name: "ConfirmLeave",
  components: {
  },
  data() {
    return {
      // 表单参数
      form: {},
      // 表单校验
      rules: {
      },
      handleTask: {
        historyTask:[]
      },
      leaveApprove: {
        variables: {},
        taskId: "",
        comment: ""
      },
      statusFormat(row, column) {
        return getDictDataLabel(DICT_TYPE.OA_LEAVE_STATUS, row.status)
      },
      leaveTypeDictData: getDictDatas(DICT_TYPE.OA_LEAVE_TYPE),
      leaveStatusData: getDictDatas(DICT_TYPE.OA_LEAVE_STATUS)
    };
  },
  created() {
    const businessKey = this.$route.query.businessKey;
    const taskId = this.$route.query.taskId;
    this.leaveApprove.taskId = taskId;
    this.getForm(businessKey);
  },
  computed:{
    stepActive: function () {
      let idx = 0;
      for(let i=0; i<this.handleTask.historyTask.length; i++){
        if(this.handleTask.historyTask[i].status === 1){
          idx= idx+1;
        }else{
          break;
        }
      }
      return idx;
    },
    stepTitle() {
      return function (item) {
        let name = item.stepName;
        if (item.status === 1) {
          name += '(已完成)'
        }
        if (item.status === 0) {
          name += '(进行中)'
        }
        return name;
      }
    },
    stepDes(){
      return function (item) {
        let desc = "";
        if (item.status === 1) {
          desc+="审批人：["+ item.assignee +"]    审批意见: [" + item.comment + "]   审批时间: " + this.parseTime(item.endTime);
        }
        return desc;
      }
    }
  },
  methods: {
    /** 提交按钮 */
    submitForm() {
      completeTask(this.leaveApprove).then(response => {
        this.msgSuccess("执行任务成功");
        this.$store.dispatch('tagsView/delView', this.$route).then(({ visitedViews }) => {
          //if (this.isActive(this.$route)) {
          this.$router.push({path: '/oa/todo'})
          //}
        })
      })
    },
    getForm(id){
      getLeave(id).then(response => {
        this.form = response.data;
      });
      const data = {
        taskId : this.leaveApprove.taskId,
        businessKey: id,
      }
      taskSteps(data).then(response => {
        this.handleTask = response.data;

      });

    }
  }
};
</script>
