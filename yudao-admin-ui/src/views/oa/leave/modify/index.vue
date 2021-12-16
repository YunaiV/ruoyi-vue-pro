<template>
  <div class="app-container">
    <el-tabs type="border-card">
      <el-tab-pane label="任务处理">
        <el-form ref="form" :model="form"  :rules="rules" label-width="100px">
          <el-row>
            <el-col :span="15">
              <el-form-item label="是否调整申请" prop="reApply">
                <el-select v-model="reApplySelect"  placeholder="是否调整申请" v-on:change="reApplyChange">
                  <el-option
                    v-for="dict in reApplyData"
                    :key="parseInt(dict.value)"
                    :label="dict.label"
                    :value="parseInt(dict.value)"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20" v-show="modifyShow">
            <el-col :span="8"><el-form-item label="申请人" >{{form.userId}}</el-form-item></el-col>
            <el-col :span="8">
              <el-form-item label="申请时间" prop="applyTime">{{ parseTime(form.applyTime) }}</el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="20" v-show="modifyShow">
            <el-col :span="8">
              <el-form-item label="开始时间" prop="startTime">
                <el-date-picker clearable size="small" v-model="form.startTime" type="date" value-format="timestamp" placeholder="选择开始时间" />
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="结束时间" prop="endTime">
                <el-date-picker clearable size="small" v-model="form.endTime" type="date" value-format="timestamp" placeholder="选择结束时间" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row v-show="modifyShow" >
            <el-col :span="8">
              <el-form-item label="请假类型" prop="leaveType">
                <el-select v-model="form.leaveType" placeholder="请选择">
                  <el-option
                    v-for="dict in leaveTypeDictData"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row v-show="modifyShow">
            <el-col :span="15">
              <el-form-item label="原因" prop="reason">
                <el-input
                  type="textarea"
                  :rows="3"
                  v-model="form.reason"
                  placeholder="请输入原因" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item>
            <el-button type="primary" @click="submitForm">确 定</el-button>
          </el-form-item>
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
import { getLeave,updateLeave } from "@/api/oa/leave"
import { taskSteps } from "@/api/oa/todo"
import { getDictDataLabel, getDictDatas, DICT_TYPE } from '@/utils/dict'
export default {
  name: "HrApproveLeave",
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
      modifyShow: true,
      reApplySelect: 1,
      reApplyData: [
        {
          value: 0,
          label: '取消申请'
        },
        {
          value: 1,
          label: '继续申请'
        }
      ],
      statusFormat(row, column) {
        return getDictDataLabel(DICT_TYPE.OA_LEAVE_STATUS, row.status)
      },
      leaveTypeDictData: getDictDatas(DICT_TYPE.OA_LEAVE_TYPE),
      leaveStatusData: getDictDatas(DICT_TYPE.OA_LEAVE_STATUS)
    };
  },
  mounted() {
    const businessKey = this.$route.query.businessKey;
    const taskId = this.$route.query.taskId;

    this.getForm(businessKey,taskId);
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
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        if (this.reApplySelect === 1) {
          this.form.variables["reApply"] = true;
          this.form.comment = '调整请假申请';
        }
        if (this.reApplySelect === 0) {
          this.form.variables["reApply"] = false;
          this.form.comment = '取消请假申请';
        }
        updateLeave(this.form).then(response => {
          this.msgSuccess("修改成功");
          this.$store.dispatch('tagsView/delView', this.$route).then(({ visitedViews }) => {
            //if (this.isActive(this.$route)) {
            this.$router.push({path: '/oa/todo'})
            //}
          })
        });
      });
    },
    getForm(id, taskId){
      getLeave(id).then(response => {
        this.form = response.data;
        this.form.taskId = taskId;
        this.form.variables = {};
      });
      const data = {
        taskId : taskId,
        businessKey: id,
      }
      taskSteps(data).then(response => {
        this.handleTask = response.data;

      });
    },
    reApplyChange(){
      if (this.reApplySelect === 1) {
        this.modifyShow = true;
      }
      if (this.reApplySelect === 0) {
        this.modifyShow = false;
      }
    }

  }
};
</script>
