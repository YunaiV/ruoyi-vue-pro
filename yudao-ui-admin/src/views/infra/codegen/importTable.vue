<template>
  <!-- 导入表 -->
  <el-dialog title="导入表" :visible.sync="visible" width="800px" top="5vh" append-to-body>
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true">
      <el-form-item label="数据源" prop="dataSourceConfigId">
        <el-select v-model="queryParams.dataSourceConfigId" placeholder="请选择数据源" clearable>
          <el-option v-for="config in dataSourceConfigs"
                     :key="config.id" :label="config.name" :value="config.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="表名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入表名称" clearable  @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="表描述" prop="comment">
        <el-input v-model="queryParams.comment" placeholder="请输入表描述" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>
    <el-row>
      <el-table v-loading="loading" @row-click="clickRow" ref="table" :data="dbTableList"
                @selection-change="handleSelectionChange" height="260px">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="表名称" :show-overflow-tooltip="true" />
        <el-table-column prop="comment" label="表描述" :show-overflow-tooltip="true" />
      </el-table>
    </el-row>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleImportTable">确 定</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getSchemaTableList, createCodegenList } from "@/api/infra/codegen";
import {getDataSourceConfigList} from "@/api/infra/dataSourceConfig";
export default {
  data() {
    return {
      // 遮罩层
      loading: false,
      // 遮罩层
      visible: false,
      // 选中数组值
      tables: [],
      // 总条数
      total: 0,
      // 表数据
      dbTableList: [],
      // 查询参数
      queryParams: {
        dataSourceConfigId: undefined,
        name: undefined,
        comment: undefined,
      },
      // 数据源列表
      dataSourceConfigs: [],
    };
  },
  methods: {
    // 显示弹框
    show() {
      this.visible = true;
      // 加载数据源
      getDataSourceConfigList().then(response => {
        this.dataSourceConfigs = response.data;
        this.queryParams.dataSourceConfigId = this.dataSourceConfigs[0].id;
        // 加载表列表
        this.getList();
      });
    },
    clickRow(row) {
      this.$refs.table.toggleRowSelection(row);
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.tables = selection.map(item => item.name);
    },
    // 查询表数据
    getList() {
      this.loading = true;
      getSchemaTableList(this.queryParams).then(res => {
        this.dbTableList = res.data;
      }).finally(() => {
        this.loading = false;
      });
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.dataSourceConfigId = this.dataSourceConfigs[0].id;
      this.handleQuery();
    },
    /** 导入按钮操作 */
    handleImportTable() {
      createCodegenList({
        dataSourceConfigId: this.queryParams.dataSourceConfigId,
        tableNames: this.tables
      }).then(res => {
        this.$modal.msgSuccess("导入成功");
        this.visible = false;
        this.$emit("ok");
      });
    }
  }
};
</script>
