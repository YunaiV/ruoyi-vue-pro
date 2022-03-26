<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="参数名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入参数名称" clearable style="width: 240px"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="参数键名" prop="key">
        <el-input v-model="queryParams.key" placeholder="请输入参数键名" clearable style="width: 240px"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="系统内置" prop="type">
        <el-select v-model="queryParams.type" placeholder="系统内置" clearable>
          <el-option v-for="dict in proxy.getDictDatas(DICT_TYPE.INFRA_CONFIG_TYPE)" :key="parseInt(dict.value)"
                     :label="dict.label" :value="parseInt(dict.value)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="YYYY-MM-DD" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd"
                   v-hasPermi="['infra:config:create']">新增
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" icon="Download" @click="handleExport" v-model:loading="exportLoading"
                   v-hasPermi="['infra:config:export']">导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="configList">
      <el-table-column label="参数主键" align="center" prop="id"/>
      <el-table-column label="参数分组" align="center" prop="group"/>
      <el-table-column label="参数名称" align="center" prop="name" :show-overflow-tooltip="true"/>
      <el-table-column label="参数键名" align="center" prop="key" :show-overflow-tooltip="true"/>
      <el-table-column label="参数键值" align="center" prop="value"/>
      <el-table-column label="系统内置" align="center" prop="type">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_CONFIG_TYPE" :value="scope.row.type"/>
        </template>
      </el-table-column>
      <el-table-column label="是否敏感" align="center" prop="sensitive">
        <template #default="scope">
          <span>{{ scope.row.sensitive ? '是' : '否' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true"/>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ proxy.parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="text" icon="Edit" @click="handleUpdate(scope.row)"
                     v-hasPermi="['infra:config:update']">修改
          </el-button>
          <el-button size="small" type="text" icon="Delete" @click="handleDelete(scope.row)"
                     v-hasPermi="['infra:config:delete']">删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page="queryParams.pageNo" :limit="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 添加或修改参数配置对话框 -->
    <el-dialog :title="title" v-model="open" width="500px" append-to-body>
      <el-form ref="form" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="参数分组" prop="group">
          <el-input v-model="formData.group" placeholder="请输入参数分组"/>
        </el-form-item>
        <el-form-item label="参数名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入参数名称"/>
        </el-form-item>
        <el-form-item label="参数键名" prop="key">
          <el-input v-model="formData.key" placeholder="请输入参数键名"/>
        </el-form-item>
        <el-form-item label="参数键值" prop="value">
          <el-input v-model="formData.value" placeholder="请输入参数键值"/>
        </el-form-item>
        <el-form-item label="是否敏感" prop="type">
          <el-radio-group v-model="formData.sensitive">
            <el-radio :key="true" :label="true">是</el-radio>
            <el-radio :key="false" :label="false">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" placeholder="请输入内容"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Config">
import {listConfig, getConfig, delConfig, addConfig, updateConfig, exportConfig} from "@/api/infra/config";

const {proxy} = getCurrentInstance();
const title=ref("");
const loading = ref(true);// 遮罩层
const exportLoading = ref(false);// 导出遮罩层
const showSearch = ref(true);// 显示搜索条件
const total = ref(0);// 总条数
const configList = ref([]);// 表格数据
const open = ref(false);// 是否显示弹出层
const dateRange = ref([]);// 日期范围
const data = reactive({
  // 表单参数
  formData: {},
  // 查询参数
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    name: undefined,
    key: undefined,
    type: undefined
  },
  // 表单校验
  rules: {
    group: [
      {required: true, message: "参数分组不能为空", trigger: "blur"}
    ],
    name: [
      {required: true, message: "参数名称不能为空", trigger: "blur"}
    ],
    key: [
      {required: true, message: "参数键名不能为空", trigger: "blur"}
    ],
    value: [
      {required: true, message: "参数键值不能为空", trigger: "blur"}
    ]
  }
});

const {formData, queryParams, rules} = toRefs(data);

/** 查询参数列表 */
function getList() {
  loading.value = true;
  listConfig(proxy.addDateRange(queryParams.value, [
    dateRange.value[0] ? dateRange.value[0] + ' 00:00:00' : undefined,
    dateRange.value[1] ? dateRange.value[1] + ' 23:59:59' : undefined,
  ])).then(response => {
        configList.value = response.data.list;
        total.value = response.data.total;
        loading.value = false;
      }
  );
}

// 取消按钮
function cancel() {
  open.value = false;
  reset();
}

// 表单重置
function reset() {
  formData.value = {
    id: undefined,
    name: undefined,
    key: undefined,
    value: undefined,
    remark: undefined
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
  reset();
  open.value = true;
  title.value = "添加参数";
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset();
  const id = row.id || ids
  getConfig(id).then(response => {
    formData.value = response.data;
    open.value = true;
    title.value = "修改参数";
  });
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs["form"].validate(valid => {
    if (!valid) {
      return;
    }
    // 修改的提交
    if (formData.value.id != null) {
      updateConfig(formData.value).then(response => {
        proxy.$modal.msgSuccess("修改成功");
        open.value  = false;
        getList();
      });
      return;
    }
    // 添加的提交
    addConfig(formData.value).then(response => {
      proxy.$modal.msgSuccess("新增成功");
      open.value  = false;
      getList();
    });
  });
}

/** 删除按钮操作 */
function handleDelete(row) {
  const ids = row.id || ids;
  proxy.$modal.confirm('是否确认删除参数编号为"' + ids + '"的数据项?').then(function () {
    return delConfig(ids);
  }).then(() => {
    getList();
    proxy.$modal.msgSuccess("删除成功");
  }).catch(() => {
  });
}

/** 导出按钮操作 */
function handleExport() {
  proxy.$modal.confirm('是否确认导出所有参数数据项?').then(() => {
    exportLoading.value = true;
    return exportConfig(proxy.addDateRange(queryParams.value, [
      dateRange.value [0] ? dateRange.value [0] + ' 00:00:00' : undefined,
      dateRange.value [1] ? dateRange.value [1] + ' 23:59:59' : undefined,
    ]));
  }).then(response => {
    proxy.$download.excel(response, '参数配置.xls');
    exportLoading.value = false;
  }).catch(() => {
  });
}

getList();
</script>
