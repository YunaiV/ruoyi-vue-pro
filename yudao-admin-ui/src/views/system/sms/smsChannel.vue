<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="短信签名" prop="signature">
        <el-input v-model="queryParams.signature" placeholder="请输入短信签名" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="启用状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择启用状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['system:sms-channel:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['system:sms-channel:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="短信签名" align="center" prop="signature" />
      <el-table-column label="渠道编码" align="center" prop="code">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.SYS_SMS_CHANNEL_CODE, scope.row.code) }}</span>
        </template>
      </el-table-column>>
      <el-table-column label="启用状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.SYS_COMMON_STATUS, scope.row.status) }}</span>
        </template>
      </el-table-column>>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="短信 API 的账号" align="center" prop="apiKey" />
      <el-table-column label="短信 API 的秘钥" align="center" prop="apiSecret" />
      <el-table-column label="短信发送回调 URL" align="center" prop="callbackUrl" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:sms-channel:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:sms-channel:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="短信签名" prop="signature">
          <el-input v-model="form.signature" placeholder="请输入短信签名" />
        </el-form-item>
        <el-form-item label="渠道编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入渠道编码" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="短信 API 的账号" prop="apiKey">
          <el-input v-model="form.apiKey" placeholder="请输入短信 API 的账号" />
        </el-form-item>
        <el-form-item label="短信 API 的秘钥" prop="apiSecret">
          <el-input v-model="form.apiSecret" placeholder="请输入短信 API 的秘钥" />
        </el-form-item>
        <el-form-item label="短信发送回调 URL" prop="callbackUrl">
          <el-input v-model="form.callbackUrl" placeholder="请输入短信发送回调 URL" />
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
import { createSmsChannel, updateSmsChannel, deleteSmsChannel, getSmsChannel, getSmsChannelPage,
  getSimpleSmsChannels } from "@/api/system/sms/smsChannel";

export default {
  name: "SmsChannel",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 短信渠道列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        signature: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        signature: [{ required: true, message: "短信签名不能为空", trigger: "blur" }],
        code: [{ required: true, message: "渠道编码不能为空", trigger: "blur" }],
        status: [{ required: true, message: "启用状态不能为空", trigger: "blur" }],
        apiKey: [{ required: true, message: "短信 API 的账号不能为空", trigger: "blur" }],
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
      getSmsChannelPage(params).then(response => {
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
        signature: undefined,
        code: undefined,
        status: undefined,
        remark: undefined,
        apiKey: undefined,
        apiSecret: undefined,
        callbackUrl: undefined,
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
      this.reset();
      this.open = true;
      this.title = "添加短信渠道";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getSmsChannel(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改短信渠道";
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
          updateSmsChannel(this.form).then(response => {
            this.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createSmsChannel(this.form).then(response => {
          this.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$confirm('是否确认删除短信渠道编号为"' + id + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return deleteSmsChannel(id);
      }).then(() => {
        this.getList();
        this.msgSuccess("删除成功");
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行导出
      this.$confirm('是否确认导出所有短信渠道数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return exportSmsChannelExcel(params);
      }).then(response => {
        this.downloadExcel(response, '短信渠道.xls');
      })
    }
  }
};
</script><template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="短信签名" prop="signature">
        <el-input v-model="queryParams.signature" placeholder="请输入短信签名" clearable size="small" @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="启用状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择启用状态" clearable size="small">
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['system:sms-channel:create']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport"
                   v-hasPermi="['system:sms-channel:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="短信签名" align="center" prop="signature" />
      <el-table-column label="渠道编码" align="center" prop="code">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.SYS_SMS_CHANNEL_CODE, scope.row.code) }}</span>
        </template>
      </el-table-column>>
      <el-table-column label="启用状态" align="center" prop="status">
        <template slot-scope="scope">
          <span>{{ getDictDataLabel(DICT_TYPE.SYS_COMMON_STATUS, scope.row.status) }}</span>
        </template>
      </el-table-column>>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['system:sms-channel:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['system:sms-channel:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="短信签名" prop="signature">
          <el-input v-model="form.signature" placeholder="请输入短信签名" />
        </el-form-item>
        <el-form-item label="渠道编码" prop="code">
          <el-select v-model="form.code" placeholder="请选择渠道编码" :disabled="form.id > 0">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.SYS_SMS_CHANNEL_CODE)"
                       :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in this.getDictDatas(DICT_TYPE.SYS_COMMON_STATUS)"
                      :key="dict.value" :label="parseInt(dict.value)">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="短信 API 的账号" prop="apiKey">
          <el-input v-model="form.apiKey" placeholder="请输入短信 API 的账号" />
        </el-form-item>
        <el-form-item v-if="form.code !== 'YUN_PIAN'" label="短信 API 的秘钥" prop="apiSecret">
          <el-input v-model="form.apiSecret" placeholder="请输入短信 API 的秘钥" />
        </el-form-item>
        <el-form-item label="短信发送回调 URL" prop="callbackUrl">
          <el-input v-model="form.callbackUrl" placeholder="请输入短信发送回调 URL" />
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
import { createSmsChannel, updateSmsChannel, deleteSmsChannel, getSmsChannel, getSmsChannelPage } from "@/api/system/sms/smsChannel";

export default {
  name: "SmsChannel",
  components: {
  },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 短信渠道列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      dateRangeCreateTime: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        signature: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      apiKeyEnableChannelCodes: ['YUN_PIAN'],
      rules: {
        signature: [{ required: true, message: "短信签名不能为空", trigger: "blur" }],
        code: [{ required: true, message: "渠道编码不能为空", trigger: "blur" }],
        status: [{ required: true, message: "启用状态不能为空", trigger: "blur" }],
        apiKey: [{ required: true, message: "短信 API 的账号不能为空", trigger: "blur" }],
        apiSecret: [{ required: true, message: "短信 API 的秘钥不能为空", trigger: "blur" }],
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
      // 处理查询参数
      let params = {...this.queryParams};
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行查询
      getSmsChannelPage(params).then(response => {
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
        signature: undefined,
        code: undefined,
        status: undefined,
        remark: undefined,
        apiKey: undefined,
        apiSecret: undefined,
        callbackUrl: undefined,
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
      this.reset();
      this.open = true;
      this.title = "添加短信渠道";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getSmsChannel(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改短信渠道";
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
          updateSmsChannel(this.form).then(response => {
            this.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createSmsChannel(this.form).then(response => {
          this.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$confirm('是否确认删除短信渠道编号为"' + id + '"的数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return deleteSmsChannel(id);
      }).then(() => {
        this.getList();
        this.msgSuccess("删除成功");
      })
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      this.addBeginAndEndTime(params, this.dateRangeCreateTime, 'createTime');
      // 执行导出
      this.$confirm('是否确认导出所有短信渠道数据项?', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(function() {
        return exportSmsChannelExcel(params);
      }).then(response => {
        this.downloadExcel(response, '短信渠道.xls');
      })
    }
  }
};
</script>
