<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件路径" prop="id">
        <el-input v-model="queryParams.id" placeholder="请输入文件路径" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="文件类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择文件类型" clearable size="small">
          <el-option label="请选择字典生成" value="" />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" size="small" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">上传文件</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="文件路径" align="center" prop="id" width="300" />
      <el-table-column label="文件类型" align="center" prop="type" width="80" />
      <el-table-column label="文件内容" align="center" prop="content">
        <template slot-scope="scope">
          <img v-if="scope.row.type === 'jpg' || scope.row.type === 'png' || scope.row.type === 'gif'"
               width="200px" :src="getFileUrl + scope.row.id">
          <i v-else>非图片，无法预览</i>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['infra:file:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="upload.title" :visible.sync="upload.open" width="400px" append-to-body>
      <el-upload ref="upload" :limit="1" accept=".jpg, .png, .gif" :auto-upload="false" drag
                 :headers="upload.headers" :action="upload.url" :data="upload.data" :disabled="upload.isUploading"
                 :on-change="handleFileChange"
                 :on-progress="handleFileUploadProgress"
                 :on-success="handleFileSuccess">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">
          将文件拖到此处，或 <em>点击上传</em>
        </div>
        <div class="el-upload__tip" style="color:red" slot="tip">提示：仅允许导入 jpg、png、gif 格式文件！</div>
      </el-upload>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitFileForm">确 定</el-button>
        <el-button @click="upload.open = false">取 消</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { deleteFile, getFilePage } from "@/api/infra/file";
import {getToken} from "@/utils/auth";

export default {
  name: "File",
  data() {
    return {
      getFileUrl: process.env.VUE_APP_BASE_API + '/api/infra/file/get/',
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 文件列表
      list: [],
      // 弹出层标题
      title: "",
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        id: null,
        type: null,
      },
      // 用户导入参数
      upload: {
        open: false, // 是否显示弹出层
        title: "", // 弹出层标题
        isUploading: false, // 是否禁用上传
        url: process.env.VUE_APP_BASE_API + '/api/' + "/infra/file/upload", // 请求地址
        headers: { Authorization: "Bearer " + getToken() }, // 设置上传的请求头部
        data: {} // 上传的额外数据，用于文件名
      },
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getFilePage(params).then(response => {
        this.list = response.data.list;
        this.total = response.data.total;
        this.loading = false;
      });
    },
    /** 取消按钮 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        content: undefined,
      };
      this.resetForm("form");
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNo = 1;
      this.getList();
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRangeCreateTime = [];
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.upload.open = true;
      this.upload.title = "上传文件";
    },
    /** 处理上传的文件发生变化 */
    handleFileChange(file, fileList) {
      this.upload.data.path = file.name;
    },
    /** 处理文件上传中 */
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true; // 禁止修改
    },
    /** 发起文件上窜 */
    submitFileForm() {
      this.$refs.upload.submit();
    },
    /** 文件上传成功处理 */
    handleFileSuccess(response, file, fileList) {
      // 清理
      this.upload.open = false;
      this.upload.isUploading = false;
      this.$refs.upload.clearFiles();
      // 提示成功，并刷新
      this.msgSuccess("上传成功");
      this.getList();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$confirm('是否确认删除文件编号为"' + id + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return deleteFile(id);
      }).then(() => {
        this.getList();
        this.msgSuccess("删除成功");
      })
    },
  }
};
</script>
