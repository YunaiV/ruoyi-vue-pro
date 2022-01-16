<template>
  <div class="app-container">
    <!-- TODO 审批信息 -->
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
    <!-- TODO 审批记录 -->
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
        this.historicTasksLoad = true;
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
