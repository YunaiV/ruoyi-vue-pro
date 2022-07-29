<template>
  <div class="app-container">
    <!-- 审批信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading" v-for="(item, index) in runningTasks" :key="index">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">审批任务【{{ item.name }}】</span>
      </div>
      <el-col :span="16" :offset="6" >
        <el-form :ref="'form' + index" :model="auditForms[index]" :rules="auditRule" label-width="100px">
          <el-form-item label="流程名" v-if="processInstance && processInstance.name">
            {{ processInstance.name }}
          </el-form-item>
          <el-form-item label="流程发起人" v-if="processInstance && processInstance.startUser">
            {{ processInstance.startUser.nickname }}
            <el-tag type="info" size="mini">{{ processInstance.startUser.deptName }}</el-tag>
          </el-form-item>
          <el-form-item label="审批建议" prop="reason">
            <el-input type="textarea" v-model="auditForms[index].reason" placeholder="请输入审批建议" />
          </el-form-item>
        </el-form>
        <div style="margin-left: 10%; margin-bottom: 20px; font-size: 14px;">
          <el-button  icon="el-icon-edit-outline" type="success" size="mini" @click="handleAudit(item, true)">通过</el-button>
          <el-button  icon="el-icon-circle-close" type="danger" size="mini" @click="handleAudit(item, false)">不通过</el-button>
          <el-button  icon="el-icon-edit-outline" type="primary" size="mini" @click="handleUpdateAssignee(item)">转办</el-button>
          <el-button icon="el-icon-edit-outline" type="primary" size="mini" @click="handleDelegate(item)">委派</el-button>
          <el-button icon="el-icon-refresh-left" type="warning" size="mini" @click="handleBack(item)">退回</el-button>
        </div>
      </el-col>
    </el-card>
    <!-- 申请信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <div slot="header" class="clearfix">
        <span class="el-icon-document">申请信息【{{ processInstance.name }}】</span>
      </div>
      <el-col v-if="this.processInstance.processDefinition && this.processInstance.processDefinition.formType === 10"
              :span="16" :offset="6">
        <div >
          <parser :key="new Date().getTime()" :form-conf="detailForm" />
        </div>
      </el-col>
      <div v-if="this.processInstance.processDefinition && this.processInstance.processDefinition.formType === 20">
        <router-link :to="this.processInstance.processDefinition.formCustomViewPath + '?id='
                          + this.processInstance.businessKey">
          <el-button type="primary">点击查看</el-button>
        </router-link>
      </div>
    </el-card>

    <!-- 审批记录 -->
    <el-card class="box-card" v-loading="tasksLoad">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">审批记录</span>
      </div>
      <el-col :span="16" :offset="4" >
        <div class="block">
          <el-timeline>
            <el-timeline-item v-for="(item, index) in tasks" :key="index"
                              :icon="getTimelineItemIcon(item)" :type="getTimelineItemType(item)">
              <p style="font-weight: 700">任务：{{ item.name }}</p>
              <el-card :body-style="{ padding: '10px' }">
                <label v-if="item.assigneeUser" style="font-weight: normal; margin-right: 30px;">
                  审批人：{{ item.assigneeUser.nickname }}
                  <el-tag type="info" size="mini">{{ item.assigneeUser.deptName }}</el-tag>
                </label>
                <label style="font-weight: normal" v-if="item.createTime">创建时间：</label>
                <label style="color:#8a909c; font-weight: normal">{{ parseTime(item.createTime) }}</label>
                <label v-if="item.endTime" style="margin-left: 30px;font-weight: normal">审批时间：</label>
                <label v-if="item.endTime" style="color:#8a909c;font-weight: normal"> {{ parseTime(item.endTime) }}</label>
                <label v-if="item.durationInMillis" style="margin-left: 30px;font-weight: normal">耗时：</label>
                <label v-if="item.durationInMillis" style="color:#8a909c;font-weight: normal"> {{ getDateStar(item.durationInMillis) }} </label>
                <p v-if="item.reason">
                  <el-tag :type="getTimelineItemType(item)">{{ item.reason }}</el-tag>
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-card>

    <!-- 高亮流程图 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">流程图</span>
      </div>
      <my-process-viewer key="designer" v-model="bpmnXML" v-bind="bpmnControlForm" :activityData="activityList"
            :processInstanceData="processInstance" :taskData="tasks" />
    </el-card>

    <!-- 对话框(转派审批人) -->
    <el-dialog title="转派审批人" :visible.sync="updateAssignee.open" width="500px" append-to-body>
      <el-form ref="updateAssigneeForm" :model="updateAssignee.form" :rules="updateAssignee.rules" label-width="110px">
        <el-form-item label="新审批人" prop="assigneeUserId">
          <el-select v-model="updateAssignee.form.assigneeUserId" clearable style="width: 100%">
            <el-option v-for="item in userOptions" :key="parseInt(item.id)" :label="item.nickname" :value="parseInt(item.id)" />
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
import {getProcessDefinitionBpmnXML} from "@/api/bpm/definition";
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import store from "@/store";
import {decodeFields} from "@/utils/formGenerator";
import Parser from '@/components/parser/Parser'
import {createProcessInstance, getProcessInstance} from "@/api/bpm/processInstance";
import {approveTask, getTaskListByProcessInstanceId, rejectTask, updateTaskAssignee,backTask} from "@/api/bpm/task";
import {getDate} from "@/utils/dateUtils";
import {listSimpleUsers} from "@/api/system/user";
import {getActivityList} from "@/api/bpm/activity";

// 流程实例的详情页，可用于审批
export default {
  name: "ProcessInstanceDetail",
  components: {
    Parser
  },
  data() {
    return {
      // 遮罩层
      processInstanceLoading: true,
      // 流程实例
      id: undefined, // 流程实例的编号
      processInstance: {},

      // 流程表单详情
      detailForm: {
        fields: []
      },

      // BPMN 数据
      bpmnXML: null,
      bpmnControlForm: {
        prefix: "flowable"
      },
      activityList: [],

      // 审批记录
      tasksLoad: true,
      tasks: [],

      // 审批表单
      runningTasks: [],
      auditForms: [],
      auditRule: {
        reason: [{ required: true, message: "审批建议不能为空", trigger: "blur" }],
      },

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
    };
  },
  created() {
    this.id = this.$route.query.id;
    if (!this.id) {
      this.$message.error('未传递 id 参数，无法查看流程信息');
      return;
    }
    this.getDetail();

    // 获得用户列表
    this.userOptions = [];
    listSimpleUsers().then(response => {
      this.userOptions.push(...response.data);
    });
  },
  methods: {
    /** 获得流程实例 */
    getDetail() {
      // 获得流程实例相关
      this.processInstanceLoading = true;
      getProcessInstance(this.id).then(response => {
        if (!response.data) {
          this.$message.error('查询不到流程信息！');
          return;
        }
        // 设置流程信息
        this.processInstance = response.data;

        // 设置表单信息
        if (this.processInstance.processDefinition.formType === 10) {
          this.detailForm = {
            ...JSON.parse(this.processInstance.processDefinition.formConf),
            disabled: true, // 表单禁用
            formBtns: false, // 按钮隐藏
            fields: decodeFields(this.processInstance.processDefinition.formFields)
          }
          // 设置表单的值
          this.detailForm.fields.forEach(item => {
            const val = this.processInstance.formVariables[item.__vModel__]
            if (val) {
              item.__config__.defaultValue = val
            }
          });
        }

        // 加载流程图
        getProcessDefinitionBpmnXML(this.processInstance.processDefinition.id).then(response => {
          this.bpmnXML = response.data
        });
        // 加载活动列表
        getActivityList({
          processInstanceId: this.processInstance.id
        }).then(response => {
          this.activityList = response.data;
        });

        // 取消加载中
        this.processInstanceLoading = false;
      });

      // 获得流程任务列表（审批记录）
      this.tasksLoad = true;
      this.runningTasks = [];
      this.auditForms = [];
      getTaskListByProcessInstanceId(this.id).then(response => {
        // 审批记录
        this.tasks = [];
        // 移除已取消的审批
        response.data.forEach(task => {
          if (task.result !== 4) {
            this.tasks.push(task);
          }
        });
        // 排序，将未完成的排在前面，已完成的排在后面；
        this.tasks.sort((a, b) => {
          // 有已完成的情况，按照完成时间倒序
          if (a.endTime && b.endTime) {
            return b.endTime - a.endTime;
          } else if (a.endTime) {
            return 1;
          } else if (b.endTime) {
            return -1;
            // 都是未完成，按照创建时间倒序
          } else {
            return b.createTime - a.createTime;
          }
        });

        // 需要审核的记录
        const userId = store.getters.userId;
        this.tasks.forEach(task => {
          if (task.result !== 1) { // 只有待处理才需要
            return;
          }
          if (!task.assigneeUser || task.assigneeUser.id !== userId) { // 自己不是处理人
            return;
          }
          this.runningTasks.push({...task});
          this.auditForms.push({
            reason: ''
          })
        });

        // 取消加载中
        this.tasksLoad = false;
      });
    },
    /** 处理选择流程的按钮操作 **/
    handleSelect(row) {
      // 设置选择的流程
      this.selectProcessInstance = row;

      // 流程表单
      if (row.formId) {
        // 设置对应的表单
        this.detailForm = {
          ...JSON.parse(row.formConf),
          fields: decodeFields(row.formFields)
        }
      } else if (row.formCustomCreatePath) {
        this.$router.push({ path: row.formCustomCreatePath});
        // 这里暂时无需加载流程图，因为跳出到另外个 Tab；
      }
    },
    getDateStar(ms) {
      return getDate(ms);
    },
    getTimelineItemIcon(item) {
      if (item.result === 1) {
        return 'el-icon-time';
      }
      if (item.result === 2) {
        return 'el-icon-check';
      }
      if (item.result === 3) {
        return 'el-icon-close';
      }
      if (item.result === 4) {
        return 'el-icon-remove-outline';
      }
      return '';
    },
    getTimelineItemType(item) {
      if (item.result === 1) {
        return 'primary';
      }
      if (item.result === 2) {
        return 'success';
      }
      if (item.result === 3) {
        return 'danger';
      }
      if (item.result === 4) {
        return 'info';
      }
      return '';
    },
    /** 处理审批通过和不通过的操作 */
    handleAudit(task, pass) {
      const index = this.runningTasks.indexOf(task);
      this.$refs['form' + index][0].validate(valid => {
        if (!valid) {
          return;
        }
        const data = {
          id: task.id,
          reason: this.auditForms[index].reason
        }
        if (pass) {
          approveTask(data).then(response => {
            this.$modal.msgSuccess("审批通过成功！");
            this.getDetail(); // 获得最新详情
          });
        } else {
          rejectTask(data).then(response => {
            this.$modal.msgSuccess("审批不通过成功！");
            this.getDetail(); // 获得最新详情
          });
        }
      });
    },
    /** 处理转派审批人 */
    handleUpdateAssignee(task) {
      // 设置表单
      this.resetUpdateAssigneeForm();
      this.updateAssignee.form.id = task.id;
      // 设置为打开
      this.updateAssignee.open = true;
    },
    /** 提交转派审批人 */
    submitUpdateAssigneeForm() {
      this.$refs['updateAssigneeForm'].validate(valid => {
        if (!valid) {
          return;
        }
        updateTaskAssignee(this.updateAssignee.form).then(response => {
          this.$modal.msgSuccess("转派任务成功！");
          this.updateAssignee.open = false;
          this.getDetail(); // 获得最新详情
        });
      });
    },
    /** 取消转派审批人 */
    cancelUpdateAssigneeForm() {
      this.updateAssignee.open = false;
      this.resetUpdateAssigneeForm();
    },
    /** 重置转派审批人 */
    resetUpdateAssigneeForm() {
      this.updateAssignee.form = {
        id: undefined,
        assigneeUserId: undefined,
      };
      this.resetForm("updateAssigneeForm");
    },
    /** 处理审批退回的操作 */
    handleDelegate(task) {
      this.$modal.msgError("暂不支持【委派】功能，可以使用【转派】替代！");
    },
    /** 处理审批退回的操作 */
    handleBack(task) {
      this.$modal.msgError("暂不支持【退回】功能！");
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
