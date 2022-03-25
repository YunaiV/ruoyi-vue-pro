<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="登录地址" prop="userIp">
        <el-input v-model="queryParams.userIp" placeholder="请输入登录地址" clearable style="width: 240px;"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="用户名称" prop="username">
        <el-input v-model="queryParams.username" placeholder="请输入用户名称" clearable style="width: 240px;"
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="结果" clearable style="width: 240px">
          <el-option :key="true" label="成功" :value="true"/>
          <el-option :key="false" label="失败" :value="false"/>
        </el-select>
      </el-form-item>
      <el-form-item label="登录时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期"
                        end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['system:login-log:export']">导出
        </el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="访问编号" align="center" prop="id"/>
      <el-table-column label="日志类型" align="center" prop="logType" width="120">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_LOGIN_TYPE" :value="scope.row.logType"/>
        </template>
      </el-table-column>
      <el-table-column label="用户名称" align="center" prop="username"/>
      <el-table-column label="登录地址" align="center" prop="userIp" width="130" :show-overflow-tooltip="true"/>
      <el-table-column label="userAgent" align="center" prop="userAgent" width="400" :show-overflow-tooltip="true"/>
      <el-table-column label="结果" align="center" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_LOGIN_RESULT" :value="scope.row.result"/>
        </template>
      </el-table-column>
      <el-table-column label="登录日期" align="center" prop="loginTime" width="180">
        <template #default="scope">
          <span>{{ proxy.parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total"  v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script setup name="Logininfor">
import {list as loginLogList, exportLoginLog} from "@/api/system/loginlog";

const {proxy} = getCurrentInstance();

const loading = ref(true);// 遮罩层
const exportLoading = ref(false);// 导出遮罩层
const showSearch = ref(true);// 显示搜索条件
const total = ref(0);// 总条数
const list = ref([]);// 表格数据
const open = ref(false);// 是否显示弹出层
const dateRange = ref([]);// 日期范围
const data = reactive({
  // 表单参数
  form: {},
  // 查询参数
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    userIp: undefined,
    username: undefined,
    status: undefined
  }
});
const {queryParams, form} = toRefs(data);


/** 查询登录日志列表 */
function getList() {
  loading.value = true;
  loginLogList(proxy.addDateRange(queryParams.value, [
    dateRange.value[0] ? dateRange.value[0] + ' 00:00:00' : undefined,
    dateRange.value[1] ? dateRange.value[1] + ' 23:59:59' : undefined,
  ])).then(response => {
        list.value = response.data.list;
        total.value = response.data.total;
        loading.value = false;
      }
  );
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

/** 导出按钮操作 */
function handleExport() {

  proxy.$modal.confirm('是否确认导出所有操作日志数据项?').then(() => {
    exportLoading.value = true;
    return exportLoginLog(queryParams.value);
  }).then(response => {
    proxy.$download.excel(response, '登录日志.xls');
    exportLoading.value = false;
  }).catch(() => {
  });
}

getList();
</script>

