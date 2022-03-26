<template>
  <div class="app-container">
<!--    <doc-alert title="上传下载" url="https://doc.iocoder.cn/file/"/>-->
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="文件路径" prop="path">
        <el-input v-model="queryParams.path" placeholder="请输入文件路径" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="YYYY-MM-DD"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd">上传文件</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="文件名" align="center" prop="path"/>
      <el-table-column label="URL" align="center" prop="url"/>
      <el-table-column label="文件大小" align="center" prop="size" width="120" :formatter="sizeFormat"/>
      <el-table-column label="文件类型" align="center" prop="type" width="80"/>
      <!--      <el-table-column label="文件内容" align="center" prop="content">-->
      <!--        <template slot-scope="scope">-->
      <!--          <img v-if="scope.row.type === 'jpg' || scope.row.type === 'png' || scope.row.type === 'gif'"-->
      <!--               width="200px" :src="getFileUrl + scope.row.id">-->
      <!--          <i v-else>非图片，无法预览</i>-->
      <!--        </template>-->
      <!--      </el-table-column>-->
      <el-table-column label="上传时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ proxy.parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="100">
        <template #default="scope">
          <el-button size="small" type="text" icon="Delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['infra:file:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page="queryParams.pageNo" :limit="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="uploadData.title" v-model="uploadData.open" width="400px" append-to-body @closed="closeDialog">
      <el-upload ref="upload" :limit="1" accept=".jpg, .png, .gif" :auto-upload="false" drag
                 :headers="uploadData.headers" :action="uploadData.url" :data="uploadData.data" :disabled="uploadData.isUploading"
                 :on-change="handleFileChange"
                 :on-progress="handleFileUploadProgress"
                 :on-success="handleFileSuccess">
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip" style="color:red" slot="tip">提示：仅允许导入 jpg、png、gif 格式文件！</div>
        </template>
      </el-upload>
      <template #footer>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="submitFileForm">确 定</el-button>
          <el-button @click="uploadData.open = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup name="File">
import {deleteFile, getFilePage} from "@/api/infra/file";
import {getToken} from "@/utils/auth";

const {proxy} = getCurrentInstance();
const title = ref("");
const loading = ref(true);// 遮罩层
const exportLoading = ref(false);// 导出遮罩层
const showSearch = ref(true);// 显示搜索条件
const total = ref(0);// 总条数
const list = ref([]);// 表格数据
const open = ref(false);// 是否显示弹出层
const dateRange = ref([]);// 日期范围
const getFileUrl = ref(import.meta.env.VUE_APP_BASE_API + '/admin-api/infra/file/get/');
const data = reactive({
  formData: {},
  // 查询参数
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    name: undefined,
    key: undefined,
    type: undefined
  },
  // 用户导入参数
  uploadData: {
    open: false, // 是否显示弹出层
    title: "", // 弹出层标题
    isUploading: false, // 是否禁用上传
    url: import.meta.env.VITE_APP_BASE_API + "/admin-api/infra/file/upload", // 请求地址
    headers: {Authorization: "Bearer " + getToken()}, // 设置上传的请求头部
    data: {} // 上传的额外数据，用于文件名
  }
});

const {formData, queryParams, uploadData} = toRefs(data);

/** 查询列表 */
function getList() {
  loading.value = true;
  // 处理查询参数
  let params = {...queryParams.value};
  proxy.addBeginAndEndTime(params, dateRange.value, 'createTime');
  // 执行查询
  getFilePage(params).then(response => {
    list.value = response.data.list;
    total.value = response.data.total;
    loading.value = false;
  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  formData.value = {
    content: undefined,
  };
  proxy.resetForm("form");
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNo = 1;
  getList();
}

/** 重置按钮操作 */
function resetQuery() {
  dateRange.value = [];
  proxy.resetForm("queryForm");
  handleQuery();
}

/** 新增按钮操作 */
function handleAdd() {
  uploadData.value.open = true;
  uploadData.value.title = "上传文件";
  console.log(upload.value)

}

/** 处理上传的文件发生变化 */
function handleFileChange(file, fileList) {
  uploadData.value.data.path = file.name;
}

/** 处理文件上传中 */
function handleFileUploadProgress(event, file, fileList) {
  uploadData.value.isUploading = true; // 禁止修改
}

/** 发起文件上传 */
function submitFileForm() {
  proxy.$refs.upload.submit();
}

/** 文件上传成功处理 */
function handleFileSuccess(response, file, fileList) {
  // 清理
  uploadData.value.open = false;
  uploadData.value.isUploading = false;
  proxy.$refs.upload.clearFiles();
  // 提示成功，并刷新
  proxy.$modal.msgSuccess("上传成功");
  getList();
}

/** 删除按钮操作 */
function handleDelete(row) {
  const id = row.id;
  proxy.$modal.confirm('是否确认删除文件编号为"' + id + '"的数据项?').then(function () {
    return deleteFile(id);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

// 用户昵称展示
function sizeFormat(row, column) {
  const unitArr = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];
  const srcSize = parseFloat(row.size);
  const index = Math.floor(Math.log(srcSize) / Math.log(1024));
  let size = srcSize / Math.pow(1024, index);
  size = size.toFixed(2);//保留的小数位数
  return size + ' ' + unitArr[index];
}
function closeDialog(){
  console.log( upload.value.open);
}
getList();
</script>
