<template>
  <div class="app-container">
    <doc-alert title="上传下载" url="https://doc.iocoder.cn/file/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="配置名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入配置名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="存储器" prop="storage">
        <el-select v-model="queryParams.storage" placeholder="请选择存储器" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INFRA_FILE_STORAGE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
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
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
                   v-hasPermi="['infra:file-config:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="配置名" align="center" prop="name" />
      <el-table-column label="存储器" align="center" prop="storage">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.INFRA_FILE_STORAGE" :value="scope.row.storage" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="主配置" align="center" prop="primary">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.master" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['infra:file-config:update']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-attract" @click="handleMaster(scope.row)"
                     :disabled="scope.row.master" v-hasPermi="['infra:file-config:update']">主配置</el-button>
          <el-button size="mini" type="text" icon="el-icon-share" @click="handleTest(scope.row)">测试</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['infra:file-config:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="配置名" prop="name">
          <el-input v-model="form.name" placeholder="请输入配置名" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="存储器" prop="storage">
          <el-select v-model="form.storage" placeholder="请选择存储器" :disabled="form.id !== undefined">
            <el-option v-for="dict in this.getDictDatas(DICT_TYPE.INFRA_FILE_STORAGE)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
          </el-select>
        </el-form-item>
        <!-- DB -->
        <!-- Local / FTP / SFTP -->
        <el-form-item v-if="form.storage >= 10 && form.storage <= 12" label="基础路径" prop="config.basePath">
          <el-input v-model="form.config.basePath" placeholder="请输入基础路径" />
        </el-form-item>
        <el-form-item v-if="form.storage >= 11 && form.storage <= 12" label="主机地址" prop="config.host">
          <el-input v-model="form.config.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item v-if="form.storage >= 11 && form.storage <= 12" label="主机端口" prop="config.port">
          <el-input-number :min="0" v-model="form.config.port" placeholder="请输入主机端口" />
        </el-form-item>
        <el-form-item v-if="form.storage >= 11 && form.storage <= 12" label="用户名" prop="config.username">
          <el-input v-model="form.config.username" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="form.storage >= 11 && form.storage <= 12" label="密码" prop="config.password">
          <el-input v-model="form.config.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="form.storage === 11" label="连接模式" prop="config.mode">
          <el-radio-group v-model="form.config.mode">
            <el-radio key="Active" label="Active">主动模式</el-radio>
            <el-radio key="Passive" label="Passive">主动模式</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- S3 -->
        <el-form-item v-if="form.storage === 20" label="节点地址" prop="config.endpoint">
          <el-input v-model="form.config.endpoint" placeholder="请输入节点地址" />
        </el-form-item>
        <el-form-item v-if="form.storage === 20" label="存储 bucket" prop="config.bucket">
          <el-input v-model="form.config.bucket" placeholder="请输入 bucket" />
        </el-form-item>
        <el-form-item v-if="form.storage === 20" label="accessKey" prop="config.accessKey">
          <el-input v-model="form.config.accessKey" placeholder="请输入 accessKey" />
        </el-form-item>
        <el-form-item v-if="form.storage === 20" label="accessSecret" prop="config.accessSecret">
          <el-input v-model="form.config.accessSecret" placeholder="请输入 accessSecret" />
        </el-form-item>
        <!-- 通用 -->
        <el-form-item v-if="form.storage === 20" label="自定义域名"> <!-- 无需参数校验，所以去掉 prop -->
          <el-input v-model="form.config.domain" placeholder="请输入自定义域名" />
        </el-form-item>
        <el-form-item v-else-if="form.storage" label="自定义域名" prop="config.domain">
          <el-input v-model="form.config.domain" placeholder="请输入自定义域名" />
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
import {
  createFileConfig,
  updateFileConfig,
  deleteFileConfig,
  getFileConfig,
  getFileConfigPage,
  testFileConfig, updateFileConfigMaster
} from "@/api/infra/fileConfig";

export default {
  name: "FileConfig",
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
      // 文件配置列表
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
        name: null,
        storage: null,
      },
      // 表单参数
      form: {
        storage: undefined,
        config: {}
      },
      // 表单校验
      rules: {
        name: [{ required: true, message: "配置名不能为空", trigger: "blur" }],
        storage: [{ required: true, message: "存储器不能为空", trigger: "change" }],
        config: {
          basePath: [{ required: true, message: "基础路径不能为空", trigger: "blur" }],
          host: [{ required: true, message: "主机地址不能为空", trigger: "blur" }],
          port: [{ required: true, message: "主机端口不能为空", trigger: "blur" }],
          username: [{ required: true, message: "用户名不能为空", trigger: "blur" }],
          password: [{ required: true, message: "密码不能为空", trigger: "blur" }],
          mode: [{ required: true, message: "连接模式不能为空", trigger: "change" }],
          endpoint: [{ required: true, message: "节点地址不能为空", trigger: "blur" }],
          bucket: [{ required: true, message: "存储 bucket 不能为空", trigger: "blur" }],
          accessKey: [{ required: true, message: "accessKey 不能为空", trigger: "blur" }],
          accessSecret: [{ required: true, message: "accessSecret 不能为空", trigger: "blur" }],
          domain: [{ required: true, message: "自定义域名不能为空", trigger: "blur" }],
        },
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
      getFileConfigPage(params).then(response => {
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
        name: undefined,
        storage: undefined,
        remark: undefined,
        config: {},
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
      this.title = "添加文件配置";
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset();
      const id = row.id;
      getFileConfig(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改文件配置";
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
          updateFileConfig(this.form).then(response => {
            this.$modal.msgSuccess("修改成功");
            this.open = false;
            this.getList();
          });
          return;
        }
        // 添加的提交
        createFileConfig(this.form).then(response => {
          this.$modal.msgSuccess("新增成功");
          this.open = false;
          this.getList();
        });
      });
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const id = row.id;
      this.$modal.confirm('是否确认删除文件配置编号为"' + id + '"的数据项?').then(function() {
        return deleteFileConfig(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    },
    /** 主配置按钮操作 */
    handleMaster(row) {
      const id = row.id;
      this.$modal.confirm('是否确认修改配置编号为"' + id + '"的数据项为主配置?').then(function() {
        return updateFileConfigMaster(id);
      }).then(() => {
        this.getList();
        this.$modal.msgSuccess("修改成功");
      }).catch(() => {});
    },
    /** 测试按钮操作 */
    handleTest(row) {
      testFileConfig(row.id).then((response) => {
        this.$modal.alert("测试通过，上传文件成功！访问地址：" + response.data);
      }).catch(() => {});
    },
  }
};
</script>
