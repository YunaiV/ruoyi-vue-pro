<template>
  <div class="app-container">

    <el-button
        icon="el-icon-circle-close"
        type="danger"
        size="mini"
        @click="handleAudit(item, false)"
    >不通过</el-button
    >
    <el-button
        icon="el-icon-edit-outline"
        type="primary"
        size="mini"
        @click="handleUpdateAssignee(item)"
    >转办</el-button
    >
    <el-button
        icon="el-icon-edit-outline"
        type="primary"
        size="mini"
        @click="handleDelegate(item)"
    >委派</el-button
    >
    <el-button
        icon="el-icon-refresh-left"
        type="warning"
        size="mini"
        @click="handleBack(item)"
    >退回</el-button
    >

    <!-- 高亮流程图 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">流程图</span>
      </div>
      <my-process-viewer
        key="designer"
        v-model="bpmnXML"
        v-bind="bpmnControlForm"
        :activityData="activityList"
        :processInstanceData="processInstance"
        :taskData="tasks"
      />
    </el-card>

    <!-- 对话框(转派审批人) -->
    <el-dialog title="转派审批人" :visible.sync="updateAssignee.open" width="500px" append-to-body>
      <el-form
        ref="updateAssigneeForm"
        :model="updateAssignee.form"
        :rules="updateAssignee.rules"
        label-width="110px"
      >
        <el-form-item label="新审批人" prop="assigneeUserId">
          <el-select v-model="updateAssignee.form.assigneeUserId" clearable style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="parseInt(item.id)"
              :label="item.nickname"
              :value="parseInt(item.id)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitUpdateAssigneeForm">确 定</el-button>
        <el-button @click="cancelUpdateAssigneeForm">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getProcessDefinitionBpmnXML } from "@/api/bpm/definition"
import { DICT_TYPE, getDictOptions } from "@/utils/dict"
import store from "@/store"
import { decodeFields } from "@/utils/formGenerator"
import Parser from '@/components/parser/Parser'
import { getProcessInstanceApi } from "@/api/bpm/processInstance"
import { approveTask, getTaskListByProcessInstanceId, rejectTask, updateTaskAssignee } from "@/api/bpm/task"
import { getListSimpleUsersApi } from "@/api/system/user"
import { getActivityList } from "@/api/bpm/activity"

// 流程实例的详情页，可用于审批
export default {
  name: "ProcessInstanceDetail",
  data () {
    return {

      // BPMN 数据
      bpmnXML: null,
      bpmnControlForm: {
        prefix: "flowable"
      },
      activityList: [],

      // 转派审批人
      userOptions: [],
      updateAssignee: {
        open: false,
        form: {
          assigneeUserId: undefined,
        },
        rules: {
          assigneeUserId: [{ required: true, message: "新审批人不能为空", trigger: "change" }],
        }
      },

      // 数据字典
      categoryDictDatas: getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY),
    }
  },
  created () {
    this.id = this.$route.query.id
    if (!this.id) {
      this.$message.error('未传递 id 参数，无法查看流程信息')
      return
    }
    this.getDetail()

    // 获得用户列表
    this.userOptions = []
    getListSimpleUsersApi().then(response => {
      this.userOptions.push(...response.data)
    })
  },
  methods: {
    /** 获得流程实例 */
    getDetail () {
      // 获得流程实例相关
      this.processInstanceLoading = true
      getProcessInstanceApi(this.id).then(response => {
        // 加载流程图
        getProcessDefinitionBpmnXML(this.processInstance.processDefinition.id).then(response => {
          this.bpmnXML = response.data
        })

        // 加载活动列表
        getActivityList({
          processInstanceId: this.processInstance.id
        }).then(response => {
          this.activityList = response.data
        })
      })
    },


    /** 处理转派审批人 */
    handleUpdateAssignee (task) {
      // 设置表单
      this.resetUpdateAssigneeForm()
      this.updateAssignee.form.id = task.id
      // 设置为打开
      this.updateAssignee.open = true
    },
    /** 提交转派审批人 */
    submitUpdateAssigneeForm () {
      this.$refs['updateAssigneeForm'].validate(valid => {
        if (!valid) {
          return
        }
        updateTaskAssignee(this.updateAssignee.form).then(response => {
          this.$modal.msgSuccess("转派任务成功！")
          this.updateAssignee.open = false
          this.getDetail() // 获得最新详情
        })
      })
    },
    /** 取消转派审批人 */
    cancelUpdateAssigneeForm () {
      this.updateAssignee.open = false
      this.resetUpdateAssigneeForm()
    },
    /** 重置转派审批人 */
    resetUpdateAssigneeForm () {
      this.updateAssignee.form = {
        id: undefined,
        assigneeUserId: undefined,
      }
      this.resetForm("updateAssigneeForm")
    },
    /** 处理审批退回的操作 */
    handleDelegate (task) {
      this.$modal.msgError("暂不支持【委派】功能，可以使用【转派】替代！")
    },
    /** 处理审批退回的操作 */
    handleBack (task) {
      this.$modal.msgError("暂不支持【退回】功能！")
      // 可参考 http://blog.wya1.com/article/636697030/details/7296
      // const data = {
      //   id: task.id,
      //   assigneeUserId: 1
      // }
      // backTask(data).then(response => {
      //   this.$modal.msgSuccess("回退成功！");
      //   this.getDetail(); // 获得最新详情
      // });
    }
  }
};
</script>

<style lang="scss">
.my-process-designer {
  height: calc(100vh - 200px);
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}
</style>
