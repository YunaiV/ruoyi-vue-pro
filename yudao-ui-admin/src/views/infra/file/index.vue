<template>
  <div class="app-container">
    <doc-alert title="上传下载" url="https://doc.iocoder.cn/file/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件路径" prop="path">
        <el-input v-model="queryParams.path" placeholder="请输入文件路径" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRangeCreateTime" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
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
      <el-table-column label="文件名" align="center" prop="name" />
      <el-table-column label="文件路径" align="center" prop="path" />
      <el-table-column label="文件 URL" align="center" prop="url" />
      <el-table-column label="文件大小" align="center" prop="size" width="120" :formatter="sizeFormat" />
      <el-table-column label="文件类型" align="center" prop="type" width="80" />
<!--      <el-table-column label="文件内容" align="center" prop="content">-->
<!--        <template slot-scope="scope">-->
<!--          <img v-if="scope.row.type === 'jpg' || scope.row.type === 'png' || scope.row.type === 'gif'"-->
<!--               width="200px" :src="getFileUrl + scope.row.id">-->
<!--          <i v-else>非图片，无法预览</i>-->
<!--        </template>-->
<!--      </el-table-column>-->
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
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
import {getAccessToken} from "@/utils/auth";

export default {
  name: "File",
  data() {
    return {
      getFileUrl: process.env.VUE_APP_BASE_API + '/admin-api/infra/file/get/',
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
        path: null,
        type: null,
      },
      // 用户导入参数
      upload: {
        open: false, // 是否显示弹出层
        title: "", // 弹出层标题
        isUploading: false, // 是否禁用上传
        url: process.env.VUE_APP_BASE_API + "/admin-api/infra/file/upload", // 请求地址
        headers: { Authorization: "Bearer " + getAccessToken() }, // 设置上传的请求头部
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

    },
    /** 处理文件上传中 */
    handleFileUploadProgress(event, file, fileList) {
      this.upload.isUploading = true; // 禁止修改
    },
    /** 发起文件上传 */
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
      this.$modal.msgSuccess("上传成功");
      this.getList();
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除文件编号为"' + id + '"的数据项?').then(function() {
        return deleteFile(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    // 用户昵称展示
    sizeFormat(row, column) {
      const unitArr = ["Bytes","KB","MB","GB","TB","PB","EB","ZB","YB"];
      const srcSize = parseFloat(row.size);
      const index = Math.floor(Math.log(srcSize) / Math.log(1024));
      let size =srcSize/Math.pow(1024,index);
      size = size.toFixed(2);//保留的小数位数
      return size + ' ' + unitArr[index];
    },
  }
};
</script>
