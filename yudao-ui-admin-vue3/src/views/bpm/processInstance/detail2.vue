<template>
  <div class="app-container">



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
  </div>
</template>

<script>

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
    }
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
