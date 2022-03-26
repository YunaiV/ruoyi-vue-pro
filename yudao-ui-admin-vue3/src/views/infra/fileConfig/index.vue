<template>
  <div class="app-container">
<!--    <doc-alert title="上传下载" url="https://doc.iocoder.cn/file/" />-->
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="配置名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入配置名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="存储器" prop="storage">
        <el-select v-model="queryParams.storage" placeholder="请选择存储器" clearable>
          <el-option v-for="dict in proxy.getDictDatas(DICT_TYPE.INFRA_FILE_STORAGE)"
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
        <el-button type="primary" plain icon="el-icon-plus" size="small" @click="handleAdd"
                   v-hasPermi="['infra:file-config:create']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="编号" align="center" prop="id" />
      <el-table-column label="配置名" align="center" prop="name" />
      <el-table-column label="存储器" align="center" prop="storage">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_FILE_STORAGE" :value="scope.row.storage" />
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" />
      <el-table-column label="主配置" align="center" prop="primary">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_BOOLEAN_STRING" :value="scope.row.master" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ proxy.parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="240">
        <template #default="scope">
          <el-button size="small" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['infra:file-config:update']">修改</el-button>
          <el-button size="small" type="text" icon="el-icon-attract" @click="handleMaster(scope.row)"
                     :disabled="scope.row.master" v-hasPermi="['infra:file-config:update']">主配置</el-button>
          <el-button size="small" type="text" icon="el-icon-share" @click="handleTest(scope.row)">测试</el-button>
          <el-button size="small" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['infra:file-config:delete']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page="queryParams.pageNo" :limit="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 对话框(添加 / 修改) -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="form" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="配置名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入配置名" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" placeholder="请输入备注" />
        </el-form-item>
        <el-form-item label="存储器" prop="storage">
          <el-select v-model="formData.storage" placeholder="请选择存储器" :disabled="formData.id">
            <el-option v-for="dict in proxy.getDictDatas(DICT_TYPE.INFRA_FILE_STORAGE)"
                       :key="dict.value" :label="dict.label" :value="parseInt(dict.value)" />
          </el-select>
        </el-form-item>
        <!-- DB -->
        <!-- Local / FTP / SFTP -->
        <el-form-item v-if="formData.storage >= 10 && formData.storage <= 12" label="基础路径" prop="config.basePath">
          <el-input v-model="formData.config.basePath" placeholder="请输入基础路径" />
        </el-form-item>
        <el-form-item v-if="formData.storage >= 11 && formData.storage <= 12" label="主机地址" prop="config.host">
          <el-input v-model="formData.config.host" placeholder="请输入主机地址" />
        </el-form-item>
        <el-form-item v-if="formData.storage >= 11 && formData.storage <= 12" label="主机端口" prop="config.port">
          <el-input-number min="0" v-model="formData.config.port" placeholder="请输入主机端口" />
        </el-form-item>
        <el-form-item v-if="formData.storage >= 11 && formData.storage <= 12" label="用户名" prop="config.username">
          <el-input v-model="formData.config.username" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="formData.storage >= 11 && formData.storage <= 12" label="密码" prop="config.password">
          <el-input v-model="formData.config.password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item v-if="formData.storage === 11" label="连接模式" prop="config.mode">
          <el-radio-group v-model="formData.config.mode">
            <el-radio key="Active" label="Active">主动模式</el-radio>
            <el-radio key="Passive" label="Passive">主动模式</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- S3 -->
        <el-form-item v-if="formData.storage === 20" label="节点地址" prop="config.endpoint">
          <el-input v-model="formData.config.endpoint" placeholder="请输入节点地址" />
        </el-form-item>
        <el-form-item v-if="formData.storage === 20" label="存储 bucket" prop="config.bucket">
          <el-input v-model="formData.config.bucket" placeholder="请输入 bucket" />
        </el-form-item>
        <el-form-item v-if="formData.storage === 20" label="accessKey" prop="config.accessKey">
          <el-input v-model="formData.config.accessKey" placeholder="请输入 accessKey" />
        </el-form-item>
        <el-form-item v-if="formData.storage === 20" label="accessSecret" prop="config.accessSecret">
          <el-input v-model="formData.config.accessSecret" placeholder="请输入 accessSecret" />
        </el-form-item>
        <!-- 通用 -->
        <el-form-item v-if="formData.storage === 20" label="自定义域名"> <!-- 无需参数校验，所以去掉 prop -->
          <el-input v-model="formData.config.domain" placeholder="请输入自定义域名" />
        </el-form-item>
        <el-form-item v-else-if="formData.storage" label="自定义域名" prop="config.domain">
          <el-input v-model="formData.config.domain" placeholder="请输入自定义域名" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup name="FileConfig">
import {
  createFileConfig,
  updateFileConfig,
  deleteFileConfig,
  getFileConfig,
  getFileConfigPage,
  testFileConfig, updateFileConfigMaster
} from "@/api/infra/fileConfig";


const {proxy} = getCurrentInstance();
const title=ref("");
const loading = ref(true);// 遮罩层
const exportLoading = ref(false);// 导出遮罩层
const showSearch = ref(true);// 显示搜索条件
const total = ref(0);// 总条数
const list = ref([]);// 表格数据
const open = ref(false);// 是否显示弹出层
const dateRange = ref([]);// 日期范围
const data = reactive({
  // 查询参数
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    name: null,
    storage: null,
  },
  // 表单参数
  formData: {
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
});

const { queryParams,formData, rules} = toRefs(data);




    /** 查询列表 */
    function   getList() {
      loading.value = true;
      // 处理查询参数
      let params = {...queryParams.value};
      proxy.addBeginAndEndTime(params, dateRange.value, 'createTime');
      // 执行查询
      getFileConfigPage(params).then(response => {
        list.value  = response.data.list;
        total.value  = response.data.total;
        loading.value  = false;
      });
    }
    /** 取消按钮 */
    function cancel() {
      open.value  = false;
      reset();
    }
    /** 表单重置 */
    function   reset() {
      formData.value = {
        id: undefined,
        name: undefined,
        storage: undefined,
        remark: undefined,
        config: {},
      };
      proxy.resetForm("form");
    }
    /** 搜索按钮操作 */
    function  handleQuery() {
      queryParams.value.pageNo = 1;
      getList();
    }
    /** 重置按钮操作 */
    function   resetQuery() {
      dateRange.vlaue = [];
      proxy.resetForm("queryForm");
      handleQuery();
    }
    /** 新增按钮操作 */
    function handleAdd() {
      reset();
      open.value  = true;
      title.value  = "添加文件配置";
    }
    /** 修改按钮操作 */
    function  handleUpdate(row) {
      reset();
      const id = row.id;

      getFileConfig(id).then(response => {
        formData.value = response.data;
        open.value  = true;
        title.value  = "修改文件配置";
      });
    }
    /** 提交按钮 */
    function  submitForm() {
      proxy.$refs["form"].validate(valid => {
        if (!valid) {
          return;
        }
        // 修改的提交
        if (formData.id != null) {
          updateFileConfig(formData.value).then(response => {
            proxy.$modal.msgSuccess("修改成功");
            open.value  = false;
            getList();
          });
          return;
        }
        // 添加的提交
        createFileConfig(formData.value).then(response => {
          proxy.$modal.msgSuccess("新增成功");
          open.value  = false;
          getList();
        });
      });
    }
    /** 删除按钮操作 */
    function  handleDelete(row) {
      const id = row.id;
      proxy.$modal.confirm('是否确认删除文件配置编号为"' + id + '"的数据项?').then(function() {
        return deleteFileConfig(id);
      }).then(() => {
        getList();
        proxy.$modal.msgSuccess("删除成功");
      }).catch(() => {});
    }
    /** 主配置按钮操作 */
    function  handleMaster(row) {
      const id = row.id;
      proxy.$modal.confirm('是否确认修改配置编号为"' + id + '"的数据项为主配置?').then(function() {
        return updateFileConfigMaster(id);
      }).then(() => {
        getList();
        proxy.$modal.msgSuccess("修改成功");
      }).catch(() => {});
    }
    /** 测试按钮操作 */
  function  handleTest(row) {
      testFileConfig(row.id).then((response) => {
        proxy.$modal.alert("测试通过，上传文件成功！访问地址：" + response.data);
      }).catch(() => {});
    }
getList();
</script>
