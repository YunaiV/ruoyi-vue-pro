<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="模型名字" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入模型名字" clearable style="width: 240px;" size="small"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="cyan" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          icon="el-icon-plus"
          size="mini"
          @click="openBpmn"
          v-hasPermi="['infra:config:create']"
        >新建流程</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>
    <el-table v-loading="loading" :data="list">
      <el-table-column label="ID" align="center" prop="id" />
      <el-table-column label="name" align="center" prop="metaInfo" >
        <template slot-scope="scope">
          <span>{{ scope.row.metaInfo ? JSON.parse(scope.row.metaInfo).name : "" }}</span>
        </template>
      </el-table-column>
      <el-table-column label="description" align="center" prop="metaInfo" >
        <template slot-scope="scope">
          <span>{{ JSON.parse(scope.row.metaInfo).description }}</span>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" >
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" >
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-setting" @click="change(scope.row)">设计流程</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="modelDelete(scope.row)">删除</el-button>
          <el-button size="mini" type="text" icon="el-icon-thumb" @click="modelDeploy(scope.row)">发布</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
    <el-dialog :visible.sync="showBpmnBool" :before-close="close" :fullscreen="true">
      <vue-bpmn v-if="showBpmnBool" product="activiti" @processSave="processSave" :bpmnXml="bpmnXML" :bpmnData="bpmnData" @beforeClose="close"></vue-bpmn>
    </el-dialog>
  </div>
</template>

<script>
import { page, exportBpmnXml, modelUpdate, modelSave, modelDelete, modelDeploy } from "@/api/bpm/model";
import VueBpmn from "@/components/bpmn/VueBpmn";

export default {
  name: "model",
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      showBpmnBool: false,
      // 总条数
      total: 0,
      // 表格数据
      list: [],
      bpmnXML: null,
      bpmnData: {},
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10
      }
    };
  },
  components: {VueBpmn},
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志列表 */
    getList() {
      this.loading = true;
      page(this.queryParams).then(response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        }
      );
    },
    // 登录状态字典翻译
    statusFormat(row, column) {
      return this.selectDictLabel(this.statusOptions, row.status);
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
    processSave(data) {
      const that = this;
      // 如果存在id 说明是修改
      if (data.id) {
        let postData = JSON.parse(data.metaInfo)
        postData.bpmnXml = data.bpmnXml
        postData.id = data.id
        postData.name = data.name
        postData.key = data.key
        postData.description = data.description
        modelUpdate(postData).then(response => {
          this.msgSuccess("保存成功");
        })
        this.showBpmnBool = false
        this.getList();
        return
      }
      modelSave(data).then(response => {
        that.bpmnData.id = response.data
        this.msgSuccess("保存成功");
      })

      this.showBpmnBool = false
      this.getList();
    },
    openBpmn() {
      this.bpmnData = {}
      this.bpmnXML = ""
      this.showBpmnBool = true
    },
    close() {
      this.showBpmnBool = false
      this.getList();
    },
    change(row) {
      const that = this;
      this.bpmnXML = ""
      this.bpmnData = {}
      exportBpmnXml({
        modelId: row.id
      }).then(response => {
        that.bpmnXML = response
        that.bpmnData = row
        that.showBpmnBool = true
      })
    },
    modelDelete(row) {
      const that = this;
      this.$confirm('是否删除该流程！！', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        modelDelete({
          modelId: row.id
        }).then(response => {
          that.getList();
          that.msgSuccess("删除成功");
        })
      })
    },
    modelDeploy(row) {
      const that = this;
      this.$confirm('是否部署该流程！！', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "success"
      }).then(function() {
        modelDeploy({
          modelId: row.id
        }).then(response => {
          that.getList();
          that.msgSuccess("部署成功");
        })
      })
    }
  }
};
</script>
<style>
.el-dialog > .el-dialog__body{
  margin: 0;
  border: 0;
}
.bpmn-viewer-header{
  background: white;
}
.v-modal{
  z-index: 2000!important;
}
</style>

