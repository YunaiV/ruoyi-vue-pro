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
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['system:login-log:export']">导出</el-button>
        <el-button type="warning" icon="el-icon-download" size="mini" @click="openBpmn"
                   v-hasPermi="['system:login-log:export']">新建</el-button>
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
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
    <el-dialog title="新建流程" :visible.sync="showBpmnBool" :before-close="close" :fullscreen="true">
      <vue-bpmn product="activiti" @processSave="processSave"></vue-bpmn>
    </el-dialog>
  </div>
</template>

<script>
import { page } from "@/api/bpm/model";
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
    /** 导出按钮操作 */
    handleExport() {
      const queryParams = this.queryParams;
      this.$confirm('是否确认导出所有操作日志数据项?', "警告", {
          confirmButtonText: "确定",
          cancelButtonText: "取消",
          type: "warning"
        }).then(function() {
          return exportLoginLog(queryParams);
        }).then(response => {
          this.downloadExcel(response, '登录日志.xls');
        })
    },
    processSave() {
      console.log("processSave")
    },
    openBpmn() {
      this.showBpmnBool = true
    },
    close() {
      this.showBpmnBool = false
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

