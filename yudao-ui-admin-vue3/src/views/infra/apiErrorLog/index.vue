<template>
  <div class="app-container">

    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户编号" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="用户类型" prop="userType">
        <el-select v-model="queryParams.userType" placeholder="请选择用户类型" clearable>
          <el-option v-for="dict in getDictDatas(DICT_TYPE.USER_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="应用名" prop="applicationName">
        <el-input v-model="queryParams.applicationName" placeholder="请输入应用名" clearable
                  @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="请求地址" prop="requestUrl">
        <el-input v-model="queryParams.requestUrl" placeholder="请输入请求地址" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="异常时间">
        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd"
                        type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"/>
      </el-form-item>
      <el-form-item label="处理状态" prop="processStatus">
        <el-select v-model="queryParams.processStatus" placeholder="请选择处理状态" clearable>
          <el-option v-for="dict in proxy.getDictDatas(DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="small" @click="handleExport"
                   :loading="exportLoading"
                   v-hasPermi="['infra:api-error-log:export']">导出
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="日志编号" align="center" prop="id"/>
      <el-table-column label="用户编号" align="center" prop="userId"/>
      <el-table-column label="用户类型" align="center" prop="userType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.USER_TYPE" :value="scope.row.userType"/>
        </template>
      </el-table-column>
      >
      <el-table-column label="应用名" align="center" prop="applicationName"/>
      <el-table-column label="请求方法名" align="center" prop="requestMethod"/>
      <el-table-column label="请求地址" align="center" prop="requestUrl" width="250"/>
      <el-table-column label="异常发生时间" align="center" prop="exceptionTime" width="180">
        <template #default="scope">
          <span>{{ proxy.parseTime(scope.row.exceptionTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="异常名" align="center" prop="exceptionName" width="250"/>
      <el-table-column label="处理状态" align="center" prop="processStatus">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS" :value="scope.row.processStatus"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button size="small" type="text" icon="el-icon-view" @click="handleView(scope.row,scope.index)"
                     v-hasPermi="['infra:api-access-log:query']">详细
          </el-button>
          <el-button type="text" size="small" icon="el-icon-check"
                     v-if="scope.row.processStatus === InfApiErrorLogProcessStatusEnum.INIT"
                     v-hasPermi="['infra:api-error-log:update-status']"
                     @click="handleProcessClick(scope.row, InfApiErrorLogProcessStatusEnum.DONE)">已处理
          </el-button>
          <el-button type="text" size="small" icon="el-icon-check"
                     v-if="scope.row.processStatus === InfApiErrorLogProcessStatusEnum.INIT"
                     v-hasPermi="['infra:api-error-log:update-status']"
                     @click="handleProcessClick(scope.row, InfApiErrorLogProcessStatusEnum.IGNORE)">已忽略
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页组件 -->
    <pagination v-show="total > 0" v-model:total="total" v-model:page="queryParams.pageNo" v-model:limit="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 查看明细 -->
    <el-dialog title="API 异常日志详细" v-model="open" width="1280px" append-to-body>
      <el-form ref="form" :model="formData" label-width="100px" size="mini">
        <el-row>
          <el-col :span="24">
            <el-form-item label="日志主键：">{{ formData.id }}</el-form-item>
            <el-form-item label="链路追踪：">{{ formData.traceId }}</el-form-item>
            <el-form-item label="应用名：">{{ formData.applicationName }}</el-form-item>
            <el-form-item label="用户信息：">
              {{ formData.userId }}
              <dict-tag :type="DICT_TYPE.USER_TYPE" :value="formData.userType"/>
              | {{ formData.userIp }} | {{ formData.userAgent }}
            </el-form-item>
            <el-form-item label="请求信息：">{{ formData.requestMethod }} | {{ formData.requestUrl }}</el-form-item>
            <el-form-item label="请求参数：">{{ formData.requestParams }}</el-form-item>
            <el-form-item label="异常时间：">{{ proxy.parseTime(formData.exceptionTime) }}</el-form-item>
            <el-form-item label="异常名">{{ formData.exceptionName }}</el-form-item>
            <el-form-item label="异常名">
              <el-input type="textarea" :readonly="true" :autosize="{ maxRows: 20}"
                        v-model="formData.exceptionStackTrace"></el-input>
            </el-form-item>
            <el-form-item label="处理状态">
              <dict-tag :type="DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS" :value="formData.processStatus"/>
            </el-form-item>
            <el-form-item label="处理人">{{ formData.processUserId }}</el-form-item>
            <el-form-item label="处理时间">{{ proxy.parseTime(formData.processTime) }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup name="ApiErrorLog">
import {updateApiErrorLogProcess, getApiErrorLogPage, exportApiErrorLogExcel} from "@/api/infra/apiErrorLog";
import {InfraApiErrorLogProcessStatusEnum} from '@/utils/constants'
const {proxy} = getCurrentInstance();


const loading = ref(true);// 遮罩层
const exportLoading = ref(false);// 导出遮罩层
const showSearch = ref(true);// 显示搜索条件
const total = ref(0);// 总条数
const list = ref([]);// 表格数据
const open = ref(false);// 是否显示弹出层
const dateRange = ref([]);// 日期范围
const InfApiErrorLogProcessStatusEnum = ref(InfraApiErrorLogProcessStatusEnum);

const data = reactive({
  // 表单参数
  formData: {},
  // 查询参数
  queryParams: {
    pageNo: 1,
    pageSize: 10,
    userId: null,
    userType: null,
    applicationName: null,
    requestUrl: null,
    processStatus: null,
  }
});

const {formData, queryParams} = toRefs(data);


/** 查询列表 */
function getList() {


  loading.value = true;
  // 处理查询参数
  let params = queryParams.value;
  proxy.addBeginAndEndTime(params, dateRange.value, 'exceptionTime');
  // 执行查询
  getApiErrorLogPage(params).then(response => {
    //loading.vlaue = false;
    list.value = response.data.list;
    total.value = response.data.total;
    //不知道什么原因，数据查询完成后会变成载入中状态，延迟赋值
    setTimeout(function(){
      loading.value=false;
    },50)

  });
}

/** 取消按钮 */
function cancel() {
  open.value = false;
  reset();
}

/** 表单重置 */
function reset() {
  formData.value = {};
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

/** 详细按钮操作 */
function handleView(row) {
  open.value = true;
  formData.value = row;
}

/** 处理已处理 / 已忽略的操作 **/
function handleProcessClick(row, processStatus) {
  const processStatusText = proxy.getDictDataLabel(DICT_TYPE.INFRA_API_ERROR_LOG_PROCESS_STATUS, processStatus)
  proxy.$modal.confirm('确认标记为' + processStatusText).then(() => {
    updateApiErrorLogProcess(row.id, processStatus).then(() => {
      proxy.$modal.msgSuccess("修改成功");
      getList();
    });
  }).catch(() => {
  });
}

/** 导出按钮操作 */
function handleExport() {
  // 处理查询参数
  let params = {...queryParams.value};
  params.pageNo = undefined;
  params.pageSize = undefined;
  proxy.addBeginAndEndTime(params, dateRange.value, 'exceptionTime');
  // 执行导出
  proxy.$modal.confirm('是否确认导出所有API 错误日志数据项?').then(() => {
    exportLoading.value = true;
    return exportApiErrorLogExcel(params);
  }).then(response => {
    proxy.$download.excel(response, 'API 错误日志.xls');
    exportLoading.value = false;
  }).catch(() => {
  });
}

getList();
</script>
