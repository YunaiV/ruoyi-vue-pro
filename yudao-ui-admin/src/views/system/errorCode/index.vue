<template>
  <div class="app-container">
    <doc-alert title="异常处理（错误码）" url="https://doc.iocoder.cn/exception/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="100px">
      <el-form-item label="错误码类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="请选择错误码类型" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYSTEM_ERROR_CODE_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="应用名" prop="applicationName">
        <el-input v-model="queryParams.applicationName" placeholder="请输入应用名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="错误码编码" prop="code">
        <el-input v-model="queryParams.code" placeholder="请输入错误码编码" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="错误码提示" prop="message">
        <el-input v-model="queryParams.message" placeholder="请输入错误码提示" clearable @keyup.enter.native="handleQuery"/>
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

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['system:error-code:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['system:error-code:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="类型" align="center" prop="type" width="80">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_ERROR_CODE_TYPE" :value="scope.row.type" />
        </template>
      </el-table-column>
      <el-table-column label="应用名" align="center" prop="applicationName" width="200" />
      <el-table-column label="错误码编码" align="center" prop="code" width="120" />
      <el-table-column label="错误码提示" align="center" prop="message" width="300" />
      <el-table-column label="备注" align="center" prop="memo" width="200" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:error-code:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:error-code:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="应用名" prop="applicationName">
          <el-input v-model="form.applicationName" placeholder="请输入应用名" />
        </el-form-item>
        <el-form-item label="错误码编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入错误码编码" />
        </el-form-item>
        <el-form-item label="错误码提示" prop="message">
          <el-input v-model="form.message" placeholder="请输入错误码提示" />
        </el-form-item>
        <el-form-item label="备注" prop="memo">
          <el-input v-model="form.memo" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { createErrorCode, updateErrorCode, deleteErrorCode, getErrorCode, getErrorCodePage, exportErrorCodeExcel } from "@/api/system/errorCode";

export default {
  name: "ErrorCode",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 导出遮罩层
      exportLoading: false,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 错误码列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        type: null,
        applicationName: null,
        code: null,
        message: null,
        createTime: []
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        applicationName: [{ required: true, message: "应用名不能为空", trigger: "blur" }],
        code: [{ required: true, message: "错误码编码不能为空", trigger: "blur" }],
        message: [{ required: true, message: "错误码提示不能为空", trigger: "blur" }],
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询列表 */
    getList() {
      this.loading = true;
      // 执行查询
      getErrorCodePage(this.queryParams).then(response => {
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
        id: undefined,
        applicationName: undefined,
        code: undefined,
        message: undefined,
        memo: undefined,
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
      this.resetForm("queryForm");
      this.handleQuery();
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加错误码";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getErrorCode(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改错误码";
      });
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (this.form.id != null) {
          updateErrorCode(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createErrorCode(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除错误码编号为"' + id + '"的数据项?').then(function() {
        return deleteErrorCode(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      // 执行导出
      this.$modal.confirm('是否确认导出所有错误码数据项?').then(() => {
        this.exportLoading = true;
        return exportErrorCodeExcel(params);
      }).then(response => {
        this.$download.excel(response, '错误码.xls');
        this.exportLoading = false;
      }).catch(() => {});
    }
  }
};
</script>
