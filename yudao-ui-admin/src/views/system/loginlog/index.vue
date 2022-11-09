<template>
  <div class="app-container">
    <doc-alert title="系统日志" url="https://doc.iocoder.cn/system-log/" />
    <!-- 搜索工作栏 -->
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
      <el-form-item label="登录时间" prop="createTime">
        <el-date-picker v-model="queryParams.createTime" style="width: 240px" value-format="yyyy-MM-dd HH:mm:ss" type="daterange"
                        range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期" :default-time="['00:00:00', '23:59:59']" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" icon="el-icon-download" size="mini" @click="handleExport" :loading="exportLoading"
                   v-hasPermi="['system:login-log:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list">
      <el-table-column label="访问编号" align="center" prop="id" />
      <el-table-column label="日志类型" align="center" prop="logType" width="120">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_LOGIN_TYPE" :value="scope.row.logType" />
        </template>
      </el-table-column>
      <el-table-column label="用户名称" align="center" prop="username" />
      <el-table-column label="登录地址" align="center" prop="userIp" width="130" :show-overflow-tooltip="true" />
      <el-table-column label="userAgent" align="center" prop="userAgent" width="400" :show-overflow-tooltip="true" />
      <el-table-column label="结果" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :type="DICT_TYPE.SYSTEM_LOGIN_RESULT" :value="scope.row.result" />
        </template>
      </el-table-column>
      <el-table-column label="登录日期" align="center" prop="loginTime" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNo" :limit.sync="queryParams.pageSize"
                @pagination="getList"/>
  </div>
</template>

<script>
import { list, exportLoginLog } from "@/api/system/loginlog";

export default {
  name: "Logininfor",
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
      // 表格数据
      list: [],
      // 状态数据字典
      statusOptions: [],
      // 查询参数
      queryParams: {
        pageNo: 1,
        pageSize: 10,
        userIp: undefined,
        username: undefined,
        status: undefined,
        createTime: []
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    /** 查询登录日志列表 */
    getList() {
      this.loading = true;
      list(this.queryParams).then(response => {
          this.list = response.data.list;
          this.total = response.data.total;
          this.loading = false;
        }
      );
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
    /** 导出按钮操作 */
    handleExport() {
      this.$modal.confirm('是否确认导出所有操作日志数据项?').then(() => {
          // 处理查询参数
          let params = {...this.queryParams};
          params.pageNo = undefined;
          params.pageSize = undefined;
          this.exportLoading = true;
          return exportLoginLog(params);
        }).then(response => {
          this.$download.excel(response, '登录日志.xls');
          this.exportLoading = false;
      }).catch(() => {});
    }
  }
};
</script>

