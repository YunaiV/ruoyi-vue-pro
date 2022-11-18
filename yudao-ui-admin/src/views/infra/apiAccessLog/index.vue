<template>
  <div class="app-container">
    <doc-alert title="系统日志" url="https://doc.iocoder.cn/system-log/" />
    <!-- 搜索工作栏 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户编号" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户编号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="用户类型" prop="userType">
        <el-select v-model="queryParams.userType" placeholder="请选择用户类型" clearable>
          <el-option v-for="dict in this.getDictDatas(DICT_TYPE.USER_TYPE)"
                     :key="dict.value" :label="dict.label" :value="dict.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="应用名" prop="applicationName">
        <el-input v-model="queryParams.applicationName" placeholder="请输入应用名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="请求地址" prop="requestUrl">
        <el-input v-model="queryParams.requestUrl" placeholder="请输入请求地址" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="请求时间" prop="beginTime">
        <el-date-picker v-model="queryParams.beginTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item label="执行时长" prop="duration">
        <el-input v-model="queryParams.duration" placeholder="请输入执行时长" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="结果码" prop="resultCode">
        <el-input v-model="queryParams.resultCode" placeholder="请输入结果码" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作工具栏 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" :loading="exportLoading" @click="handleExport"
                   v-hasPermi="['infra:api-access-log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 列表 -->
    <el-table v-loading="loading" :data="list">
      <el-table-column label="日志编号" align="center" prop="id" />
      <el-table-column label="用户编号" align="center" prop="userId" />
      <el-table-column label="用户类型" align="center" prop="userType">
        <template v-slot="scope">
          <dict-tag :type="DICT_TYPE.USER_TYPE" :value="scope.row.userType"/>
        </template>
      </el-table-column>>
      <el-table-column label="应用名" align="center" prop="applicationName" />
      <el-table-column label="请求方法名" align="center" prop="requestMethod" />
      <el-table-column label="请求地址" align="center" prop="requestUrl" width="250" />
      <el-table-column label="请求时间" align="center" prop="beginTime" width="180">
        <template v-slot="scope">
          <span>{{ parseTime(scope.row.beginTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="执行时长" align="center" prop="startTime">
        <template v-slot="scope">
          <span>{{ scope.row.duration }}  ms</span>
        </template>
      </el-table-column>
      <el-table-column label="操作结果" align="center" prop="status">
        <template v-slot="scope">
          <span>{{ scope.row.resultCode === 0 ? '成功' : '失败(' + scope.row.resultMsg + ')' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template v-slot="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row,scope.index)"
                     v-hasPermi="['infra:api-access-log:query']">详细</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页组件 -->
    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>

    <!-- 查看明细 -->
    <el-dialog title="API 访问日志详细" :visible.sync="open" width="700px" append-to-body>
      <el-form ref="form" :model="form" label-width="100px" size="mini">
        <el-row>
          <el-col :span="24">
            <el-form-item label="日志主键：">{{ form.id }}</el-form-item>
            <el-form-item label="链路追踪：">{{ form.traceId }}</el-form-item>
            <el-form-item label="应用名：">{{ form.applicationName }}</el-form-item>
            <el-form-item label="用户信息：">
              {{ form.userId }} <dict-tag :type="DICT_TYPE.USER_TYPE" :value="form.userType"/> | {{ form.userIp }} | {{ form.userAgent}}
            </el-form-item>
            <el-form-item label="请求信息：">{{ form.requestMethod }} | {{ form.requestUrl }} </el-form-item>
            <el-form-item label="请求参数：">{{ form.requestParams }}</el-form-item>
            <el-form-item label="开始时间：">
              {{ parseTime(form.beginTime) }} ~ {{ parseTime(form.endTime) }} | {{ form.duration }} ms
            </el-form-item>
            <el-form-item label="操作结果：">
              <div v-if="form.resultCode === 0">正常</div>
              <div v-else-if="form.resultCode > 0">失败 | {{ form.resultCode }} || {{ form.resultMsg}}</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { getApiAccessLogPage, exportApiAccessLogExcel } from "@/api/infra/apiAccessLog";

export default {
  name: "ApiAccessLog",
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
      // API 访问日志列表
      list: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        userId: null,
        userType: null,
        applicationName: null,
        requestUrl: null,
        duration: null,
        resultCode: null,
        beginTime: []
      },
      // 表单参数
      form: {},
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
      getApiAccessLogPage(this.queryParams).then(response => {
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
      this.form = {};
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
    /** 详细按钮操作 */
    handleView(row) {
      this.open = true;
      this.form = row;
    },
    /** 导出按钮操作 */
    handleExport() {
      // 处理查询参数
      let params = {...this.queryParams};
      params.pageNo = undefined;
      params.pageSize = undefined;
      // 执行导出
      this.$modal.confirm('是否确认导出所有API 访问日志数据项?').then(() => {
        this.exportLoading = true;
        return exportApiAccessLogExcel(params);
      }).then(response => {
        this.$download.excel(response, 'API 访问日志.xls');
        this.exportLoading = false;
      }).catch(() => {});
    }
  }
};
</script>
