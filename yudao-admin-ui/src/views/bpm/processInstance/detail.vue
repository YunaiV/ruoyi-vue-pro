<template>
  <div class="app-container">
    <!-- 审批信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">审批任务【TODO】</span>
      </div>
      <el-col :span="16" :offset="6" >
        <el-form ref="form" :model="form" :rules="rules" label-width="80px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="审批建议" prop="comment">
                <el-input type="textarea" v-model="form.comment" placeholder="请输入审批建议" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
        <div style="margin-left: 10%; margin-bottom: 20px; font-size: 14px;">
          <el-button  icon="el-icon-edit-outline" type="success" size="mini" @click="handleComplete">通过</el-button>
          <el-button  icon="el-icon-circle-close" type="danger" size="mini" @click="handleReject">不通过</el-button>
          <el-button  icon="el-icon-edit-outline" type="primary" size="mini" @click="handleAssign">转办</el-button>
          <el-button  icon="el-icon-edit-outline" type="info" size="mini" @click="handleDelegate">委派</el-button>
          <el-button  icon="el-icon-refresh-left" type="warning" size="mini" @click="handleReturn">退回</el-button>
        </div>
      </el-col>
    </el-card>
    <!-- 申请信息 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <div slot="header" class="clearfix">
        <span class="el-icon-document">申请信息【{{ processInstance.name }}】</span>
      </div>
      <el-col :span="16" :offset="6">
        <div>
          <parser :key="new Date().getTime()" :form-conf="detailForm" @submit="submitForm" />
        </div>
      </el-col>
    </el-card>
    <el-card class="box-card" v-loading="historicTasksLoad">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">审批记录</span>
      </div>
      <el-col :span="16" :offset="4" >
        <div class="block">
          <el-timeline>
            <el-timeline-item v-for="(item, index) in historicTasks" :key="index"
                              :icon="getTimelineItemIcon(item)"
                              :type="getTimelineItemType(item)">
              <p style="font-weight: 700">任务：{{ item.name }}</p>
              <el-card :body-style="{ padding: '10px' }">
                <label v-if="item.assigneeUser" style="font-weight: normal; margin-right: 30px;">
                  审批人：{{ item.assigneeUser.nickname }}
                  <el-tag type="info" size="mini">{{ item.assigneeUser.deptName }}</el-tag>
                </label>
                <label style="font-weight: normal">创建时间：</label>
                <label style="color:#8a909c; font-weight: normal">{{ parseTime(item.createTime) }}</label>
                <label v-if="item.endTime" style="margin-left: 30px;font-weight: normal">审批时间：</label>
                <label v-if="item.endTime" style="color:#8a909c;font-weight: normal"> {{ parseTime(item.endTime) }}</label>
                <label v-if="item.durationInMillis" style="margin-left: 30px;font-weight: normal">耗时：</label>
                <label v-if="item.durationInMillis" style="color:#8a909c;font-weight: normal"> {{ getDateStar(item.durationInMillis) }} </label>
                <p v-if="item.comment">
                  <el-tag :type="getTimelineItemType(item)">{{ item.comment }}</el-tag>
                </p>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-col>
    </el-card>
    <!-- TODO 流程图 -->
    <el-card class="box-card" v-loading="processInstanceLoading">
      <div slot="header" class="clearfix">
        <span class="el-icon-picture-outline">流程图</span>
      </div>
      <my-process-viewer key="designer" v-model="bpmnXML" v-bind="bpmnControlForm" />
    </el-card>
  </div>
</template>

<script>
import {getProcessDefinitionBpmnXML, getProcessDefinitionList} from "@/api/bpm/definition";
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {getForm} from "@/api/bpm/form";
import {decodeFields} from "@/utils/formGenerator";
import Parser from '@/components/parser/Parser'
import {createProcessInstance, getMyProcessInstancePage, getProcessInstance} from "@/api/bpm/processInstance";
import {getHistoricTaskListByProcessInstanceId} from "@/api/bpm/task";
import {getDate} from "@/utils/dateUtils";

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
        prefix: "activiti"
      },

      // 审批记录
      historicTasksLoad: true,
      historicTasks: [],

      // 审批表单
      form: {},
      rules: {
        comment: [{ required: true, message: "审批建议不能为空", trigger: "blur" }],
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
        })

        // 加载流程图
        getProcessDefinitionBpmnXML(this.processInstance.processDefinition.id).then(response => {
          this.bpmnXML = response.data
        })

        // 取消加载中
        this.processInstanceLoading = false;
      });

      // 获得流程任务列表（审批记录）
      this.historicTasksLoad = true;
      getHistoricTaskListByProcessInstanceId(this.id).then(response => {
        this.historicTasks = response.data;
        // 排序，将未完成的排在前面，已完成的排在后面；
        this.historicTasks.sort((a, b) => {
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
        this.historicTasksLoad = false;
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
    /** 提交按钮 */
    submitForm(params) {
      if (!params) {
        return;
      }
      // 设置表单禁用
      const conf = params.conf;
      conf.disabled = true; // 表单禁用
      conf.formBtns = false; // 按钮隐藏

      // 提交表单，创建流程
      const variables = params.values;
      createProcessInstance({
        processDefinitionId: this.selectProcessInstance.id,
        variables: variables
      }).then(response => {
        this.msgSuccess("发起流程成功");
        // 关闭当前窗口
        this.$store.dispatch("tagsView/delView", this.$route);
        this.$router.go(-1);
      }).catch(() => {
        conf.disabled = false; // 表单开启
        conf.formBtns = true; // 按钮展示
      })
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
