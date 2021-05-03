<template>
  <div class="app-container">
    <!-- 操作工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="表名称" prop="tableName">
        <el-input
          v-model="queryParams.tableName"
          placeholder="请输入表名称"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="表描述" prop="tableComment">
        <el-input
          v-model="queryParams.tableComment"
          placeholder="请输入表描述"
          clearable
          size="small"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker
          v-model="dateRange"
          size="small"
          style="width: 240px"
          value-format="yyyy-MM-dd"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload" size="mini" @click="openImportTable"
                   v-hasPermi="['tool:codegen:create']">基于 DB 导入</el-button>
        <el-button type="info" plain icon="el-icon-upload" size="mini" @click="openImportSQL"
                   v-hasPermi="['tool:codegen:create']">基于 SQL 导入</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="tableList">
      <el-table-column label="表名称" align="center" prop="tableName" :show-overflow-tooltip="true" width="200"/>
      <el-table-column label="表描述" align="center" prop="tableComment" :show-overflow-tooltip="true" width="120"/>
      <el-table-column label="实体" align="center" prop="className" :show-overflow-tooltip="true" width="200"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="createTime" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="text" size="small" icon="el-icon-view" @click="handlePreview(scope.row)" v-hasPermi="['tool:codegen:preview']">预览</el-button>
          <el-button type="text" size="small" icon="el-icon-edit" @click="handleEditTable(scope.row)" v-hasPermi="['tool:codegen:update']">编辑</el-button>
          <el-button type="text" size="small" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['tool:codegen:delete']">删除</el-button>
          <el-button type="text" size="small" icon="el-icon-refresh" @click="handleSynchDb(scope.row)" v-hasPermi="['tool:codegen:update']">同步</el-button>
          <el-button type="text" size="small" icon="el-icon-download" @click="handleGenTable(scope.row)" v-hasPermi="['tool:codegen:download']">生成代码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 预览界面 -->
    <el-dialog :title="preview.title" :visible.sync="preview.open" width="90%" top="5vh" append-to-body>

      <el-row>
        <el-col :span="7">
          <el-tree :data="preview.fileTree" :expand-on-click-node="false" default-expand-all
                   @node-click="handleNodeClick"/>
        </el-col>
        <el-col :span="17">
          <el-tabs v-model="preview.activeName">
            <el-tab-pane v-for="item in preview.data" :label="item.filePath.substring(item.filePath.lastIndexOf('/') + 1)"
                         :name="item.filePath" :key="item.filePath">
              <pre><code class="hljs" v-html="highlightedCode(item)"></code></pre>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 基于 DB 导入 -->
    <import-table ref="import" @ok="handleQuery" />

    <!-- 基于 SQL 导入 -->
    <el-dialog :title="importSQL.title" :visible.sync="importSQL.open" width="800px" append-to-body>
      <el-form ref="importSQLForm" :model="importSQL.form" :rules="importSQL.rules" label-width="120px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="建表 SQL 语句" prop="sql">
              <el-input v-model="importSQL.form.sql" type="textarea" rows="30" style="width: 650px;" placeholder="请输入建 SQL 语句" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitImportSQLForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCodegenTablePage, previewCodegen, downloadCodegen, deleteCodegen,
  syncCodegenFromDB, syncCodegenFromSQL, createCodegenListFromSQL } from "@/api/tool/codegen";

import importTable from "./importTable";
// 代码高亮插件
import hljs from "highlight.js/lib/highlight";
import "highlight.js/styles/github-gist.css";
import {SysCommonStatusEnum} from "@/utils/constants";
import {createTestDemo, updateTestDemo} from "@/api/tool/testDemo";
hljs.registerLanguage("java", require("highlight.js/lib/languages/java"));
hljs.registerLanguage("xml", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("html", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("vue", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("javascript", require("highlight.js/lib/languages/javascript"));
hljs.registerLanguage("sql", require("highlight.js/lib/languages/sql"));

export default {
  name: "Codegen",
  components: { importTable },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 唯一标识符
      uniqueId: "",
      // 选中表数组
      tableNames: [],
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 表数据
      tableList: [],
      // 日期范围
      dateRange: "",
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        tableName: undefined,
        tableComment: undefined
      },
      // 预览参数
      preview: {
        open: false,
        title: "代码预览",
        fileTree: [],
        data: {},
        activeName: "",
      },
      // 基于 SQL 导入
      importSQL: {
        open: false,
        title: "",
        form: {

        },
        rules: {
          sql: [{ required: true, message: "SQL 不能为空", trigger: "blur" }]
        }
      }
    };
  },
  created() {
    this.getList();
  },
  activated() {
    const time = this.$route.query.t;
    if (time != null && time !== this.uniqueId) {
      this.uniqueId = time;
      this.resetQuery();
    }
  },
  methods: {
    /** 查询表集合 */
    getList() {
      this.loading = true;
      getCodegenTablePage(this.addDateRange(this.queryParams, [
        this.dateRange[0] ? this.dateRange[0] + ' 00:00:00' : undefined,
        this.dateRange[1] ? this.dateRange[1] + ' 23:59:59' : undefined,
      ], 'CreateTime')).then(response => {
            this.tableList = response.data.list;
            this.total = response.data.total;
            this.loading = false;
          }
      );
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 生成代码操作 */
    handleGenTable(row) {
      downloadCodegen(row.id).then(response => {
        this.downloadZip(response, 'codegen-' + row.tableName + '.zip');
      })
    },
    /** 同步数据库操作 */
    handleSynchDb(row) {
      // 基于 SQL 同步
      if (row.importType === 2) {
        this.importSQL.open = true;
        this.importSQL.form.tableId = row.id;
        return;
      }
      // 基于 DB 同步
      const tableName = row.tableName;
      this.$confirm('确认要强制同步"' + tableName + '"表结构吗？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
          return syncCodegenFromDB(row.id);
      }).then(() => {
          this.msgSuccess("同步成功");
      })
    },
    /** 打开导入表弹窗 */
    openImportTable() {
      this.$refs.import.show();
    },
    /** 打开 SQL 导入的弹窗 **/
    openImportSQL() {
      this.importSQL.open = true;
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 预览按钮 */
    handlePreview(row) {
      previewCodegen(row.id).then(response => {
        this.preview.data = response.data;
        let files = this.handleFiles(response.data);
        console.log(files)
        this.preview.fileTree = this.handleTree(files, "id", "parentId", "children",
            "/"); // "/" 为根节点
        console.log(this.preview.fileTree)
        this.preview.activeName = response.data[0].filePath;
        this.preview.open = true;
      });
    },
    /** 高亮显示 */
    highlightedCode(item) {
      // const vmName = key.substring(key.lastIndexOf("/") + 1, key.indexOf(".vm"));
      // var language = vmName.substring(vmName.indexOf(".") + 1, vmName.length);
      var language = item.filePath.substring(item.filePath.lastIndexOf(".") + 1);
      const result = hljs.highlight(language, item.code || "", true);
      return result.value || '&nbsp;';
    },
    /** 生成 files 目录 **/
    handleFiles(datas) {
      let exists = {}; // key：file 的 id；value：true
      let files = [];
      // 遍历每个元素
      for (const data of datas) {
        let paths = data.filePath.split('/');
        let fullPath = ''; // 从头开始的路径，用于生成 id
        for (let i = 0; i < paths.length; i++) {
          // 已经添加大奥 files 中，则跳过
          let oldFullPath = fullPath;
          fullPath = fullPath.length === 0 ? paths[i] : fullPath + '/' + paths[i];
          if (exists[fullPath]) {
            continue;
          }
          // 添加到 files 中
          exists[fullPath] = true;
          files.push({
            id: fullPath,
            label: paths[i],
            parentId: oldFullPath || '/'  // "/" 为根节点
          });
        }
      }
      return files;
    },
    /** 节点单击事件 **/
    handleNodeClick(data) {
      this.preview.activeName = data.id;
    },
    /** 修改按钮操作 */
    handleEditTable(row) {
      const tableId = row.id;
      this.$router.push("/codegen/edit/" + tableId);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const tableIds = row.id;
      this.$confirm('是否确认删除表名称为"' + row.tableName + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
          return deleteCodegen(tableIds);
      }).then(() => {
          this.getList();
          this.msgSuccess("删除成功");
      })
    },
    // 取消按钮
    cancel() {
      this.importSQL.open = false;
      this.reset();
    },
    // 表单重置
    reset() {
      this.importSQL.form = {
        tableId: undefined,
        sql: undefined,
      };
      this.resetForm("importSQLForm");
    },
    // 提交 import SQL 表单
    submitImportSQLForm() {
      this.$refs["importSQLForm"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        let form = this.importSQL.form;
        if (form.tableId != null) {
          syncCodegenFromSQL(form.tableId, form.sql).then(response => {
            this.msgSuccess("同步成功");
            this.importSQL.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createCodegenListFromSQL(form).then(response => {
          this.msgSuccess("导入成功");
          this.importSQL.open = false;
          this.getList();
        });
      });
    }
  }
};
</script>
