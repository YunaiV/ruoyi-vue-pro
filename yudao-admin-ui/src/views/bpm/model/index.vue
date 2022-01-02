<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="流程标识" prop="key">
        <el-input v-model="queryParams.key" placeholder="请输入流程标识" clearable style="width: 240px;" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="流程名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入流程名称" clearable style="width: 240px;" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="流程分类" prop="category">
        <el-select v-model="queryParams.category" placeholder="流程分类" clearable size="small" style="width: 240px">
          <el-option v-for="dict in categoryDictDatas" :key="parseInt(dict.value)" :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="cyan" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['infra:config:create']">新建流程</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="流程标识" align="center" prop="key" />
      <el-table-column label="流程名称" align="center" prop="name" width="200">
        <template slot-scope="scope">
          <el-button type="text" @click="handleBpmnDetail(scope.row)">
            <span>{{ scope.row.name }}</span>
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="流程分类" align="center" prop="category" width="100">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.BPM_MODEL_CATEGORY, scope.row.category) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="表单信息" align="center" prop="formId">
        <template slot-scope="scope">
          <el-button v-if="scope.row.formId" type="text" @click="handleFormDetail(scope.row)">
            <span>{{ scope.row.formName }}</span>
          </el-button>
          <label v-else>暂无表单</label>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最新部署的流程定义" align="center">
        <el-table-column label="流程版本" align="center" prop="processDefinition.version" width="80">
          <template slot-scope="scope">
            <el-tag size="medium" v-if="scope.row.processDefinition">v{{ scope.row.processDefinition.version }}</el-tag>
            <el-tag size="medium" type="warning" v-else>未部署</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="激活状态" align="center" prop="processDefinition.version" width="80">
          <template slot-scope="scope">
            <el-switch v-if="scope.row.processDefinition" v-model="scope.row.processDefinition.suspensionState"
                       :active-value="1" :inactive-value="2" @change="handleStatusChange(scope.row)" />
          </template>
        </el-table-column>
        <el-table-column label="部署时间" align="center" prop="createTime" width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.processDefinition">{{ parseTime(scope.row.processDefinition.deploymentTime) }}</span>
          </template>
        </el-table-column>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-setting" @click="handleUpdate(scope.row)">设计流程</el-button>
          <el-button size="mini" type="text" icon="el-icon-thumb" @click="handleDeploy(scope.row)">发布流程</el-button>
          <el-button size="mini" type="text" icon="el-icon-ice-cream-round" @click="handleDefinitionList(scope.row)">流程定义</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 流程表单配置详情 -->
    <el-dialog title="表单详情" :visible.sync="detailOpen" width="50%" append-to-body>
      <parser :key="new Date().getTime()" :form-conf="detailForm" />
    </el-dialog>

    <!-- 流程模型图的预览 -->
    <el-dialog title="流程图" :visible.sync="showBpmnOpen" width="80%" append-to-body>
      <my-process-viewer key="designer" v-model="bpmnXML" v-bind="bpmnControlForm" />
    </el-dialog>
  </div>
</template>

<script>
import {deleteModel, deployModel, getModelPage, getModel} from "@/api/bpm/model";
import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import {getForm} from "@/api/bpm/form";
import {decodeFields} from "@/utils/formGenerator";
import Parser from '@/components/parser/Parser'

export default {
  name: "model",
  components: {
    Parser
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10
      },

      // BPMN 数据
      showBpmnOpen: false,
      bpmnXML: null,
      bpmnControlForm: {
        prefix: "activiti"
      },

      // 流程表单详情
      detailOpen: false,
      detailForm: {
        fields: []
      },

      // 数据字典
      categoryDictDatas: getDictDatas(DICT_TYPE.BPM_MODEL_CATEGORY),
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询流程模型列表 */
    getList() {
      this.loading = true;
      getModelPage(this.queryParams).then(response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        }
      );
    },
    // 表单重置
    reset() {
      this.bpmnData = {}
      this.bpmnXML = ""
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.$router.push({
        path:"/bpm/manager/model/edit"
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.$router.push({
        path:"/bpm/manager/model/edit",
        query:{
          modelId: row.id
        }
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const that = this;
      this.$confirm('是否删除该流程！！', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        deleteModel(row.id).then(response => {
          that.getList();
          that.msgSuccess("删除成功");
        })
      })
    },
    /** 部署按钮操作 */
    handleDeploy(row) {
      const that = this;
      this.$confirm('是否部署该流程！！', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "success"
      }).then(function() {
        deployModel(row.id).then(response => {
          that.getList();
          that.msgSuccess("部署成功");
        })
      })
    },
    /** 流程表单的详情按钮操作 */
    handleFormDetail(row) {
      getForm(row.formId).then(response => {
        // 设置值
        const data = response.data
        this.detailForm = {
          ...JSON.parse(data.conf),
          fields: decodeFields(data.fields)
        }
        // 弹窗打开
        this.detailOpen = true
      })
    },
    /** 流程图的详情按钮操作 */
    handleBpmnDetail(row) {
      getModel(row.id).then(response => {
        this.bpmnXML = response.data.bpmnXml
        // 弹窗打开
        this.showBpmnOpen = true
      })
    },
    /** 跳转流程定义的列表 */
    handleDefinitionList(row) {
      this.$router.push({
        path:"/bpm/manager/definition",
        query:{
          key: row.key
        }
      });
    }
  }
};
</script>

<style lang="scss">

.my-process-designer {
  height: calc(100vh - 200px);
}

</style>
