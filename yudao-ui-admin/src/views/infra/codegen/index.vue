<template>
  <div class="app-container">
    <doc-alert title="代码生成" url="https://doc.iocoder.cn/new-feature/" />
    <doc-alert title="单元测试" url="https://doc.iocoder.cn/unit-test/" />
    <!-- 操作工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="表名称" prop="tableName">
        <el-input v-model="queryParams.tableName" placeholder="请输入表名称" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="表描述" prop="tableComment">
        <el-input v-model="queryParams.tableComment" placeholder="请输入表描述" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工作栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload" size="mini" @click="openImportTable"
                   v-hasPermi="['infra:codegen:create']">导入</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="tableList">
      <el-table-column label="数据源" align="center" :formatter="dataSourceConfigNameFormat"/>
      <el-table-column label="表名称" align="center" prop="tableName" width="200"/>
      <el-table-column label="表描述" align="center" prop="tableComment" :show-overflow-tooltip="true" width="120"/>
      <el-table-column label="实体" align="center" prop="className" width="200"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="更新时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300px" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button type="text" size="small" icon="el-icon-view" @click="handlePreview(scope.row)" v-hasPermi="['infra:codegen:preview']">预览</el-button>
          <el-button type="text" size="small" icon="el-icon-edit" @click="handleEditTable(scope.row)" v-hasPermi="['infra:codegen:update']">编辑</el-button>
          <el-button type="text" size="small" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['infra:codegen:delete']">删除</el-button>
          <el-button type="text" size="small" icon="el-icon-refresh" @click="handleSynchDb(scope.row)" v-hasPermi="['infra:codegen:update']">同步</el-button>
          <el-button type="text" size="small" icon="el-icon-download" @click="handleGenTable(scope.row)" v-hasPermi="['infra:codegen:download']">生成代码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 预览界面 -->
    <el-dialog :title="preview.title" :visible.sync="preview.open" width="90%" top="5vh" append-to-body class="scrollbar">
      <el-row>
        <el-col :span="7">
          <el-tree :data="preview.fileTree" :expand-on-click-node="false" default-expand-all highlight-current
                   @node-click="handleNodeClick"/>
        </el-col>
        <el-col :span="17">
          <el-tabs v-model="preview.activeName">
            <el-tab-pane v-for="item in preview.data" :label="item.filePath.substring(item.filePath.lastIndexOf('/') + 1)"
                         :name="item.filePath" :key="item.filePath">
              <el-link :underline="false" icon="el-icon-document-copy" v-clipboard:copy="item.code" v-clipboard:success="clipboardSuccess" style="float:right">复制</el-link>
              <pre><code class="hljs" v-html="highlightedCode(item)"></code></pre>
            </el-tab-pane>
          </el-tabs>
        </el-col>
      </el-row>
    </el-dialog>

    <!-- 基于 DB 导入 -->
    <import-table ref="import" @ok="handleQuery" />
  </div>
</template>

<script>
import { getCodegenTablePage, previewCodegen, downloadCodegen, deleteCodegen,
  syncCodegenFromDB } from "@/api/infra/codegen";

import importTable from "./importTable";
// 代码高亮插件
import hljs from "highlight.js/lib/highlight";
import "highlight.js/styles/github-gist.css";
import {getDataSourceConfigList} from "@/api/infra/dataSourceConfig";
hljs.registerLanguage("java", require("highlight.js/lib/languages/java"));
hljs.registerLanguage("xml", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("html", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("vue", require("highlight.js/lib/languages/xml"));
hljs.registerLanguage("javascript", require("highlight.js/lib/languages/javascript"));
hljs.registerLanguage("sql", require("highlight.js/lib/languages/sql"));
hljs.registerLanguage("typescript", require("highlight.js/lib/languages/typescript"));
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
        tableComment: undefined,
        createTime: []
      },
      // 预览参数
      preview: {
        open: false,
        title: "代码预览",
        fileTree: [],
        data: {},
        activeName: "",
      },
      // 数据源列表
      dataSourceConfigs: [],
    };
  },
  created() {
    this.getList();
    // 加载数据源
    getDataSourceConfigList().then(response => {
      this.dataSourceConfigs = response.data;
    });
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
      getCodegenTablePage(this.queryParams).then(response => {
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
        this.$download.zip(response, 'codegen-' + row.tableName + '.zip');
      })
    },
    /** 同步数据库操作 */
    handleSynchDb(row) {
      // 基于 DB 同步
      const tableName = row.tableName;
      this.$modal.confirm('确认要强制同步"' + tableName + '"表结构吗？').then(function() {
          return syncCodegenFromDB(row.id);
      }).then(() => {
          this.$modal.msgSuccess("同步成功");
      }).catch(() => {});
    },
    /** 打开导入表弹窗 */
    openImportTable() {
      this.$refs.import.show();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 预览按钮 */
    handlePreview(row) {
      previewCodegen(row.id).then(response => {
        this.preview.data = response.data;
        let files = this.handleFiles(response.data);
        this.preview.fileTree = this.handleTree(files, "id", "parentId", "children",
            "/"); // "/" 为根节点
        // console.log(this.preview.fileTree)
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
    /** 复制代码成功 */
    clipboardSuccess() {
      this.$modal.msgSuccess("复制成功");
    },
    /** 生成 files 目录 **/
    handleFiles(datas) {
      let exists = {}; // key：file 的 id；value：true
      let files = [];
      // 遍历每个元素
      for (const data of datas) {
        let paths = data.filePath.split('/');
        let fullPath = ''; // 从头开始的路径，用于生成 id
        // 特殊处理 java 文件
        if (paths[paths.length - 1].indexOf('.java') >= 0) {
          let newPaths = [];
          for (let i = 0; i < paths.length; i++) {
            let path = paths[i];
            if (path !== 'java') {
              newPaths.push(path);
              continue;
            }
            newPaths.push(path);
            // 特殊处理中间的 package，进行合并
            let tmp = undefined;
            while (i < paths.length) {
              path = paths[i + 1];
              if (path === 'controller'
                || path === 'convert'
                || path === 'dal'
                || path === 'enums'
                || path === 'service'
                || path === 'vo' // 下面三个，主要是兜底。可能考虑到有人改了包结构
                || path === 'mysql'
                || path === 'dataobject') {
                break;
              }
              tmp = tmp ? tmp + '.' + path : path;
              i++;
            }
            if (tmp) {
              newPaths.push(tmp);
            }
          }
          paths = newPaths;
        }
        // 遍历每个 path， 拼接成树
        for (let i = 0; i < paths.length; i++) {
          // 已经添加到 files 中，则跳过
          let oldFullPath = fullPath;
          // 下面的 replaceAll 的原因，是因为上面包处理了，导致和 tabs 不匹配，所以 replaceAll 下
          fullPath = fullPath.length === 0 ? paths[i] : fullPath.replaceAll('.', '/') + '/' + paths[i];
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
    handleNodeClick(data, node) {
      if (node && !node.isLeaf) {
        return false;
      }
      // 判断，如果非子节点，不允许选中
      this.preview.activeName = data.id;
    },
    /** 修改按钮操作 */
    handleEditTable(row) {
      const tableId = row.id;
      const tableName = row.tableName || this.tableNames[0];
      const params = { pageNum: this.queryParams.pageNum };
      this.$tab.openPage("修改[" + tableName + "]生成配置", '/codegen/edit/' + tableId, params);
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const tableIds = row.id;
      this.$modal.confirm('是否确认删除表名称为"' + row.tableName + '"的数据项?').then(function() {
          return deleteCodegen(tableIds);
      }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    // 数据源配置的名字
    dataSourceConfigNameFormat(row, column) {
      for (const config of this.dataSourceConfigs) {
        if (row.dataSourceConfigId === config.id) {
          return config.name;
        }
      }
      return '未知【' + row.leaderUserId + '】';
    },
  }
};
</script>
